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
import java.text.MessageFormat;
import java.util.Locale;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.help.IHelpContentProducer;

public class NSISHelpProducer implements IHelpContentProducer, INSISConstants
{
    private static final File cHelpCacheLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),PLUGIN_HELP_LOCATION_PREFIX);
    private NSISHelpURLProvider mHelpURLProvider = NSISHelpURLProvider.getInstance();
    
    /* (non-Javadoc)
     * @see org.eclipse.help.IHelpContentProducer#getInputStream(java.lang.String, java.lang.String, java.util.Locale)
     */
    public InputStream getInputStream(String pluginID, String href, Locale locale)
    {
        if(pluginID.equals(PLUGIN_NAME) && href.startsWith(NSIS_HELP_PREFIX)) {
            String nsisHome = NSISPreferences.getPreferences().getNSISHome();
            if(!Common.isEmpty(nsisHome)) {
                File nsisDir = new File(nsisHome);
                if(nsisDir.exists() && nsisDir.isDirectory()) {
                    File helpFile = null;
                    String href2=href.substring(NSIS_HELP_PREFIX.length());
                    boolean isDocs = false;
                    if(href2.startsWith(DOCS_LOCATION_PREFIX)) {
                        isDocs = true;
                        if(cHelpCacheLocation.exists() && cHelpCacheLocation.isDirectory()) {
                            helpFile = new File(cHelpCacheLocation,href2);
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
                        return new ByteArrayInputStream(MessageFormat.format(EclipseNSISPlugin.getResourceString("missing.help.format"), //$NON-NLS-1$
                                new Object[]{href,PLUGIN_NAME,
                                             NSISLiveHelpAction.class.getName()}).getBytes());
                    }
                    else {
                        return new ByteArrayInputStream(MessageFormat.format(EclipseNSISPlugin.getResourceString("missing.file.format"), //$NON-NLS-1$
                                new Object[]{href}).getBytes());
                    }
                }
            }
            return new ByteArrayInputStream(MessageFormat.format(EclipseNSISPlugin.getResourceString("unconfigured.help.format"), //$NON-NLS-1$
                    new Object[]{PLUGIN_NAME,NSISLiveHelpAction.class.getName()}).getBytes());
        }
        return null;
    }
}