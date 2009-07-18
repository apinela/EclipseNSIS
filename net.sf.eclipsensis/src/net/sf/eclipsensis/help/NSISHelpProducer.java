/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
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
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.swt.program.Program;

public class NSISHelpProducer implements IExecutableExtension, IHelpContentProducer, INSISConstants
{
    public static final String STYLE = EclipseNSISPlugin.getResourceString("help.style",""); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String CONFIGURE = "configure"; //$NON-NLS-1$

    private static final String NSIS_CONTRIB_PATH="help/NSIS/$CONTRIB$"; //$NON-NLS-1$
    private static final byte[] NSIS_CONTRIB_JS=new StringBuffer("<!--").append(LINE_SEPARATOR).append( //$NON-NLS-1$
        "var nsisContribPath=\"/").append(PLUGIN_ID).append("/").append(NSIS_CONTRIB_PATH).append( //$NON-NLS-1$ //$NON-NLS-2$
        "\";").append(LINE_SEPARATOR).append("//-->").append(LINE_SEPARATOR).toString().getBytes(); //$NON-NLS-1$ //$NON-NLS-2$

    private static final byte[] GO_BACK = "<html><head><script language=\"javascript\">\n<!--\nhistory.go(-1);\n//-->\n</script></head></html>".getBytes(); //$NON-NLS-1$
    private static final File cHelpCacheLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),PLUGIN_HELP_LOCATION_PREFIX);
    private String mPluginId = PLUGIN_ID;
    private boolean mJavascriptOnly = false;
    private String[] mHelpURLPrefixes = {PLUGIN_ID, NSIS_PLATFORM_HELP_PREFIX};
    private MessageFormat mLocationReplaceFormat = new MessageFormat("<html><head><script language=\"javascript\">\n<!--\nif(window.location.replace) '{'window.location.replace(\"{0}\");'}' else '{'window.location.href=\"{0}\";'}'\n//-->\n</script></head><body></body></html>"); //$NON-NLS-1$

    private String stripPrefixes(String href)
    {
        for (int i = 0; i < mHelpURLPrefixes.length; i++) {
            if(href.charAt(0) == '/') {
                href = href.substring(1);
            }
            if(href.startsWith(mHelpURLPrefixes[i])) {
                href = href.substring(mHelpURLPrefixes[i].length());
            }
        }
        return href;
    }

    /* (non-Javadoc)
     * @see org.eclipse.help.IHelpContentProducer#getInputStream(java.lang.String, java.lang.String, java.util.Locale)
     */
    public InputStream getInputStream(String pluginID, String href, Locale locale)
    {
        if(pluginID.equals(mPluginId)) {
            if(href.equals(NSISCONTRIB_JS_LOCATION)) {
                return new ByteArrayInputStream(NSIS_CONTRIB_JS);
            }
            else if(href.startsWith(NSIS_PLATFORM_HELP_PREFIX) && !mJavascriptOnly) {
                if(href.startsWith(NSIS_CONTRIB_PATH)) {
                    String nsisContribPath = NSISHelpURLProvider.getInstance().getNSISContribPath();
                    if(nsisContribPath == null) {
                        nsisContribPath = NSIS_PLATFORM_HELP_PREFIX+"Contrib"; //$NON-NLS-1$
                    }
                    href = Common.replaceAll(href, NSIS_CONTRIB_PATH, nsisContribPath, true);
                }
                String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
                if(!Common.isEmpty(nsisHome)) {
                    File nsisDir = new File(nsisHome);
                    if(IOUtility.isValidDirectory(nsisDir)) {
                        File helpFile = null;
                        String href2=href.substring(NSIS_PLATFORM_HELP_PREFIX.length());
                        boolean isKeyword = href2.startsWith(KEYWORD_PREFIX);
                        if(isKeyword) {
                            String keyword = href2.substring(KEYWORD_PREFIX.length());
                            String url = NSISHelpURLProvider.getInstance().getHelpURL(keyword, true);
                            if(url != null) {
                                href2 = stripPrefixes(url);
                            }
                        }
                        String target = null;
                        int n = href2.lastIndexOf('#');
                        if(n > 0) {
                            target = href2.substring(n);
                            href2 = href2.substring(0,n);
                        }
                        boolean isDocs = href2.startsWith(DOCS_LOCATION_PREFIX);
                        boolean isContrib = href2.startsWith(CONTRIB_LOCATION_PREFIX);
                        if(isDocs || isContrib) {
                            if(IOUtility.isValidDirectory(cHelpCacheLocation)) {
                                helpFile = new File(cHelpCacheLocation,href2);
                            }
                            if(!IOUtility.isValidFile(helpFile)) {
                                helpFile = new File(nsisDir,href2);
                            }
                        }
                        else {
                            helpFile = new File(nsisDir,href2);
                        }
                        if(IOUtility.isValidFile(helpFile)) {
                            if(isKeyword) {
                                href2 = IOUtility.getFileURLString(helpFile);
                                if(target != null) {
                                    href2 += target;
                                }
                                String content = mLocationReplaceFormat.format(new String[] {href2});
                                return new ByteArrayInputStream(content.getBytes());
                            }
                            if(HelpBrowserLocalFileHandler.INSTANCE.handle(helpFile)) {
                                return new ByteArrayInputStream(GO_BACK);
                            }
                            else {
                                try {
                                    return new BufferedInputStream(new FileInputStream(helpFile));
                                }
                                catch (FileNotFoundException e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                }
                            }
                        }
                        else if(helpFile != null && IOUtility.isValidDirectory(helpFile)) {
                            try {
                                Program.launch(helpFile.getCanonicalPath());
                                return new ByteArrayInputStream(GO_BACK);
                            }
                            catch (IOException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                        if(isDocs || isContrib) {
                            if(isDocs && !NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
                                try {
                                    return new BufferedInputStream(new FileInputStream(NSISHelpURLProvider.getInstance().getNoHelpFile()));
                                }
                                catch (FileNotFoundException e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                }
                            }
                            return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("missing.help.format", //$NON-NLS-1$
                                                                    new Object[]{STYLE, href,PLUGIN_ID,
                                                                                 NSISLiveHelpAction.class.getName()}).getBytes());
                        }
                        else {
                            return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("missing.file.format", //$NON-NLS-1$
                                                                                new Object[]{STYLE,href}).getBytes());
                        }
                    }
                }
                return new ByteArrayInputStream(EclipseNSISPlugin.getFormattedString("unconfigured.help.format", //$NON-NLS-1$
                                                                    new Object[]{STYLE,PLUGIN_ID,NSISLiveHelpAction.class.getName(),CONFIGURE}).getBytes());
            }
        }
        return null;
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
    {
        if(data instanceof Map<?,?>) {
            Map<?,?> map = (Map<?,?>)data;
            if(map.containsKey("pluginId")) { //$NON-NLS-1$
                mPluginId = (String)map.get("pluginId"); //$NON-NLS-1$
            }
            else {
                mPluginId = PLUGIN_ID;
            }
            if(map.containsKey("javascriptOnly")) { //$NON-NLS-1$
                mJavascriptOnly = Boolean.valueOf((String)map.get("javascriptOnly")).booleanValue(); //$NON-NLS-1$
            }
        }
    }
}