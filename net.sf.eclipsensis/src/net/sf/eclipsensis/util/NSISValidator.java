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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;

public class NSISValidator implements INSISConstants
{

    public static double MINIMUM_NSIS_VERSION = 2.0;
    private static Pattern cVersionPattern = Pattern.compile("[vV]?(\\d+[\\.\\d+]?).*"); //$NON-NLS-1$

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
