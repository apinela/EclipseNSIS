/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

public class NSISValidator implements INSISConstants
{
    public static final Version MINIMUM_NSIS_VERSION = new Version(EclipseNSISPlugin.getResourceString("minimum.nsis.version")); //$NON-NLS-1$
    private static final Pattern cVersionPattern = Pattern.compile("v?(\\d+(?:\\.\\d+)?(?:[A-Za-z]+\\d*)?)"); //$NON-NLS-1$
    private static final Pattern cCVSVersionPattern = Pattern.compile("v?([0-3][0-9]-[a-zA-Z]{3}-20[0-9]{2})\\.cvs"); //$NON-NLS-1$
    private static final SimpleDateFormat cCVSDateFormat;
    private static final SimpleDateFormat cCVSEnglishDateFormat;
    public static final String DEFINED_SYMBOLS_PREFIX = "Defined symbols: "; //$NON-NLS-1$
    private static Map cVersionDateMap;

    static {
        TimeZone tz = TimeZone.getTimeZone("GMT"); //$NON-NLS-1$
        cCVSDateFormat = new SimpleDateFormat("dd-MMM-yyyy"); //$NON-NLS-1$
        cCVSDateFormat.setTimeZone(tz);
        if(Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            cCVSEnglishDateFormat = null;
        }
        else {
            cCVSEnglishDateFormat = new SimpleDateFormat("dd-MMM-yyyy",Locale.ENGLISH); //$NON-NLS-1$
            cCVSEnglishDateFormat.setTimeZone(tz);
        }


        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISValidator.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        Map map = Common.loadMapProperty(bundle,"version.dates"); //$NON-NLS-1$
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //$NON-NLS-1$
        sdf.setTimeZone(tz);
        cVersionDateMap = new LinkedHashMap();
        for(Iterator iter=map.keySet().iterator(); iter.hasNext(); ) {
            String key = (String)iter.next();
            String value = (String)map.get(key);
            Version v = new Version(key);
            Date d;
            try {
                d = sdf.parse(value);
            }
            catch (ParseException e) {
                EclipseNSISPlugin.getDefault().log(e);
                d = new Date(0);
            }
            cVersionDateMap.put(v,d);
        }
    }

    private NSISValidator()
    {
    }
    
    public static boolean isCVSVersion(Version v)
    {
        try {
            String version = "v" + v.toString(); //$NON-NLS-1$
            return cCVSVersionPattern.matcher(version).matches();
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return false;
        }
    }

    public static File findNSISExe(File nsisHome)
    {
        if(IOUtility.isValidDirectory(nsisHome)) {
            File file = new File(nsisHome,MAKENSIS_EXE);
            if(IOUtility.isValidFile(file)) {
                Version version = getNSISVersion(file);
                if(version.compareTo(MINIMUM_NSIS_VERSION) >= 0) {
                    return file;
                }
            }
        }
        return null;
    }

    public static Properties loadNSISDefaultSymbols(File nsisEXE)
    {
        Properties props = new Properties();
        String exeName = nsisEXE.getAbsoluteFile().getAbsolutePath();
        String[] output = MakeNSISRunner.runProcessWithOutput(exeName,
	                                                          new String[]{MakeNSISRunner.MAKENSIS_HDRINFO_OPTION},
	                                                          nsisEXE.getParentFile(),1);
        if(!Common.isEmptyArray(output)) {
            for (int i = 0; i < output.length; i++) {
                if(output[i].startsWith(DEFINED_SYMBOLS_PREFIX)) {
                    StringTokenizer st = new StringTokenizer(output[i].substring(DEFINED_SYMBOLS_PREFIX.length()),","); //$NON-NLS-1$
                    while(st.hasMoreTokens()) {
                        String token = st.nextToken();
                        int n = token.indexOf('=');
                        if(n>0 && token.length() > n+1) {
                            props.put(token.substring(0,n).trim(),token.substring(n+1).trim());
                        }
                        else {
                            props.setProperty(token,""); //$NON-NLS-1$
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

    public static Version getNSISVersion(File exeFile)
    {
        Version version = null;
        String exeName = exeFile.getAbsoluteFile().getAbsolutePath();
        String[] output = MakeNSISRunner.runProcessWithOutput(exeName,
                                                              new String[]{MakeNSISRunner.MAKENSIS_VERSION_OPTION},
                                                              exeFile.getParentFile());
        if(!Common.isEmptyArray(output)) {
            for (int i = 0; i < output.length; i++) {
                Matcher matcher = cVersionPattern.matcher(output[i]);
                if(matcher.matches()) {
                    version = new Version(matcher.group(1));
                    break;
                }
                else {
                    matcher = cCVSVersionPattern.matcher(output[i]);
                    if(matcher.matches()) {
                        Date cvsDate;
                        try {
                            cvsDate = cCVSDateFormat.parse(matcher.group(1));
                        }
                        catch (ParseException e) {
                            if(cCVSEnglishDateFormat != null) {
                                try {
                                    cvsDate = cCVSEnglishDateFormat.parse(matcher.group(1));
                                }
                                catch (ParseException e1) {
                                    EclipseNSISPlugin.getDefault().log(e1);
                                    cvsDate = new Date(0);
                                }
                            }
                            else {
                                EclipseNSISPlugin.getDefault().log(e);
                                cvsDate = new Date(0);
                            }
                        }

                        for(Iterator iter=cVersionDateMap.keySet().iterator(); iter.hasNext(); ) {
                            Version v = (Version)iter.next();
                            Date d = (Date)cVersionDateMap.get(v);
                            if(cvsDate.compareTo(d) >= 0) {
                                version = v;
                            }
                            else {
                                break;
                            }
                        }
                        if(version != null) {
                            version = new Version(version);
                            version.setDisplayText(output[i].substring(1));
                        }
                    }
                }
            }
        }

        return (version == null?Version.EMPTY_VERSION:version);
    }

}
