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
    /* (non-Javadoc)
     * @see org.eclipse.help.IHelpContentProducer#getInputStream(java.lang.String, java.lang.String, java.util.Locale)
     */
    public InputStream getInputStream(String pluginID, String href,
            Locale locale)
    {
        if(pluginID.equals(PLUGIN_NAME) && href.startsWith(NSIS_REFERENCE_PREFIX)) {
            href=href.substring(NSIS_REFERENCE_PREFIX.length());
            String nsisHome = NSISPreferences.getPreferences().getNSISHome();
            if(!Common.isEmpty(nsisHome)) {
                File nsisDir = new File(nsisHome);
                if(nsisDir.exists() && nsisDir.isDirectory()) {
                    File helpFile = new File(nsisDir,href);
                    if(helpFile.exists() && helpFile.isFile()) {
                        try {
                            return new BufferedInputStream(new FileInputStream(helpFile));
                        }
                        catch (FileNotFoundException e) {
                        }
                    }
                }
                return new ByteArrayInputStream(MessageFormat.format(EclipseNSISPlugin.getResourceString("missing.docs.help.format"), //$NON-NLS-1$
                                                new Object[]{nsisDir.getAbsolutePath()}).getBytes());
            }
            else {
                return new ByteArrayInputStream(MessageFormat.format(EclipseNSISPlugin.getResourceString("unconfigured.docs.help.format"), //$NON-NLS-1$
                                                new Object[]{PLUGIN_NAME,NSISLiveHelpAction.class.getName()}).getBytes());
            }
        }
        return null;
    }
}