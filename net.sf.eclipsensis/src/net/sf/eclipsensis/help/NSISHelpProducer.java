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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

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
                else {
                    return new ByteArrayInputStream(new StringBuffer("<html><body>NSIS documentation not found in").append(
                                                                     nsisDir.getAbsolutePath()).append(
                                                                     " folder.</body></html>").toString().getBytes());
                }
            }
            else {
                return new ByteArrayInputStream(new StringBuffer("<html><head>").append(
                                                    "<script language=\"JavaScript\" src=\"../../../../org.eclipse.help/livehelp.js\"> </script>").append(
                                                    "</head><body><p>Please <a href='javascript:liveAction(").append(
                                                    "\"").append(PLUGIN_NAME).append("\",").append(
                                                    "\"").append(NSISLiveHelpAction.class.getName()).append("\",").append(
                                                    "\"\")'>configure</a> the Eclipse NSIS plugin before accessing help.</p>").append(
                                                    "<p>Click <a href='javascript:history.go(0)'>here</a> to refresh this page.</p>").append(
                                                    "</body></html>").toString().getBytes());
            }
        }
        return null;
    }
}