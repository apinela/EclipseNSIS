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

import java.util.Properties;

import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveProperties;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class NSISUsageProvider
{
    private static Properties cUsages = new CaseInsensitiveProperties();
    private static String cLineSeparator;
    private static NSISPreferences cPreferences = NSISPreferences.getPreferences();
    private static IPropertyChangeListener cPropertyChangeListener;
    
    static {
        cLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
        cPropertyChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
                    loadUsages();
                }
            }
        };
        cPreferences.getPreferenceStore().addPropertyChangeListener(cPropertyChangeListener);
        loadUsages();
    }
    
    public static String getUsage(String keyWord)
    {
        if(!Common.isEmpty(keyWord)) {
            return cUsages.getProperty(keyWord);
        }
        else {
            return null;
        }
    }

    private synchronized static void loadUsages()
    {
        cUsages.clear();
        String makeNsisExe = cPreferences.getNSISExe();
        if(makeNsisExe != null) {
            String[] output = Common.runProcessWithOutput(new String[]{makeNsisExe,"/V1","/CMDHELP"}, //$NON-NLS-1$ //$NON-NLS-2$
                                                          null,1);
            if(!Common.isEmptyArray(output)) {
                StringBuffer buf = null;
                for (int i = 0; i < output.length; i++) {
                    String line = output[i];
                    if(buf == null) {
                        buf = new StringBuffer(line);
                    }
                    else {
                        if(Character.isWhitespace(line.charAt(0))) {
                            buf.append(cLineSeparator).append(line);
                        }
                        else {
                            String usage = buf.toString();
                            int n = usage.indexOf(" "); //$NON-NLS-1$
                            String keyword = (n > 0?usage.substring(0,n):usage);
                            cUsages.setProperty(keyword,usage);
                            buf = new StringBuffer(line);
                        }
                    }
                }
                if(buf != null && buf.length() > 0) {
                    String usage = buf.toString();
                    int n = usage.indexOf(" "); //$NON-NLS-1$
                    String keyword = (n > 0?usage.substring(0,n):usage);
                    cUsages.setProperty(keyword,usage);
                }
            }
        }
    }
}
