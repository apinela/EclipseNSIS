/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.*;
import java.util.Locale;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpContentProducer;

public class NSISHelpProducer implements IExecutableExtension, IHelpContentProducer, INSISConstants
{
    private static final File cHelpCacheLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),PLUGIN_HELP_LOCATION_PREFIX);
    private String mPluginId = PLUGIN_ID;
    private boolean mJavascriptOnly = false;

    /* (non-Javadoc)
     * @see org.eclipse.help.IHelpContentProducer#getInputStream(java.lang.String, java.lang.String, java.util.Locale)
     */
    public InputStream getInputStream(String pluginID, String href, Locale locale)
    {
        if(pluginID.equals(mPluginId)) {
            if(href.equals(NSISCONTRIB_JS_LOCATION)) {
                String nsisContribPath = NSISHelpURLProvider.INSTANCE.getNSISContribPath();
                StringBuffer buf = new StringBuffer("<!--").append(LINE_SEPARATOR);
                buf.append("var nsisContribPath=");
                if(nsisContribPath == null) {
                    buf.append("null");
                }
                else {
                    buf.append("\"").append(nsisContribPath).append("\"");
                }
                buf.append(";").append(LINE_SEPARATOR);
                buf.append("//-->").append(LINE_SEPARATOR);
                return new ByteArrayInputStream(buf.toString().getBytes());
            }
            else if(href.startsWith(NSIS_HELP_PREFIX) && !mJavascriptOnly) {
                String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
                if(!Common.isEmpty(nsisHome)) {
                    File nsisDir = new File(nsisHome);
                    if(nsisDir.exists() && nsisDir.isDirectory()) {
                        File helpFile = null;
                        String href2=href.substring(NSIS_HELP_PREFIX.length());
                        boolean isDocs = false;
                        if(href2.startsWith(DOCS_LOCATION_PREFIX) || href2.startsWith(CONTRIB_LOCATION_PREFIX)) {
                            isDocs = true;
                            if(cHelpCacheLocation.exists() && cHelpCacheLocation.isDirectory()) {
                                helpFile = new File(cHelpCacheLocation,href2);
                            }
                            if(!helpFile.exists() || !helpFile.isFile()) {
                                helpFile = new File(nsisDir,href2);
                            }
                        }
                        else {
                            helpFile = new File(nsisDir,href2);
                        }
                        if(helpFile != null && helpFile.exists() && helpFile.isFile()) {
                            try {
                                return new BufferedInputStream(new FileInputStream(helpFile));
                            }
                            catch (FileNotFoundException e) {
                            }
                        }
                        if(isDocs) {
                            return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("missing.help.format", //$NON-NLS-1$
                                                                    new Object[]{href,PLUGIN_ID,
                                                                                 NSISLiveHelpAction.class.getName()}).getBytes());
                        }
                        else {
                            return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("missing.file.format", //$NON-NLS-1$
                                                                                new Object[]{href}).getBytes());
                        }
                    }
                }
                return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("unconfigured.help.format", //$NON-NLS-1$
                                                                    new Object[]{PLUGIN_ID,NSISLiveHelpAction.class.getName()}).getBytes());
            }
        }
        return null;
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        if(data instanceof Map) {
            Map map = (Map)data;
            if(map.containsKey("pluginId")) {
                mPluginId = (String)map.get("pluginId");
            }
            else {
                mPluginId = PLUGIN_ID;
            }
            if(map.containsKey("javascriptOnly")) {
                mJavascriptOnly = Boolean.valueOf((String)map.get("javascriptOnly")).booleanValue();
            }
        }
    }
}