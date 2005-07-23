/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.beans.*;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.dgc.VMID;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.*;

/**
 * Common class is the Swiss Army Knife of the project. Most miscellaneous utility functions
 * have been dumped in here.
 * 
 * @author Sunil.Kamath
 */
public class Common
{
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String cPathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
    private static final String cOnePathLevelUp = ".." + cPathSeparator; //$NON-NLS-1$

    private static Pattern cValidPathName = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathNameSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidFileName = Pattern.compile("(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidURL = Pattern.compile("(?:(?:ftp|https?):\\/\\/)?(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|edu|biz|org|gov|int|info|mil|net|name|museum|coop|aero|[a-z][a-z])\\b(?:\\d+)?(?:\\/[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]*(?:[.,?]+[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]+)*)?"); //$NON-NLS-1$

    
    public static String encodePath(String path)
    {
        String nsisdirKeyword = NSISKeywords.INSTANCE.getKeyword("${NSISDIR}"); //$NON-NLS-1$
        String nsisHome = NSISPreferences.getPreferences().getNSISHome().toLowerCase();
        if(path.toLowerCase().startsWith(nsisHome)) {
            path = nsisdirKeyword + path.substring(nsisHome.length());
        }
        return path;
    }

    public static String decodePath(String path)
    {
        String nsisdirKeyword = NSISKeywords.INSTANCE.getKeyword("${NSISDIR}").toLowerCase(); //$NON-NLS-1$
        String nsisHome = NSISPreferences.getPreferences().getNSISHome();
        if(path.toLowerCase().startsWith(nsisdirKeyword)) {
            path = nsisHome + path.substring(nsisdirKeyword.length());
        }
        return path;
    }

    public static Object readObject(File file) throws IOException, ClassNotFoundException
    {
        return readObject(new BufferedInputStream(new FileInputStream(file)));
    }

    public static Object readObject(InputStream inputStream) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = null;
        try {
            if(!(inputStream instanceof BufferedInputStream)) {
                inputStream = new BufferedInputStream(inputStream);
            }
            ois = new ObjectInputStream(inputStream);
            return ois.readObject();
        }
        finally {
            closeIO(ois);
            closeIO(inputStream);
        }
    }
    
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
                                e1.printStackTrace();
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
                        e.printStackTrace();
                    }
                    w.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
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

    public static boolean isEmpty(String string)
    {
        return (string == null || string.trim().length() == 0);
    }

    /**
     * Flip array
     *
     * @param array       Array to be resized
     */
    public static void flipArray(Object array) 
    {
        if(array != null && array.getClass().isArray()) {
            int len = Array.getLength(array);
            int n = (2*len+1)/4;
            for(int i=0; i<n; i++) {
                int j = len - 1 - i;
                Object temp = Array.get(array,i);
                Array.set(array,i,Array.get(array,j));
                Array.set(array,j,temp);
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Resize array while keeping existing elements
     *
     * @param array       Array to be resized
     * @param offset      Offset index
     * @param newLength   New length of array
     * @return            New array of different size
     */
    public static Object resizeArray(Object array, int offset, int newLength) 
    {
        if (array == null || newLength < 0)
        {
            throw new IllegalArgumentException();
        }
        Class c = array.getClass();
        if ( !c.isArray() ) {
            throw new IllegalArgumentException();
        }
        Object newArray = Array.newInstance(c.getComponentType(), newLength);
        int oldLength = Array.getLength(array);
        System.arraycopy(array,offset,newArray,0,Math.min(oldLength - offset, newLength));
        return newArray;
    }

    /**
     * Resize array while keeping existing elements
     *
     * @param array       Array to be resized
     * @param newLength   New length of array
     * @return            New array of different size
     */
    public static Object resizeArray(Object array, int newLength) 
    {
        return resizeArray(array,0,newLength);
    }
    
    /**
     * Get subset of array
     *
     * @param array       Input array
     * @param beginOffset Begin offset (inclusive)
     * @param endOffset   End offset (exclusive)
     * @return            Sub array
     */
    public static Object subArray(Object array, int beginOffset, int endOffset)
    {
        if(array != null && array.getClass().isArray()) {
            int n = Array.getLength(array);
            if(beginOffset >= 0 && endOffset <= n && endOffset >= beginOffset) {
                return resizeArray(array,beginOffset, endOffset-beginOffset);
            }
            else {
                throw new IndexOutOfBoundsException();
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public static Object joinArrays(Object[] arrays)
    {
        Object newArray = null;
        if(!isEmptyArray(arrays)) {
            Class clasz = null;
            int count = 0;
            int[] lengths = new int[arrays.length];
            for (int i = 0; i < arrays.length; i++) {
                if(arrays[i] != null) {
                    Class arrayClass = arrays[i].getClass();
                    if(arrayClass.isArray()) {
                        lengths[i] = Array.getLength(arrays[i]);
                        count += lengths[i];
                        if(clasz == null) {
                            clasz = arrayClass.getComponentType(); 
                        }
                        else {
                            Class clasz2 = arrayClass.getComponentType();
                            if(!clasz2.equals(clasz)) {
                                if(clasz.isAssignableFrom(clasz2)) {
                                    continue;
                                }
                                else if(clasz2.isAssignableFrom(clasz)) {
                                    clasz = clasz2;
                                }
                                else {
                                    clasz = Object.class;
                                }
                            }
                        }
                    }
                    else {
                        throw new IllegalArgumentException();
                    }
                }
                else {
                    lengths[i] = 0;
                }
            }
            
            newArray = Array.newInstance(clasz, count);
            int n = 0;
            for (int i = 0; i < arrays.length; i++) {
                if(lengths[i] > 0) {
                    System.arraycopy(arrays[i],0,newArray,n,lengths[i]);
                    n += lengths[i];
                }
            }
        }
        return newArray;
    }

    /**
     * Append one array to another
     *
     * @param oldArray    The target array
     * @param newArray    The array to be appended
     * @return            Appended array
     */
    public static Object appendArray(Object oldArray, Object newArray) 
    {
        return appendArray(oldArray, newArray, 0, Array.getLength(newArray));
    }

    /**
     * Append one array to another
     *
     * @param oldArray    The target array
     * @param newArray    The array to be appended
     * @return            Appended array
     */
    public static Object appendArray(Object oldArray, Object newArray, int startIndex, int length) 
    {
        if (isEmptyArray(newArray))
        {
            return oldArray;
        }
        Class newClass = newArray.getClass();
        if(!newClass.isArray()) {
            throw new IllegalArgumentException();
        }
        if(isEmptyArray(oldArray)) {
            return cloneArray(newArray);
        }
        Class oldClass = oldArray.getClass();
        if(!oldClass.isArray()) {
            throw new IllegalArgumentException();
        }
        
        Object appendedArray = null;
        if(newClass.equals(oldClass) || oldClass.isAssignableFrom(newClass)) {
            int oldLength = Array.getLength(oldArray);
            int newLength = Array.getLength(newArray);
            startIndex = Math.min(startIndex,newLength-1);
            newLength = Math.min(newLength,newLength-startIndex);
            appendedArray = resizeArray(oldArray, oldLength+newLength);
            System.arraycopy(newArray,startIndex,appendedArray,oldLength,newLength);
        }
        return appendedArray;
    }

    /**
     * Clone an array
     *
     * @param array       Array to be cloned
     * @return            Clone of the array
     */
    public static Object cloneArray(Object array) 
    {

        Class clasz = array.getClass();
        if(clasz.isArray()) {
            Object arrayClone = Array.newInstance(clasz.getComponentType(),
                                                  Array.getLength(array));
            System.arraycopy(array,0,arrayClone,0,Array.getLength(arrayClone));
            return arrayClone;
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Check for an empty array
     *
     * @param array       Array to be tested
     * @return            True if the array is null or length is zero
     */
    public static boolean isEmptyArray(Object array)
    {
        if(array != null) {
            if(array.getClass().isArray()) {
                return (Array.getLength(array) == 0);
            }
        }
        return true;
    }

    /**
     * Check for an empty collection
     *
     * @param collection       Collection to be tested
     * @return            True if the collection is null or size is zero
     */
    public static boolean isEmptyCollection(Collection collection)
    {
        if(collection != null) {
            return (collection.size() == 0);
        }
        return true;
    }

    /**
     * Check for an empty map
     *
     * @param map       Map to be tested
     * @return          True if the map is null or size is zero
     */
    public static boolean isEmptyMap(Map map)
    {
        if(map != null) {
            return (map.size() == 0);
        }
        return true;
    }

    public static String[] runProcessWithOutput(String[] cmdArray, File workDir)
    {
        return runProcessWithOutput(cmdArray, workDir, 0);
    }

    public static String[] runProcessWithOutput(String[] cmdArray, File workDir, int validReturnCode)
    {
        String[] output = null;
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray, null, workDir);
            new Thread(new RunnableInputStreamReader(proc.getErrorStream(),false)).start();
            output = new RunnableInputStreamReader(proc.getInputStream()).getOutput();
            int rv = proc.waitFor();
            if(rv != validReturnCode) {
                output = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            output = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            output = null;
        }

        return output;
    }

    public static String leftPad(String text, int length, char padChar)
    {
        if(text.length() < length) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for(int i=text.length(); i<length; i++) {
                buf.append(padChar);
            }
            buf.append(text);
            text = buf.toString();
        }
        return text;
    }

    public static String flatten(Object[] array, char separator)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmptyArray(array)) {
            buf.append(array[0]);
            for (int i = 1; i < array.length; i++) {
                buf.append(separator).append((array[i]==null?"":array[i])); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    public static String[] tokenize(String text, char separator)
    {
        ArrayList list = new ArrayList();
        if(text != null && text.length() > 0) {
            char[] chars = text.toCharArray();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] != separator) {
                    buf.append(chars[i]);
                }
                else {
                    list.add(buf.toString());
                    buf.delete(0,buf.length());
                }
            }
            list.add(buf.toString().trim());
        }
        return (String[])list.toArray(new String[0]);
    }

    public static String[] loadArrayProperty(ResourceBundle bundle, String propertyName)
    {
        String[] array = EMPTY_STRING_ARRAY;
        if(bundle != null) {
            String property = null;
            try {
                property = bundle.getString(propertyName);
            }
            catch(MissingResourceException mre) {
                property = null;
            }
            if(!isEmpty(property)) {
                StringTokenizer st = new StringTokenizer(property,","); //$NON-NLS-1$
                ArrayList list = new ArrayList();
                while(st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if(!isEmpty(token)) {
                        list.add(token.trim());
                    }
                }
                array = (String[])list.toArray(EMPTY_STRING_ARRAY);
            }
        }
        return array;
    }

    public static Map loadMapProperty(ResourceBundle bundle, String propertyName)
    {
        Map map = new LinkedHashMap();
        if(bundle != null) {
            String property = bundle.getString(propertyName);
            if(!isEmpty(property)) {
                StringTokenizer st = new StringTokenizer(property,","); //$NON-NLS-1$
                while(st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int n=token.indexOf("="); //$NON-NLS-1$
                    if(n > 0) {
                        String key = token.substring(0,n).trim();
                        String value = null;
                        if(n < token.length() - 1) {
                            value = token.substring(n+1).trim();
                        }
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    public static boolean isValidNSISPathName(String pathName)
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
            String[] array = NSISKeywords.INSTANCE.getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES);
            for(int i=0; i<array.length; i++) {
                if(array[i].equalsIgnoreCase(prefix)) {
                    if(!Common.isEmpty(suffix)) {
                        Matcher matcher = cValidNSISPrefixedPathNameSuffix.matcher(suffix);
                        return matcher.matches();
                    }
                    return true;
                }
            }
        }
        return isValidPathName(pathName);
    }

    public static boolean isValidPathName(String pathName)
    {
        Matcher matcher = cValidPathName.matcher(pathName);
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

    public static void beanToStore(Object bean, IPreferenceStore store, java.util.List properties)
    {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                String name = pd[i].getName();
                if(properties.contains(name)) {
                    Class clasz = pd[i].getPropertyType();
                    Method m = pd[i].getReadMethod();
                    try {
                        if(clasz.equals(Boolean.class) || clasz.equals(Boolean.TYPE)) {
                            Boolean b = (Boolean)m.invoke(bean,null);
                            store.setValue(name,(b==null?false:b.booleanValue()));
                        }
                        else if(clasz.equals(Integer.class) || clasz.equals(Integer.TYPE)) {
                            Integer n = (Integer)m.invoke(bean,null);
                            store.setValue(name,(n==null?0:n.intValue()));
                        }
                        else if(clasz.equals(Long.class) || clasz.equals(Long.TYPE)) {
                            Long l = (Long)m.invoke(bean,null);
                            store.setValue(name,(l==null?0:l.longValue()));
                        }
                        else if(clasz.equals(Double.class) || clasz.equals(Double.TYPE)) {
                            Double d = (Double)m.invoke(bean,null);
                            store.setValue(name,(d==null?0:d.doubleValue()));
                        }
                        else if(clasz.equals(Float.class) || clasz.equals(Float.TYPE)) {
                            Float f = (Float)m.invoke(bean,null);
                            store.setValue(name,(f==null?0:f.floatValue()));
                        }
                        else if(clasz.equals(String.class)) {
                            String s = (String)m.invoke(bean,null);
                            store.setValue(name,(s==null?"":s)); //$NON-NLS-1$
                        }
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public static void storeToBean(Object bean, IPreferenceStore store, java.util.List properties)
    {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = beanInfo.getPropertyDescriptors();
            Object[] args = new Object[1];

            for (int i = 0; i < pd.length; i++) {
                String name = pd[i].getName();
                if(properties.contains(name)) {
                    Class clasz = pd[i].getPropertyType();
                    Method m = pd[i].getWriteMethod();
                    try {
                        if(clasz.equals(Boolean.class) || clasz.equals(Boolean.TYPE)) {
                            args[0] = Boolean.valueOf(store.getBoolean(name));
                        }
                        else if(clasz.equals(Integer.class) || clasz.equals(Integer.TYPE)) {
                            args[0] = new Integer(store.getInt(name));
                        }
                        else if(clasz.equals(Long.class) || clasz.equals(Long.TYPE)) {
                            args[0] = new Long(store.getLong(name));
                        }
                        else if(clasz.equals(Double.class) || clasz.equals(Double.TYPE)) {
                            args[0] = new Double(store.getDouble(name));
                        }
                        else if(clasz.equals(Float.class) || clasz.equals(Float.TYPE)) {
                            args[0] = new Float(store.getFloat(name));
                        }
                        else if(clasz.equals(String.class)) {
                            String value = store.getString(name);
                            args[0] = (value==null?"":value); //$NON-NLS-1$
                        }
                        else {
                            continue;
                        }
                        m.invoke(bean, args);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public static boolean stringsAreEqual(String str1, String str2)
    {
        return stringsAreEqual(str1, str2, false);
    }

    public static boolean stringsAreEqual(String str1, String str2, boolean ignoreCase)
    {
        return ((str1 == null && str2 == null) ||
                (str1 !=null && str2 != null && (ignoreCase?str1.equalsIgnoreCase(str2):str1.equals(str2))));
    }

    public static String makeRelativeLocation(IResource resource, String pathname)
    {
        Path childPath = new Path(pathname);
        if(resource instanceof IContainer || resource instanceof IFile) {
            IPath reference = (resource instanceof IContainer?(IContainer)resource:((IFile)resource).getParent()).getLocation();
            if(reference.isAbsolute() && childPath.isAbsolute()) {
              if(stringsAreEqual(reference.getDevice(), childPath.getDevice(), true)) {
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
        }
        return childPath.toOSString();
    }

    public static String[] formatLines(String text, int maxLength) 
    {
        ArrayList lines = new ArrayList();
        BreakIterator boundary = BreakIterator.getLineInstance();
        boundary.setText(text);
        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;
    
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        while (end != BreakIterator.DONE) {
            String word = text.substring(start,end);
            lineLength = lineLength + word.length();
            if (lineLength >= maxLength) {
                lines.add(buf.toString());
                buf.delete(0,buf.length());
                lineLength = word.length();
            }
            buf.append(word);
            start = end;
            end = boundary.next();
        }
        if(buf.length() > 0) {
            lines.add(buf.toString());
        }
        return (String[])lines.toArray(EMPTY_STRING_ARRAY);
    }
    
    public static String generateUniqueName(String prefix, String suffix)
    {
        StringBuffer name = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmpty(prefix)) {
            name.append(prefix);
        }
        name.append(new VMID().toString().replaceAll("[:-]","")); //$NON-NLS-1$ //$NON-NLS-2$
        if(!Common.isEmpty(suffix)) {
            name.append(suffix);
        }
        return name.toString();
    }
    
    public static void openError(Shell shell, String message)
    {
        MessageDialog.openError(shell, EclipseNSISPlugin.getResourceString("error.title"), message); //$NON-NLS-1$
    }
    
    public static void openWarning(Shell shell, String message)
    {
        MessageDialog.openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), message); //$NON-NLS-1$
    }
    
    public static boolean openConfirm(Shell shell, String message)
    {
        return MessageDialog.openConfirm(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                          message);
    }
    
    public static boolean openQuestion(Shell shell, String message)
    {
        return MessageDialog.openQuestion(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                          message);
    }
    
    public static Point calculateControlSize(Control control, int chars, int lines)
    {
        Point pt = new Point(0,0);
        GC gc = new GC(control);
        FontMetrics fontMetrics = gc.getFontMetrics();
        if(chars > 0) {
            pt.x = chars*fontMetrics.getAverageCharWidth();
        }
        if(lines > 0) {
            pt.y = lines*fontMetrics.getHeight();
        }
        gc.dispose();
        return pt;
    }
    
    public static void addAttribute(Document document, Node node, String name, String value)
    {
        if(value != null) {
            Attr attribute = document.createAttribute(name);
            attribute.setValue(value);
            node.getAttributes().setNamedItem(attribute);
        }
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
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
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
            e.printStackTrace();
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
            bytes = (byte[])resizeArray(bytes, n+numread);
            buf.get(bytes,n,numread);
            buf.rewind();
            numread = channel.read(buf);
        }
        return bytes;
    }
}
