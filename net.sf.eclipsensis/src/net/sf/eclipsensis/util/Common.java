/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;



public class Common
{
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    private static String[] cEnv = null;
    
    public static boolean isEmpty(String string)
    {
        return (string == null || string.trim().length() == 0);
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
            StringBuffer buf = new StringBuffer("");
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
            StringBuffer buf = new StringBuffer("");
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
            String property = bundle.getString(propertyName);
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
}
