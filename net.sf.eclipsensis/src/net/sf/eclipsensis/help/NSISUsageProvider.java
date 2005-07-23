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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class NSISUsageProvider implements IEclipseNSISService
{
    public static NSISUsageProvider INSTANCE = null;
    
    private Map mUsages = new CaseInsensitiveMap();
    private String mLineSeparator;
    private NSISPreferences mPreferences = NSISPreferences.getPreferences();
    private IPropertyChangeListener mPropertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
                loadUsages();
            }
        }
    };

    public void start(IProgressMonitor monitor)
    {
        monitor.subTask("Loading NSIS command help");
        mLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
        loadUsages();
        mPreferences.getPreferenceStore().addPropertyChangeListener(mPropertyChangeListener);
        INSTANCE = this;
    }

    public void stop(IProgressMonitor monitor)
    {
        INSTANCE = null;
        mPreferences.getPreferenceStore().removePropertyChangeListener(mPropertyChangeListener);
    }
    
    public String getUsage(String keyWord)
    {
        if(!Common.isEmpty(keyWord)) {
            return (String)mUsages.get(keyWord);
        }
        else {
            return null;
        }
    }

    private synchronized void loadUsages()
    {
        mUsages.clear();
        String makeNSISExe = mPreferences.getNSISExe();
        if(makeNSISExe != null) {
            File exeFile = new File(makeNSISExe);
            if(exeFile.exists()) {
                long exeTimeStamp = exeFile.lastModified();
                
                File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
                File cacheFile = new File(stateLocation,NSISUsageProvider.class.getName()+".Usages.ser"); //$NON-NLS-1$
                long cacheTimeStamp = 0;
                if(cacheFile.exists()) {
                    cacheTimeStamp = cacheFile.lastModified();
                }
                
                if(exeTimeStamp != cacheTimeStamp) {
                    String[] output = Common.runProcessWithOutput(new String[]{makeNSISExe,
                                                                  MakeNSISRunner.MAKENSIS_VERBOSITY_OPTION+"1", //$NON-NLS-1$
                                                                  MakeNSISRunner.MAKENSIS_CMDHELP_OPTION},
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
                                    buf.append(mLineSeparator).append(line);
                                }
                                else {
                                    String usage = buf.toString();
                                    int n = usage.indexOf(" "); //$NON-NLS-1$
                                    String keyword = (n > 0?usage.substring(0,n):usage);
                                    mUsages.put(keyword,usage);
                                    buf = new StringBuffer(line);
                                }
                            }
                        }
                        if(buf != null && buf.length() > 0) {
                            String usage = buf.toString();
                            int n = usage.indexOf(" "); //$NON-NLS-1$
                            String keyword = (n > 0?usage.substring(0,n):usage);
                            mUsages.put(keyword,usage);
                        }
                    }
                    try {
                        Common.writeObject(cacheFile,mUsages);
                        cacheFile.setLastModified(exeTimeStamp);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        mUsages = (Map)Common.readObject(cacheFile);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
