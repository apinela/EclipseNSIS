/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontUtility
{
    private static final String UNKNOWN_FONT = "??";
    private static final String DEFAULT_DEFAULT_FONT_NAME = "MS Shell Dlg"; //$NON-NLS-1$
    public static final int DEFAULT_DEFAULT_FONT_SIZE = 8;
    public static final String DEFAULT_FONT_NAME;
    public static final int DEFAULT_FONT_SIZE;

    private static Font cDefaultDefaultFont;
    private static Font cInstallOptionsFont = null;
    private static HashMap cFontMap = new HashMap();
    private static NSISSettings cNSISSettings = null;
    private static File cPropertiesFile = null;
    private static INSISConsole cNSISConsole = null;

    static {
        String fontNameKey;
        String fontSizeKey;
        if(EclipseNSISPlugin.getDefault().isWin2K()) {
            fontNameKey = "default.font.name.win2k"; //$NON-NLS-1$
            fontSizeKey = "default.font.size.win2k"; //$NON-NLS-1$
        }
        else {
            fontNameKey = "default.font.name"; //$NON-NLS-1$
            fontSizeKey = "default.font.size"; //$NON-NLS-1$
        }
        DEFAULT_FONT_NAME = InstallOptionsPlugin.getResourceString(fontNameKey,DEFAULT_DEFAULT_FONT_NAME);
        int fontSize;
        try {
            fontSize = Integer.parseInt(InstallOptionsPlugin.getResourceString(fontSizeKey, Integer.toString(DEFAULT_DEFAULT_FONT_SIZE)));
        }
        catch (NumberFormatException e) {
            fontSize = DEFAULT_DEFAULT_FONT_SIZE;
        }
        DEFAULT_FONT_SIZE = fontSize;

        final Font[] f = new Font[1];
        final FontDescriptor fd = FontDescriptor.createFrom(DEFAULT_FONT_NAME,DEFAULT_FONT_SIZE,SWT.NORMAL);
        Runnable r = new Runnable() {
            public void run()
            {
                try {
                    f[0] = fd.createFont(Display.getDefault());
                }
                catch (DeviceResourceException e) {
                    InstallOptionsPlugin.getDefault().log(e);
                    f[0] = Display.getDefault().getSystemFont();
                }
            }
        };

        if(Display.getCurrent() != null) {
            r.run();
        }
        else {
            Display.getDefault().syncExec(r);
        }
        cDefaultDefaultFont = f[0];
        cInstallOptionsFont = getFont(NSISLanguageManager.getInstance().getDefaultLanguage());
        Display.getDefault().disposeExec(new Runnable() {
            public void run()
            {
                if(cDefaultDefaultFont != null && !cDefaultDefaultFont.isDisposed()) {
                    cDefaultDefaultFont.dispose();
                }
                for(Iterator iter = cFontMap.values().iterator(); iter.hasNext(); ) {
                    Font font = (Font)iter.next();
                    if(font != null && !font.isDisposed()) {
                        font.dispose();
                    }
                }
            }
        });
    }

    private FontUtility()
    {
    }

    public static Font getInstallOptionsFont()
    {
        return cInstallOptionsFont;
    }

    public static Font getFont(NSISLanguage lang)
    {
        Font font = (Font)cFontMap.get(lang);
        if(font == null) {
            font = createFont(lang);
            cFontMap.put(lang, font);
        }
        return font;
    }

    private synchronized static Font createFont(NSISLanguage lang)
    {
        Font font = null;
        try {
            if(cNSISSettings == null) {
                cNSISSettings = new DummyNSISSettings();
            }
            if(cPropertiesFile == null) {
                cPropertiesFile = File.createTempFile("font",".properties");//$NON-NLS-1$
                cPropertiesFile.deleteOnExit();
            }
            if(cNSISConsole == null) {
                cNSISConsole = new NullNSISConsole();
            }
            LinkedHashMap symbols = cNSISSettings.getSymbols();

            symbols.put("LANGUAGE",lang.getName()); //$NON-NLS-1$
            symbols.put("PROPERTIES_FILE",cPropertiesFile.getAbsolutePath()); //$NON-NLS-1$
            cNSISSettings.setSymbols(symbols);
            File fontScript = IOUtility.ensureLatest(InstallOptionsPlugin.getDefault().getBundle(),
                    new Path("/font/getfont.nsi"),  //$NON-NLS-1$
                    InstallOptionsPlugin.getPluginStateLocation());
            long timestamp = System.currentTimeMillis();
            MakeNSISResults results = MakeNSISRunner.compile(fontScript, cNSISSettings, cNSISConsole, new INSISConsoleLineProcessor() {
                public NSISConsoleLine processText(String text)
                {
                    return NSISConsoleLine.info(text);
                }

                public void reset()
                {
                }
            },false);


            if(results != null) {
                if (results.getReturnCode() == 0) {
                    File outfile = new File(InstallOptionsPlugin.getPluginStateLocation(),"getfont.exe");
                    if (IOUtility.isValidFile(outfile) && outfile.lastModified() > timestamp) {
                        MakeNSISRunner.testInstaller(outfile.getAbsolutePath(), null, true);
                        if(cPropertiesFile.exists()) {
                            Properties props = new Properties();
                            FileInputStream is = null;
                            try {
                                is = new FileInputStream(cPropertiesFile);
                                props.load(is);
                                String fontName = props.getProperty("name");
                                if(fontName == null) {
                                    fontName = DEFAULT_FONT_NAME;
                                }
                                else if(fontName.equals(UNKNOWN_FONT)) {
                                    fontName = InstallOptionsPlugin.getResourceString("unknown.font."+lang.getName().toLowerCase(),DEFAULT_FONT_NAME);
                                }
                                int fontSize;
                                String tmpFontSize = props.getProperty("size");
                                if(tmpFontSize == null) {
                                    fontSize = DEFAULT_FONT_SIZE;
                                }
                                else {
                                    try {
                                        fontSize = Integer.parseInt(tmpFontSize);
                                    }
                                    catch (NumberFormatException e) {
                                        fontSize = DEFAULT_FONT_SIZE;
                                    }
                                }
                                final FontData fd = new FontData(fontName, fontSize, SWT.NORMAL);
                                final Font[] f = {null};
                                Runnable r = new Runnable() {
                                    public void run()
                                    {
                                        f[0] = new Font(Display.getDefault(),fd);
                                    }
                                };
                                if(Display.getCurrent() != null) {
                                    r.run();
                                }
                                else {
                                    Display.getDefault().syncExec(r);
                                }
                                font = f[0];
                            }
                            catch (Exception e) {
                                InstallOptionsPlugin.getDefault().log(e);
                                if(is != null) {
                                    try {
                                        is.close();
                                    }
                                    catch (Exception e1) {
                                        InstallOptionsPlugin.getDefault().log(e1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e1) {
            InstallOptionsPlugin.getDefault().log(e1);
        }
        if(font == null) {
            font = (cInstallOptionsFont==null?cDefaultDefaultFont:cInstallOptionsFont);
        }
        return font;
    }
}
