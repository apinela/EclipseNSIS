/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class NSISBrowserInformationProvider extends NSISInformationProvider
{
    public static final File COLORS_CSS_FILE;
    
    private static RGB cBrowserHelpBackground = null;
    private static RGB cBrowserHelpForeground = null;

    static {
        File f = null;
        try {
            f = new File(new File(EclipseNSISPlugin.getPluginStateLocation(),"hoverhelp"),"colors.css");
        } catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            f = null;
        }
        
        COLORS_CSS_FILE = f;
    }
    
    private static void updateColorStyles()
    {
        if(Display.getCurrent() != null && COLORS_CSS_FILE != null) {
            try {
                Display d = Display.getCurrent();
                RGB fg = d.getSystemColor(SWT.COLOR_INFO_FOREGROUND).getRGB();
                RGB bg = d.getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();
                if(!fg.equals(cBrowserHelpForeground) || !bg.equals(cBrowserHelpBackground)) {
                    StringBuffer buf = new StringBuffer("body { text: #");
                    buf.append(ColorManager.rgbToHex(fg)).append("; background-color: #").append(
                    ColorManager.rgbToHex(bg)).append("}\n");

                    RGB bg2 = new RGB(Math.max(0,bg.red-8),Math.max(0,bg.green-8),Math.max(0,bg.blue-8));
                    buf.append("pre { background-color: #").append(
                            ColorManager.rgbToHex(bg2)).append("}\n");
                    IOUtility.writeContentToFile(COLORS_CSS_FILE, buf.toString().getBytes());
                    
                    cBrowserHelpBackground = bg;
                    cBrowserHelpForeground = fg;
                }
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }
    
    protected Object getInformation(String word)
    {
        String info = NSISHelpURLProvider.getInstance().getKeywordHelp(word);
        if(info == null) {
            StringBuffer buf = new StringBuffer(NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX);
            Object obj = super.getInformation(word);
            if(obj != null) {
                buf.append(obj);
            }
            buf.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_SUFFIX);
            info = buf.toString();
        }
        else {
            updateColorStyles();
        }
        return info;
    }
}