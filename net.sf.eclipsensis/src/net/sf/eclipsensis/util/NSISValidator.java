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

import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;

public class NSISValidator implements INSISConstants
{
    public static double MINIMUM_NSIS_VERSION = 2.0;
    private static Pattern cVersionPattern = Pattern.compile("[vV]?(\\d+[\\.\\d+]?).*"); //$NON-NLS-1$
    public static final String DEFINED_SYMBOLS_PREFIX = "Defined symbols: ";

    public static File findNSISExe(File nsisHome)
    {
        if(nsisHome != null) {
            if(nsisHome.exists() && nsisHome.isDirectory()) {
                File file = new File(nsisHome,MAKENSIS_EXE);
                if(file.exists() && file.isFile()) {
                    double version = getVersion(file);
                    if(version >= MINIMUM_NSIS_VERSION) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    public static Properties loadNSISOptions(File nsisEXE)
    {
        Properties props = new Properties();
        String exeName = nsisEXE.getAbsoluteFile().getAbsolutePath();
        String[] output = Common.runProcessWithOutput(new String[]{exeName,"/HDRINFO"}, //$NON-NLS-1$
                                                     nsisEXE.getParentFile(),1);
        if(!Common.isEmptyArray(output)) {
            for (int i = 0; i < output.length; i++) {
                if(output[i].startsWith(DEFINED_SYMBOLS_PREFIX)) {
                    StringTokenizer st = new StringTokenizer(output[i].substring(DEFINED_SYMBOLS_PREFIX.length()),",");
                    while(st.hasMoreTokens()) {
                        String token = st.nextToken();
                        int n = token.indexOf('=');
                        if(n>0 && token.length() > n+1) {
                            props.put(token.substring(0,n).trim(),token.substring(n+1).trim());
                        }
                        else {
                            props.setProperty(token,"");
                        }
                    }
                }
            }
        }
        return props;
    }

    public static boolean validateNSISHome(String nsisHome)
    {
        if(!Common.isEmpty(nsisHome)) {
            return (findNSISExe(new File(nsisHome)) != null);
        }
        return false;
    }

    private static double getVersion(File exeFile)
    {
        double version = 0;
        String exeName = exeFile.getAbsoluteFile().getAbsolutePath();
        String[] output = Common.runProcessWithOutput(new String[]{exeName,"/VERSION"}, //$NON-NLS-1$
                                               exeFile.getParentFile());
        if(!Common.isEmptyArray(output)) {
            for (int i = 0; i < output.length; i++) {
                Matcher matcher = cVersionPattern.matcher(output[i]);
                if(matcher.matches()) {
                    try {
                        double temp = Double.parseDouble(matcher.group(1));
                        version = temp;
                        break;
                    }
                    catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        
        return version;
    }

}
