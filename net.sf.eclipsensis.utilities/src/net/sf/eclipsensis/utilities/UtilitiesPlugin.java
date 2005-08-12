/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities;

import java.net.URL;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UtilitiesPlugin extends AbstractUIPlugin 
{
	//The shared instance.
	private static UtilitiesPlugin cPlugin;
    private ResourceBundle mResourceBundle;
    private Image mShellImage;
	
	/**
	 * The constructor.
	 */
	public UtilitiesPlugin() 
    {
		cPlugin = this;
        try {
            mResourceBundle = ResourceBundle.getBundle("net.sf.eclipsensis.utilities.UtilitiesPluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            mResourceBundle = null;
        }
	}
	/**
	 * Returns the shared instance.
	 */
	public static UtilitiesPlugin getDefault() 
    {
		return cPlugin;
	}

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) 
    {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }
    
    public static String getFormattedString(String key, Object[] args)
    {
        return MessageFormat.format(getResourceString(key),args);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return mResourceBundle;
    }
    
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        URL entry = getBundle().getEntry(getResourceString("utilities.icon"));
        getImageRegistry().put("utilities.icon", ImageDescriptor.createFromURL(entry));
        mShellImage = getImageRegistry().get("utilities.icon");
    }
    
    public Image getShellImage()
    {
        return mShellImage;
    }
    
    public void stop(BundleContext context) throws Exception
    {
        mShellImage = null;
        super.stop(context);
    }
}
