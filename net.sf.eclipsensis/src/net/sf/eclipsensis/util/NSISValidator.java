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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Sunil.Kamath
 */
public class NSISValidator
{

    public static double MINIMUM_NSIS_VERSION = 2.0;
    private static Pattern cVersionPattern = Pattern.compile("[vV]?(\\d+[\\.\\d+]?).*"); //$NON-NLS-1$

    public static File findNSISExe(File nsisHome)
    {
        if(nsisHome != null) {
            if(nsisHome.exists() && nsisHome.isDirectory()) {
                File file = new File(nsisHome,"makensis.exe"); //$NON-NLS-1$
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
        try {
            final Process proc = Runtime.getRuntime().exec(new String[]{exeName,"/VERSION"},Common.getEnv(), //$NON-NLS-1$
                                                                        exeFile.getParentFile());
            new Thread(new Runnable() {
                public void run()
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    try {
                        String line = br.readLine();
                        while(line != null) {
                            line = br.readLine();
                        }
                    }
                    catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                    finally {
                        try {
                            br.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            try {
                String line = br.readLine();
                while(line != null) {
                    Matcher matcher = cVersionPattern.matcher(line);
                    if(matcher.matches()) {
                        try {
                            double temp = Double.parseDouble(matcher.group(1));
                            version = temp;
                        }
                        catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    line = br.readLine();
                }
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
                version = 0;
            }
            finally {
                try {
                    br.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int rv = proc.waitFor();
            if(rv != 0) {
                version = 0;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            version = 0;
        }
        
        return version;
    }

}
