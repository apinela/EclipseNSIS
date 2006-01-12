/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.util.IOUtility;

public class NSISUpdateURLs
{
    private static final String DOWNLOAD_URL = "download.url"; //$NON-NLS-1$
    private static final String UPDATE_URL = "update.url"; //$NON-NLS-1$
    
    private static final MessageFormat cDefaultUpdateURLFormat;
    private static final MessageFormat cDefaultDownloadURLFormat;

    private static MessageFormat cCustomUpdateURLFormat = null;
    private static MessageFormat cCustomDownloadURLFormat = null;
    
    private static final File cCustomBundleFile;
    private static long cCustomBundleFileSize = 0;
    private static long cCustomBundleFileTimestamp = 0;

    static {
        String className = NSISUpdateURLs.class.getName();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(className);
        } catch (MissingResourceException x) {
            bundle = null;
        }
        cDefaultUpdateURLFormat = readBundle(bundle, UPDATE_URL);
        cDefaultDownloadURLFormat = readBundle(bundle, DOWNLOAD_URL);
        className = className.substring(NSISUpdateURLs.class.getPackage().getName().length()+1);
        cCustomBundleFile = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),className+".properties"); //$NON-NLS-1$
    }
    
    private NSISUpdateURLs()
    {
    }
    
    private static MessageFormat readBundle(ResourceBundle bundle, String key)
    {
        MessageFormat format = null;
        if(bundle != null) {
            try {
                format = new MessageFormat(bundle.getString(key));
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
                format = null;
            }            
        }
        return format;
    }
    
    private static void checkCustomBundle()
    {
        if(IOUtility.isValidFile(cCustomBundleFile)) {
            FileInputStream fis = null;
            try {
                long size = cCustomBundleFile.length();
                long timestamp = cCustomBundleFile.lastModified();
                if(size != cCustomBundleFileSize || timestamp != cCustomBundleFileTimestamp) {
                    fis = new FileInputStream(cCustomBundleFile);
                    PropertyResourceBundle bundle = new PropertyResourceBundle(fis);
                    cCustomUpdateURLFormat = readBundle(bundle, UPDATE_URL);
                    cCustomDownloadURLFormat = readBundle(bundle, DOWNLOAD_URL);
                    cCustomBundleFileSize = size;
                    cCustomBundleFileTimestamp = timestamp;
                }
            }
            catch (Exception e) {
                cCustomUpdateURLFormat = null;
                cCustomDownloadURLFormat = null;
                cCustomBundleFileSize = 0;
                cCustomBundleFileTimestamp = 0;
            }
            finally {
                IOUtility.closeIO(fis);
            }
        }
        else {
            cCustomUpdateURLFormat = null;
            cCustomDownloadURLFormat = null;
            cCustomBundleFileSize = 0;
            cCustomBundleFileTimestamp = 0;
        }
    }

    public static synchronized URL getUpdateURL(String version) throws IOException
    {
        checkCustomBundle();
        return new URL((cCustomUpdateURLFormat==null?cDefaultUpdateURLFormat:cCustomUpdateURLFormat).format(new String[] {version}));
    }
    
    public static synchronized URL getDownloadURL(String version) throws IOException
    {
        checkCustomBundle();
        return new URL((cCustomDownloadURLFormat==null?cDefaultDownloadURLFormat:cCustomDownloadURLFormat).format(new String[] {version}));
    }
}
