/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.startup;

import java.util.*;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EclipseNSISStartup extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID = "net.sf.eclipsensis.startup"; //$NON-NLS-1$

	// The shared instance
	private static EclipseNSISStartup cPlugin;

    private BundleContext mBundleContext;
    private ResourceBundle mResourceBundle;

	/**
	 * The constructor
	 */
	public EclipseNSISStartup()
    {
        try {
            mResourceBundle = ResourceBundle.getBundle("net.sf.eclipsensis.startup.EclipseNSISStartupMessages"); //$NON-NLS-1$
        }
        catch(MissingResourceException mre) {
            mResourceBundle = null;
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
    {
        cPlugin = this;
        mBundleContext = context;
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
    {
		super.stop(context);
        cPlugin = null;
        mBundleContext = null;
	}

	BundleContext getBundleContext()
    {
        return mBundleContext;
    }

    /**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EclipseNSISStartup getDefault()
    {
		return cPlugin;
	}

    public ResourceBundle getResourceBundle()
    {
        return mResourceBundle;
    }

    public static String getResourceString(String key)
    {
        EclipseNSISStartup plugin = getDefault();
        if(plugin != null) {
            ResourceBundle bundle = plugin.getResourceBundle();
            try {
                return (bundle != null) ? bundle.getString(key) : key;
            }
            catch (MissingResourceException e) {
            }
        }
        return key;
    }
}
