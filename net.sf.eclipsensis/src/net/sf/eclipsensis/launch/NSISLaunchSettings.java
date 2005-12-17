/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.IFilter;

public class NSISLaunchSettings extends NSISSettings
{
    public static final String SCRIPT = "script"; //$NON-NLS-1$
    public static final String RUN_INSTALLER = "runInstaller"; //$NON-NLS-1$

    private NSISSettings mParent;
    private String mScript = ""; //$NON-NLS-1$
    private boolean mRunInstaller = false;
    private ILaunchConfiguration mLaunchConfig;
    private IFilter mFilter = null;

    NSISLaunchSettings(NSISSettings parent)
    {
        this(parent, null);
    }

    NSISLaunchSettings(NSISSettings parent, ILaunchConfiguration launchConfig)
    {
        this(parent, launchConfig, null);
    }

    NSISLaunchSettings(NSISSettings parent, ILaunchConfiguration launchConfig, IFilter filter)
    {
        mParent = parent;
        setLaunchConfig(launchConfig);
        mFilter = filter;
        load();
    }

    public ILaunchConfiguration getLaunchConfig()
    {
        return mLaunchConfig;
    }

    public void setLaunchConfig(ILaunchConfiguration launchConfig)
    {
        mLaunchConfig = launchConfig;
    }

    public boolean getRunInstaller()
    {
        return mRunInstaller;
    }

    public void setRunInstaller(boolean runInstaller)
    {
        mRunInstaller = runInstaller;
    }

    public String getScript()
    {
        return mScript;
    }

    public void setScript(String script)
    {
        mScript = script;
    }

    public void load()
    {
        mScript = getString(SCRIPT);
        mRunInstaller = getBoolean(RUN_INSTALLER);
        super.load();
    }

    public void store()
    {
        setValue(SCRIPT, mScript, "");
        setValue(RUN_INSTALLER, mRunInstaller, false);
        super.store();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String name)
    {
        if(mFilter == null || mFilter.select(name)) {
            boolean defaultValue = mParent.getBoolean(name);
            if(mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    public int getInt(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            int defaultValue = mParent.getInt(name);
            if (mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    public String getString(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            String defaultValue = mParent.getString(name);
            if (mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    public void setValue(String name, boolean value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    public void setValue(String name, int value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    public void setValue(String name, String value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    public void removeBoolean(String name)
    {
        remove(name);
    }

    private void remove(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (String)null);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    public void removeInt(String name)
    {
        remove(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    public void removeString(String name)
    {
        remove(name);
    }
    
    private List storeSymbols(Map map)
    {
        List list = new ArrayList();
        if (!Common.isEmptyMap(map)) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                String key = (String)entry.getKey();
                if (key != null) {
                    buf.append(key);
                }
                String value = (String)entry.getValue();
                if (value != null) {
                    buf.append('\255').append(value);
                }
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    buf.setLength(0);
                }
            }
        }
        return list;
    }
    
    private LinkedHashMap loadSymbols(List list)
    {
        LinkedHashMap map = new LinkedHashMap();
        if (!Common.isEmptyCollection(list)) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                String item = (String)iter.next();
                if (item != null && item.length() > 0) {
                    String key;
                    String value;
                    int n = item.indexOf('\255');
                    if (n >= 0) {
                        key = item.substring(0, n);
                        value = item.substring(n + 1);
                    }
                    else {
                        key = item;
                        value = ""; //$NON-NLS-1$
                    }
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    public void storeObject(String name, Object object)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                if (object instanceof String) {
                    ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (String)object);
                }
                else if (object instanceof List) {
                    ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (List)object);
                }
                else if (object instanceof Map) {
                    if (name.equals(SYMBOLS)) {
                        List list = storeSymbols((Map)object);
                        ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, list);
                    }
                    else {
                        ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (Map)object);
                    }
                }
                else if (object == null) {
                    remove(name);
                }
            }
        }
    }

    public void removeObject(String name)
    {
        storeObject(name, null);
    }

    public Object loadObject(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null) {
                try {
                    Map map = mLaunchConfig.getAttributes();
                    Object object = map.get(name);
                    if (name.equals(SYMBOLS) && object instanceof List) {
                        object = loadSymbols((List)object);
                    }
                    return object;
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    e.printStackTrace();
                }
            }
            return mParent.loadObject(name);
        }
        return null;
    }
}
