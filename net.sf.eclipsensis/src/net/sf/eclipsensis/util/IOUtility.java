/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

public class IOUtility
{
    public static final IOUtility INSTANCE = new IOUtility();
    
    private static final String cPathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
    private static final String cOnePathLevelUp = ".." + cPathSeparator; //$NON-NLS-1$
    private static Pattern cValidPathName = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathNameSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidPathSpec = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathSpecSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidFileName = Pattern.compile("(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidURL = Pattern.compile("(?:(?:ftp|https?):\\/\\/)?(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|edu|biz|org|gov|int|info|mil|net|name|museum|coop|aero|[a-z][a-z])\\b(?:\\d+)?(?:\\/[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]*(?:[.,?]+[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]+)*)?"); //$NON-NLS-1$

    private static HashMap cBundleResources = new HashMap();
    
    private IOUtility()
    {
    }

    public static String getFileExtension(File file)
    {
        String name = file.getName();
        int n = name.lastIndexOf('.');
        if(n >= 0) {
            return name.substring(n+1);
        }
        return null;
    }

    public static String encodePath(String path)
    {
        String nsisdirKeyword = NSISKeywords.getInstance().getKeyword("${NSISDIR}"); //$NON-NLS-1$
        String nsisHome = NSISPreferences.INSTANCE.getNSISHome().toLowerCase();
        if(path.toLowerCase().startsWith(nsisHome)) {
            path = nsisdirKeyword + path.substring(nsisHome.length());
        }
        return path;
    }

    public static String decodePath(String path)
    {
        String nsisdirKeyword = NSISKeywords.getInstance().getKeyword("${NSISDIR}").toLowerCase(); //$NON-NLS-1$
        String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
        if(path.toLowerCase().startsWith(nsisdirKeyword)) {
            path = nsisHome + path.substring(nsisdirKeyword.length());
        }
        return path;
    }

    public static Object readObject(File file) throws IOException, ClassNotFoundException
    {
        return readObject(file, null);
    }

    public static Object readObject(File file, ClassLoader classLoader) throws IOException, ClassNotFoundException
    {
        return readObject(new BufferedInputStream(new FileInputStream(file)), classLoader);
    }

    public static Object readObject(InputStream inputStream) throws IOException, ClassNotFoundException
    {
        return readObject(inputStream, null);
    }

    public static Object readObject(InputStream inputStream, final ClassLoader classLoader) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = null;
        try {
            if(!(inputStream instanceof BufferedInputStream)) {
                inputStream = new BufferedInputStream(inputStream);
            }
            ois = new ObjectInputStream(inputStream){
    
                protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
                {
                    if(classLoader != null) {
                        try {
                            return Class.forName(desc.getName(), false, classLoader);
                        }
                        catch(ClassNotFoundException e) {}
                    }
                    return super.resolveClass(desc);
                }
            };
            return ois.readObject();
        }
        finally {
            closeIO(ois);
            closeIO(inputStream);
        }
    }

