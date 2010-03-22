/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.editor.outline;

import java.io.File;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.IProgressMonitor;

// TODO Implement outline caching
/**
 * @author Sunil.Kamath
 *
 */
public class NSISOutlineCache implements IEclipseNSISService
{
    private static volatile NSISOutlineCache cInstance;
    private static final File cCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISOutlineCache.class.getName()+".ser"); //$NON-NLS-1$
    //    private static final File cCacheFolder = new File(EclipseNSISPlugin.getPluginStateLocation(),"outline"); //$NON-NLS-1$

    private Map<String, String> mOutlineCache;
    private INSISHomeListener mNSISHomeListener = null;

    public static NSISOutlineCache getInstance()
    {
        return cInstance;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mNSISHomeListener  = new INSISHomeListener() {
                public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
                {
                    mOutlineCache.clear();
                }
            };
            loadCache(monitor);
            NSISPreferences.INSTANCE.addListener(mNSISHomeListener);
            cInstance = this;
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.INSTANCE.removeListener(mNSISHomeListener);
            mNSISHomeListener = null;
        }
    }

    NSISOutlineElement getCachedOutline(String filename)
    {
        File file = new File(filename);
        if(file.exists()) {
            //NSISOutline
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void loadCache(IProgressMonitor monitor)
    {
        if(monitor != null) {
            monitor.beginTask("", 100); //$NON-NLS-1$
            monitor.subTask(EclipseNSISPlugin.getResourceString("loading.cache.message")); //$NON-NLS-1$
        }
        if(IOUtility.isValidFile(cCacheFile))
        {
            try {
                mOutlineCache = (CaseInsensitiveMap<String>)IOUtility.readObject(cCacheFile);
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
                mOutlineCache = new CaseInsensitiveMap<String>();
            }
        }
        else {
            mOutlineCache = new CaseInsensitiveMap();
        }
    }

    /*
    private class NSISOutlineCacheEntry implements Serializable
    {
        private static final long serialVersionUID = -5261030713063978974L;

        public Version nsisVersion;
        public Version version;
        public long timestamp;
        public long size;
        public String filename;
        public String cacheFilename;
    }
     */
}
