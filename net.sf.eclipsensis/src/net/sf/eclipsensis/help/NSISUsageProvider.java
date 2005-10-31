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
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IProgressMonitor;

public class NSISUsageProvider implements IEclipseNSISService
{
    private static NSISUsageProvider cInstance = null;
    
    private Map mUsages = null;
    private String mLineSeparator;
    private INSISHomeListener mNSISHomeListener = null;

    public static NSISUsageProvider getInstance()
    {
        return cInstance;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mUsages = new CaseInsensitiveMap();
            mNSISHomeListener = new INSISHomeListener() {
                public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
                {
                    loadUsages(monitor);
                }
            };
            mLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
            loadUsages(monitor);
            NSISPreferences.INSTANCE.addListener(mNSISHomeListener);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.INSTANCE.removeListener(mNSISHomeListener);
            mUsages = null;
            mNSISHomeListener = null;
            mLineSeparator = null;
        }
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

    private synchronized void loadUsages(IProgressMonitor monitor)
    {
        if(monitor != null) {
            monitor.subTask(EclipseNSISPlugin.getResourceString("loading.cmdhelp.message")); //$NON-NLS-1$
        }
        mUsages.clear();
        File exeFile = NSISPreferences.INSTANCE.getNSISExeFile();
        if(exeFile != null && exeFile.exists()) {
            long exeTimeStamp = exeFile.lastModified();
            
            File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
            File cacheFile = new File(stateLocation,NSISUsageProvider.class.getName()+".Usages.ser"); //$NON-NLS-1$
            long cacheTimeStamp = 0;
            if(cacheFile.exists()) {
                cacheTimeStamp = cacheFile.lastModified();
            }
            
            if(exeTimeStamp != cacheTimeStamp) {
                String[] output = MakeNSISRunner.runProcessWithOutput(exeFile.getAbsolutePath(),new String[]{
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
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else {
                try {
                    mUsages = (Map)Common.readObject(cacheFile);
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }
}
