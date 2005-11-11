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
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

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

    private static Pattern cValidPathName = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathNameSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidPathSpec = Pattern.compile("([A-Za-z]:)?\\\\?(((\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidNSISPrefixedPathSpecSuffix = Pattern.compile("\\\\(((\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2}+)\\\\)*(\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidFileName = Pattern.compile("(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidURL = Pattern.compile("(?:(?:ftp|https?):\\/\\/)?(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|edu|biz|org|gov|int|info|mil|net|name|museum|coop|aero|[a-z][a-z])\\b(?:\\d+)?(?:\\/[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]*(?:[.,?]+[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]+)*)?"); //$NON-NLS-1$

    private Common()
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
        int oldLength = Array.getLength(array);
        Object newArray;
        if(oldLength == newLength && offset == 0) {
            newArray = array;
        }
        else {
            newArray = Array.newInstance(c.getComponentType(), newLength);
            System.arraycopy(array,offset,newArray,0,Math.min(oldLength - offset, newLength));
        }
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
        return (String[])tokenizeList(text,separator).toArray(EMPTY_STRING_ARRAY);
    }

    public static List tokenizeList(String text, char separator)
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
                    buf.setLength(0);
                }
            }
            list.add(buf.toString().trim());
        }
        return list;
    }

    public static String[] loadArrayProperty(ResourceBundle bundle, String propertyName)
    {
        return (String[])loadListProperty(bundle,propertyName).toArray(EMPTY_STRING_ARRAY);
    }

    public static String[] loadArrayProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        return (String[])loadListProperty(bundle,propertyName, separator).toArray(EMPTY_STRING_ARRAY);
    }

    public static List loadListProperty(ResourceBundle bundle, String propertyName)
    {
        return loadListProperty(bundle, propertyName, ',');
    }

    public static List loadListProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        String property = null;
        if(bundle != null) {
            try {
                property = bundle.getString(propertyName);
            }
            catch(MissingResourceException mre) {
                property = null;
            }
        }
        return tokenizeList(property, separator);
    }

    public static Map loadMapProperty(ResourceBundle bundle, String propertyName)
    {
        return loadMapProperty(bundle,propertyName,',');
    }

    public static Map loadMapProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        Map map = new LinkedHashMap();
        List list = loadListProperty(bundle, propertyName, separator);
        for(Iterator iter=list.iterator(); iter.hasNext(); ) {
            String pair = (String)iter.next();
            int n=pair.indexOf("="); //$NON-NLS-1$
            if(n > 0) {
                String key = pair.substring(0,n).trim();
                String value = null;
                if(n < pair.length() - 1) {
                    value = pair.substring(n+1).trim();
                }
                map.put(key, value);
            }
        }
        return map;
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

    private static boolean isValidPathNameOrSpec(String pathName, Pattern path)
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
                        EclipseNSISPlugin.getDefault().log(e1);
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            EclipseNSISPlugin.getDefault().log(e);
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
                        EclipseNSISPlugin.getDefault().log(e1);
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            EclipseNSISPlugin.getDefault().log(e);
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

    public static String maybeQuote(String text)
    {
        if(shouldQuote(text)) {
            text = quote(text);
        }
        return text;
    }

    public static boolean shouldQuote(String text)
    {
        boolean shouldQuote = false;
        for(int i=0; i<text.length(); i++) {
            if(Character.isWhitespace(text.charAt(i))) {
                shouldQuote = true;
            }
        }
        return shouldQuote;
    }

    public static String quote(String text)
    {
        return new StringBuffer("\"").append(text).append("\"").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String makeNSISRelativeLocation(String pathName)
    {
        if(!Common.isEmpty(pathName)) {
            String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
            if(!Common.isEmpty(nsisHome) && pathName.regionMatches(true,0,nsisHome,0,nsisHome.length())) {
                boolean shouldQuote = shouldQuote(pathName);
                pathName = NSISKeywords.getInstance().getKeyword("${NSISDIR}")+pathName.substring(nsisHome.length()); //$NON-NLS-1$
                if(shouldQuote) {
                    pathName = quote(pathName);
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
            return childPath.toOSString();
        }
        return pathname;
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
                buf.setLength(0);
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

    public static void openError(Shell shell, String message, Image icon)
    {
        openError(shell, EclipseNSISPlugin.getResourceString("error.title"), message, icon); //$NON-NLS-1$
    }

    public static void openWarning(Shell shell, String message, Image icon)
    {
        openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), message, icon); //$NON-NLS-1$
    }

    public static boolean openConfirm(Shell shell, String message, Image icon)
    {
        return openConfirm(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                          message, icon);
    }

    public static boolean openQuestion(Shell shell, String message, Image icon)
    {
        return openQuestion(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                          message, icon);
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
            bytes = (byte[])resizeArray(bytes, n+numread);
            buf.get(bytes,n,numread);
            buf.rewind();
            numread = channel.read(buf);
        }
        return bytes;
    }

    public static String padString(String str, int length)
    {
        if(str != null) {
            if(str.length() < length) {
                char[] c = new char[length-str.length()];
                Arrays.fill(c,' ');
                str += new String(c);
            }
        }
        return str;
    }

    public static void printBundleExtensions(Bundle bundle)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtension[] extensions = registry.getExtensions(bundle.getSymbolicName());
        for (int i = 0; i < extensions.length; i++) {
            System.out.print(extensions[i].getExtensionPointUniqueIdentifier()+" label="); //$NON-NLS-1$
            System.out.print(extensions[i].getLabel()+" simpleId="); //$NON-NLS-1$
            System.out.print(extensions[i].getSimpleIdentifier()+" uniqueId="); //$NON-NLS-1$
            System.out.println(extensions[i].getUniqueIdentifier());
            printChildren("",extensions[i].getConfigurationElements()); //$NON-NLS-1$
        }
    }

    private static void printChildren(String prefix, IConfigurationElement[] elements) {
        if(!Common.isEmptyArray(elements)) {
            prefix += "\t"; //$NON-NLS-1$
            for (int j = 0; j < elements.length; j++) {
                String[] attr = elements[j].getAttributeNames();
                if(!Common.isEmptyArray(attr)) {
                    for (int i = 0; i < attr.length; i++) {
                        System.out.println(prefix+attr[i]+"="+elements[j].getAttribute(attr[i])); //$NON-NLS-1$
                    }
                }
                printChildren(prefix,elements[j].getChildren());
            }
        }
    }

    public static boolean openConfirm(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                message, MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        return dialog.open() == 0;
    }

    public static void openError(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                message, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static void openInformation(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                message, MessageDialog.INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static boolean openQuestion(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
        return dialog.open() == 0;
    }

    public static void openWarning(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                message, MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static String trim(String str)
    {
        if(str != null) {
            return str.trim();
        }
        return null;
    }

    public static String escapeQuotes(String text)
    {
        if(!isEmpty(text)) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] == '"' || chars[i] == '\'' || chars[i] == '`') {
                    buf.append("$\\"); //$NON-NLS-1$
                }
                buf.append(chars[i]);
            }
            text = buf.toString();
        }
        return text;
    }
}
