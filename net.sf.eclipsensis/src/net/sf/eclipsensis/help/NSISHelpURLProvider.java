/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveProperties;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISHelpURLProvider implements INSISConstants, IPropertyChangeListener
{
    private NSISPreferences mPreferences = NSISPreferences.getPreferences();
    
    private final String mDocsHelpPrefix;
    private final String mDocsHelpSuffix;
    private final String mCHMHelpPrefix;
    private final String mCHMHelpSuffix;

    private Properties mDocsHelpURLs = new CaseInsensitiveProperties();
    private Properties mCHMHelpURLs = new CaseInsensitiveProperties();
    
    private ResourceBundle mBundle;
    
    private static NSISHelpURLProvider cInstance = null;
    private static IEclipseNSISPluginListener cShutdownListener = new IEclipseNSISPluginListener() {
        public void stopped()
        {
            if(cInstance != null) {
                synchronized(NSISHelpProducer.class) {
                    if(cInstance != null) {
                        cInstance.dispose();
                        cInstance = null;
                    }                    
                }
            }
        }
    };
    
    public static NSISHelpURLProvider getInstance()
    {
        if(cInstance == null) {
            synchronized(NSISHelpURLProvider.class) {
                if(cInstance == null) {
                    cInstance = new NSISHelpURLProvider();
                    EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
                }                
            }
        }
        return cInstance;
    }
    
    private NSISHelpURLProvider()
    {
        mDocsHelpPrefix = EclipseNSISPlugin.getResourceString("docs.help.prefix","Chapter"); //$NON-NLS-1$ //$NON-NLS-2$
        mDocsHelpSuffix = EclipseNSISPlugin.getResourceString("docs.help.suffix","html"); //$NON-NLS-1$ //$NON-NLS-2$
        mCHMHelpPrefix = EclipseNSISPlugin.getResourceString("chm.help.prefix","Section"); //$NON-NLS-1$ //$NON-NLS-2$
        mCHMHelpSuffix = EclipseNSISPlugin.getResourceString("chm.help.suffix","html"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            mBundle = ResourceBundle.getBundle(NSISHelpURLProvider.class.getName());
        } 
        catch (MissingResourceException x) {
            mBundle = null;
        }
        loadDocsHelpURLs();
        loadCHMHelpURLs();
        mPreferences.getPreferenceStore().addPropertyChangeListener(this);
    }
    
    public void dispose()
    {
        mPreferences.getPreferenceStore().removePropertyChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
            loadCHMHelpURLs();
        }
    }

    private void loadDocsHelpURLs()
    {
        mDocsHelpURLs.clear();
        if(mBundle != null) {
            for(Enumeration enum=mBundle.getKeys(); enum.hasMoreElements();) {
                String key = (String)enum.nextElement();
                String[] ids = Common.loadArrayProperty(mBundle,key);
                if(!Common.isEmptyArray(ids) && ids.length >= 3) {
                    StringBuffer buf = new StringBuffer("/").append(PLUGIN_NAME).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
                                                    NSIS_REFERENCE_DOCS_PREFIX).append(
                                                    mDocsHelpPrefix).append(ids[0]).append(".").append( //$NON-NLS-1$
                                                    mDocsHelpSuffix).append('#').append(ids[0]);
                    for (int i = 1; i < ids.length; i++) {
                        buf.append(".").append(ids[i]); //$NON-NLS-1$
                    }
                    mDocsHelpURLs.setProperty(key, buf.toString());
                }
            }
        }
    }
    
    private void loadCHMHelpURLs()
    {
        mCHMHelpURLs.clear();
        if(mBundle != null) {
            String home = mPreferences.getNSISHome();
            if(!Common.isEmpty(home)) {
                for(Enumeration enum=mBundle.getKeys(); enum.hasMoreElements();) {
                    String key = (String)enum.nextElement();
                    String[] ids = Common.loadArrayProperty(mBundle,key);
                    if(!Common.isEmptyArray(ids) && ids.length >= 3) {
                        StringBuffer buf = new StringBuffer("mk:@MSITStore:").append( //$NON-NLS-1$
                                                home);
                        if(!home.endsWith("\\")) { //$NON-NLS-1$
                            buf.append("\\"); //$NON-NLS-1$
                        }
                        buf.append(NSIS_REFERENCE_CHM_LOCATION).append("::/").append(mCHMHelpPrefix).append( //$NON-NLS-1$
                                  ids[0]).append(".").append(ids[1]).append(".").append( //$NON-NLS-1$ //$NON-NLS-2$
                                  mCHMHelpSuffix).append("#").append(ids[0]); //$NON-NLS-1$
                        for (int i = 1; i < ids.length; i++) {
                            buf.append(".").append(ids[i]); //$NON-NLS-1$
                        }
                        mCHMHelpURLs.setProperty(key, buf.toString());
                    }
                }
            }
        }
    }
    
    private String getHelpURL(String keyWord, boolean useDocHelp)
    {
        if(!Common.isEmpty(keyWord)) {
            if(useDocHelp) {
                return mDocsHelpURLs.getProperty(keyWord);
            }
            else {
                return mCHMHelpURLs.getProperty(keyWord);
            }
        }
        else {
            return null;
        }
    }
    
    public void showHelpURL(String keyword)
    {
        String url = null;
        if(mPreferences.isUseDocsHelp()) {
            url = getHelpURL(keyword, true);
        }
        if(Common.isEmpty(url)) {
            url = getHelpURL(keyword, false);
            if(!Common.isEmpty(url)) {
                WinAPI.HtmlHelp(WinAPI.GetDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
            }
        }
        else {
            WorkbenchHelp.displayHelpResource(url);
        }
    }
}
