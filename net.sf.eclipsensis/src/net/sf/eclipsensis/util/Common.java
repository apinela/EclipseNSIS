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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;



/**
 * @author Sunil.Kamath
 */
public class Common
{
    private static String[] cEnv = null;
    public static boolean isEmpty(String string)
    {
        return (string == null || string.trim().length() == 0);
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
}