    /*
     *     protected Class resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException
    {
    String name = desc.getName();
    try {
        return Class.forName(name, false, latestUserDefinedLoader());
    } catch (ClassNotFoundException ex) {
        Class cl = (Class) primClasses.get(name);
        if (cl != null) {
        return cl;
        } else {
        throw ex;
        }
    }
    }
    
     */
    public static void closeIO(Object object)
    {
        if(object != null) {
            try {
                if(object instanceof InputStream) {
                    InputStream is = (InputStream)object;
                    try {
                        int count = 0;
                        while(count < 100 && is.available() > 0) {
                            try {
                                Thread.sleep(10);
                            }
                            catch (InterruptedException e1) {
                            }
                            count++;
                        }
                    }
                    catch (IOException e) {
                    }
                    is.close();
                }
                else if(object instanceof OutputStream) {
                    OutputStream os = (OutputStream)object;
                    try {
                        os.flush();
                    }
                    catch (IOException e) {
                    }
                    os.close();
                }
                else if(object instanceof Reader) {
                    Reader r = (Reader)object;
                    try {
                        int count = 0;
                        while(count < 100 && r.ready()) {
                            try {
                                Thread.sleep(10);
                            }
                            catch (InterruptedException e1) {
                                EclipseNSISPlugin.getDefault().log(e1);
                            }
                            count++;
                        }
                    }
                    catch (IOException e) {
                    }
                    r.close();
                }
                else if(object instanceof Writer) {
                    Writer w = (Writer)object;
                    try {
                        w.flush();
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    w.close();
                }
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    public static void writeObject(File file, Object object) throws IOException
    {
        writeObject(new BufferedOutputStream(new FileOutputStream(file)), object);
    }

    public static void writeObject(OutputStream outputStream, Object object) throws IOException
    {
        if(object != null) {
            ObjectOutputStream oos = null;
    
            try {
                if(!(outputStream instanceof BufferedOutputStream)) {
                    outputStream = new BufferedOutputStream(outputStream);
                }
                oos = new ObjectOutputStream(outputStream);
                oos.writeObject(object);
            }
            finally {
                closeIO(oos);
                closeIO(outputStream);
            }
        }
    }

    public static boolean isValidNSISPathName(String pathName)
    {
        return isValidNSISPathNameOrSpec(pathName, cValidNSISPrefixedPathNameSuffix, cValidPathName);
    }

    public static boolean isValidNSISPathSpec(String pathName)
    {
        return isValidNSISPathNameOrSpec(pathName, cValidNSISPrefixedPathSpecSuffix, cValidPathSpec);
    }

    public static boolean isValidPathName(String pathName)
    {
        return isValidPathNameOrSpec(pathName, cValidPathName);
    }

    public static boolean isValidPathSpec(String pathName)
    {
        return isValidPathNameOrSpec(pathName, cValidPathSpec);
    }

    private static boolean isValidNSISPathNameOrSpec(String pathName, Pattern nsisPath, Pattern path)
    {
        int n = pathName.indexOf('\\');
        String suffix = null;
        String prefix = null;
        if(n >= 1) {
            suffix = pathName.substring(n);
            prefix = pathName.substring(0,n);
        }
        else {
            prefix = pathName;
        }
        if(!Common.isEmpty(prefix)) {
            String[] array = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES);
            for(int i=0; i<array.length; i++) {
                if(array[i].equalsIgnoreCase(prefix)) {
                    if(!Common.isEmpty(suffix)) {
                        Matcher matcher = nsisPath.matcher(suffix);
                        return matcher.matches();
                    }
                    return true;
                }
            }
        }
        return isValidPathNameOrSpec(pathName, path);
    }

    static boolean isValidPathNameOrSpec(String pathName, Pattern path)
    {
        Matcher matcher = path.matcher(pathName);
        return matcher.matches();
    }

    public static boolean isValidFileName(String fileName)
    {
        Matcher matcher = cValidFileName.matcher(fileName);
        return matcher.matches();
    }

    public static boolean isValidFile(String fileName)
    {
        File file = new File(fileName);
        return (file.exists() && file.isFile());
    }

    public static boolean isValidPath(String pathName)
    {
        File file = new File(pathName);
        return (file.exists() && file.isDirectory());
    }

    public static boolean isValidURL(String url)
    {
        Matcher matcher = cValidURL.matcher(url);
        return matcher.matches();
    }

    public static String makeNSISRelativeLocation(String pathName)
    {
        if(!Common.isEmpty(pathName)) {
            String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
            if(!Common.isEmpty(nsisHome) && pathName.regionMatches(true,0,nsisHome,0,nsisHome.length())) {
                boolean shouldQuote = Common.shouldQuote(pathName);
                pathName = NSISKeywords.getInstance().getKeyword("${NSISDIR}")+pathName.substring(nsisHome.length()); //$NON-NLS-1$
                if(shouldQuote) {
                    pathName = Common.quote(pathName);
                }
            }
        }
        return pathName;
    }

    public static String makeRelativeLocation(IResource resource, String pathname)
    {
        if(resource instanceof IContainer || resource instanceof IFile) {
            return makeRelativeLocation((resource instanceof IContainer?(IContainer)resource:((IFile)resource).getParent()).getLocation(), pathname);
        }
        return pathname;
    }

    public static String makeRelativeLocation(File file, String pathname)
    {
        if(file != null) {
            String filepath = file.isFile()?file.getParent():file.getPath();
            if(filepath != null) {
                return makeRelativeLocation(new Path(filepath), pathname);
            }
        }
        return pathname;
    }

    private static String makeRelativeLocation(IPath reference, String pathname)
    {
        if(!Common.isEmpty(pathname)) {
            IPath childPath = new Path(pathname);
            if(reference.isAbsolute() && childPath.isAbsolute()) {
              if(Common.stringsAreEqual(reference.getDevice(), childPath.getDevice(), true)) {
                  StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                  int l1 = reference.segmentCount();
                  int l2 = childPath.segmentCount();
                  int n = Math.min(l1,l2);
    
                  int i=0;
                  for(; i<n; i++) {
                      if(!reference.segment(i).equalsIgnoreCase(childPath.segment(i))) {
                          break;
                      }
                  }
    
                  for(int j=i; j<l1; j++) {
                      buf.append(cOnePathLevelUp);
                  }
                  for(int j=i; j<l2-1; j++) {
                      buf.append(childPath.segment(j)).append(cPathSeparator);
                  }
                  buf.append(childPath.lastSegment());
                  childPath = new Path(buf.toString());
              }
            }
            return childPath.toOSString();
        }
        return pathname;
    }

    public static void writeContentToFile(File file, byte[] content)
    {
        if(!file.canWrite()) {
            file.delete();
        }
        ByteBuffer buf = ByteBuffer.wrap(content);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.getChannel().write(buf);
        }
        catch (FileNotFoundException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        finally {
            try {
                fos.close();
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    public static byte[] loadContentFromFile(File file) {
        byte[] bytes = new byte[0];
        if(file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                bytes = readChannel(fis.getChannel());
            }
            catch (FileNotFoundException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            finally {
                if(fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
        }
        return bytes;
    }

    public static byte[] loadContentFromStream(InputStream stream)
    {
        try {
            return readChannel(Channels.newChannel(stream));
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
            return new byte[0];
        }
    }

    public static byte[] readChannel(ReadableByteChannel channel) throws IOException
    {
        byte[] bytes = new byte[0];
        ByteBuffer buf = ByteBuffer.allocateDirect(8192);
        int numread = channel.read(buf);
        while(numread > 0) {
            buf.rewind();
            int n = bytes.length;
            bytes = (byte[])Common.resizeArray(bytes, n+numread);
            buf.get(bytes,n,numread);
            buf.rewind();
            numread = channel.read(buf);
        }
        return bytes;
    }

    public static File ensureLatest(Bundle bundle, IPath source, File destFolder) throws IOException
    {
        if(destFolder.exists() && destFolder.isFile()) {
            destFolder.delete();
        }
        if(!destFolder.exists()) {
            destFolder.mkdirs();
        }
        BundleResource key = new BundleResource(bundle,source);
        long lastModified;
        Long l = (Long)cBundleResources.get(key);
        if(l != null) {
            lastModified = l.longValue();
        }
        else {
            lastModified = bundle.getLastModified();
            try {
                IPath relative = source.makeRelative();
                URL url = Platform.resolve(bundle.getEntry("/"));
                if (url.getProtocol().equalsIgnoreCase("file")) {
                    File original = new File(url.getFile(), relative.toString());
                    if (original.exists() && original.isFile()) {
                        lastModified = original.lastModified();
                    }
                }
                else if (url.getProtocol().equals("jar")) {
                    JarURLConnection conn = (JarURLConnection)url.openConnection();
                    JarFile jarFile = conn.getJarFile();
                    JarEntry entry = jarFile.getJarEntry(relative.toString());
                    if (entry != null && !entry.isDirectory()) {
                        lastModified = entry.getTime();
                    }
                }
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
        
        File destFile = new File(destFolder,source.lastSegment()); //$NON-NLS-1$
        if(destFile.exists()) {
            if(destFile.isDirectory()) {
                destFile.delete();
            }
            else {
                if (destFile.lastModified() >= lastModified) {
                    return destFile;
                }
                destFile.delete();
            }
        }
        URL url = bundle.getEntry(source.toString()); //$NON-NLS-1$
        InputStream is = null;
        byte[] data;
        try {
            is = new BufferedInputStream(url.openStream());
            data = loadContentFromStream(is);
        }
        finally {
            closeIO(is);
        }
        writeContentToFile(destFile,data);
        destFile.setLastModified(lastModified);
        cBundleResources.put(key, new Long(lastModified));
        return destFile;
    }
    
    private static class BundleResource
    {
        private Bundle mBundle;
        private IPath mResource;

        public BundleResource(Bundle bundle, IPath resource)
        {
            mBundle = bundle;
            mResource = resource;
        }

        public boolean equals(Object obj)
        {
            if(obj instanceof BundleResource) {
                BundleResource other = (BundleResource)obj;
                return mBundle.equals(other.mBundle) && mResource.equals(other.mResource);
            }
            return false;
        }

        public int hashCode()
        {
            return mBundle.hashCode() << 16 + mResource.hashCode();
        }
    }
}