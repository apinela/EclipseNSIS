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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.help.NSISKeywords;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Common
{
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static XStream cXStream = new XStream(new DomDriver());

    private static String[] cEnv = null;
    private static final String cPathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
    private static final String cOnePathLevelUp = ".." + cPathSeparator; //$NON-NLS-1$

    private static Pattern cValidPathName = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathNameSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidFileName = Pattern.compile("(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\&_\\^\\x20])+"); //$NON-NLS-1$
    private static Pattern cValidURL = Pattern.compile("(?:(?:ftp|https?):\\/\\/)?(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|edu|biz|org|gov|int|info|mil|net|name|museum|coop|aero|[a-z][a-z])\\b(?:\\d+)?(?:\\/[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]*(?:[.,?]+[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]+)*)?"); //$NON-NLS-1$

    public static Object readObjectXML(File file) throws IOException, ClassNotFoundException
    {
        return readObjectXML(new BufferedReader(new FileReader(file)));
    }

    public static Object readObjectXML(Reader reader) throws IOException, ClassNotFoundException
    {
        ArrayList objectList = new ArrayList();
        ObjectInputStream ois = null;
        try {
            ois = cXStream.createObjectInputStream(reader);
            return ois.readObject();
        }
        finally {
            closeIO(ois);
            closeIO(reader);
        }
    }

    public static void writeObjectXML(File file, Object object) throws IOException
    {
        writeObjectXML(new BufferedWriter(new FileWriter(file)), object);
    }

    public static void writeObjectXML(Writer writer, Object object) throws IOException
    {
        if(object != null) {
            ObjectOutputStream oos = null;
    
            try {
                oos = cXStream.createObjectOutputStream(writer);
                oos.writeObject(object);
            }
            finally {
                closeIO(oos);
                closeIO(writer);
            }
        }
    }


    public static Object readObject(File file) throws IOException, ClassNotFoundException
    {
        return readObject(new BufferedInputStream(new FileInputStream(file)));
    }

    public static Object readObject(InputStream inputStream) throws IOException, ClassNotFoundException
    {
        ArrayList objectList = new ArrayList();
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
                    ((InputStream)object).close();
                }
                else if(object instanceof OutputStream) {
                    ((OutputStream)object).close();
                }
                else if(object instanceof Reader) {
                    ((Reader)object).close();
                }
                else if(object instanceof Writer) {
                    ((Writer)object).close();
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
        if (newArray == null)
        {
            return oldArray;
        }
        Class newClass = newArray.getClass();
        if(!newClass.isArray()) {
            throw new IllegalArgumentException();
        }
        if(oldArray == null) {
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

    public static String[] getEnv() throws IOException
    {
        if(cEnv == null) {
            synchronized(Common.class) {
                if(cEnv == null) {
                    Properties props = new Properties();
                    Process proc = null;
                    Runtime runtime = Runtime.getRuntime();
                    String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

                    if (osName.indexOf("windows") >= 0) { //$NON-NLS-1$
                        if (osName.indexOf("windows 9") >= 0) { //$NON-NLS-1$
                            proc = runtime.exec("command.com /c set"); //$NON-NLS-1$
                        }
                        else {
                            proc = runtime.exec("cmd.exe /c set"); //$NON-NLS-1$
                        }
                    }
                    else {
                        proc = runtime.exec("env"); //$NON-NLS-1$
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                                                            proc.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        int n = line.indexOf('=');
                        if(n >= 0) {
                            String key = line.substring(0, n);
                            String value = line.substring(n + 1);
                            props.setProperty(key, value);
                        }
                    }
                    br.close();
                    cEnv = new String[props.size()];
                    int i=0;
                    for(Iterator iter = props.entrySet().iterator(); iter.hasNext(); ) {
                        Map.Entry entry = (Map.Entry)iter.next();
                        cEnv[i++] = new StringBuffer((String)entry.getKey()).append("=").append((String)entry.getValue()).toString(); //$NON-NLS-1$
                    }
                }
            }
        }
        return cEnv;
    }

    public static String[] runProcessWithOutput(String[] cmdArray, File workDir)
    {
        return runProcessWithOutput(cmdArray, workDir, 0);
    }

    public static String[] runProcessWithOutput(String[] cmdArray, File workDir, int validReturnCode)
    {
        String[] output = null;
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray,getEnv(), workDir);
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

    public static String[] tokenize(String text, char separator)
    {
        ArrayList list = new ArrayList();
        if(!Common.isEmpty(text)) {
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
            list.add(buf.toString());
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
        HashMap map = new HashMap();
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
        if(n >= 0) {
            suffix = pathName.substring(n);
            prefix = pathName.substring(0,n);
        }
        if(!Common.isEmpty(prefix)) {
            for(int i=0; i<NSISKeywords.PREDEFINED_PATH_VARIABLES.length; i++) {
                if(NSISKeywords.PREDEFINED_PATH_VARIABLES[i].equalsIgnoreCase(prefix)) {
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
}
