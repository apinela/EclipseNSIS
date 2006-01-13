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

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;

public class NSISUpdateURLs
{
    private static final String cDefaultDownloadSite;
    private static final String cDefaultUpdateSite;
    private static final MessageFormat cUpdateURLFormat;
    private static final MessageFormat cDownloadURLFormat;

    static {
        String className = NSISUpdateURLs.class.getName();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(className);
        } catch (MissingResourceException x) {
            bundle = null;
        }
        
        cDefaultUpdateSite = readBundle(bundle, "default.update.site"); //$NON-NLS-1$
        cDefaultDownloadSite = readBundle(bundle, "default.download.site"); //$NON-NLS-1$
        cUpdateURLFormat = readBundleFormat(bundle, "update.url.format"); //$NON-NLS-1$
        cDownloadURLFormat = readBundleFormat(bundle, "download.url.format"); //$NON-NLS-1$
    }
    
    private NSISUpdateURLs()
    {
    }
    
    private static String readBundle(ResourceBundle bundle, String key)
    {
        String string = null;
        if(bundle != null) {
            try {
                string = bundle.getString(key);
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
                string = null;
            }            
        }
        return string;
    }
    
    private static MessageFormat readBundleFormat(ResourceBundle bundle, String key)
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
    
    public static String getDefaultDownloadSite()
    {
        return cDefaultDownloadSite;
    }

    public static String getDefaultUpdateSite()
    {
        return cDefaultUpdateSite;
    }

    public static synchronized URL getUpdateURL(String site, String version) throws IOException
    {
        return new URL(cUpdateURLFormat.format(new String[] {site, version}));
    }
    
    public static synchronized URL getDownloadURL(String site, String version) throws IOException
    {
        return new URL(cDownloadURLFormat.format(new String[] {site, version}));
    }

    public static synchronized URL getUpdateURL(String version) throws IOException
    {
        return getUpdateURL(cDefaultUpdateSite, version);
    }
    
    public static synchronized URL getDownloadURL(String version) throws IOException
    {
        return getDownloadURL(cDefaultDownloadSite, version);
    }
}
