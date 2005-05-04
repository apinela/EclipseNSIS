/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.CompoundResourceBundle;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class InstallOptionsPlugin extends AbstractUIPlugin implements IInstallOptionsConstants
{
    private static InstallOptionsPlugin cPlugin;
    private ResourceBundle mResourceBundle;
    public static final String[] BUNDLE_NAMES = new String[]{RESOURCE_BUNDLE,MESSAGE_BUNDLE};
    private ImageManager mImageManager;
    private boolean mZoomSupported = false;
    
    /**
     * 
     */
    public InstallOptionsPlugin()
    {
        super();
        cPlugin = this;
        try {
            mResourceBundle = new CompoundResourceBundle(InstallOptionsPlugin.class.getClassLoader(),BUNDLE_NAMES);
        } 
        catch (MissingResourceException x) {
            x.printStackTrace();
        }
        mZoomSupported = Boolean.valueOf(getResourceString("zoom.supported")).booleanValue(); //$NON-NLS-1$
    }

    /**
     * Returns the shared instance.
     */
    public static InstallOptionsPlugin getDefault() {
        return cPlugin;
    }

    public static ImageManager getImageManager()
    {
        return getDefault().mImageManager;
    }
    
    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) 
    {
        ResourceBundle bundle = InstallOptionsPlugin.getDefault().getResourceBundle();
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
     * Returns the string from the plugin bundle's resource bundle,
     * or 'key' if not found.
     */
    public static String getBundleResourceString(String key) 
    {
        return Platform.getResourceString(getDefault().getBundle(), key);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return mResourceBundle;
    }
    
    public boolean isZoomSupported()
    {
        return mZoomSupported;
    }

    private void initializePreference(IPreferenceStore store, String name, String defaultValue)
    {
        store.setDefault(name,defaultValue);
        if(!store.contains(name)) {
            store.setToDefault(name);
        }
    }

    private void initializePreferences()
    {
        IPreferenceStore store = getPreferenceStore();
        initializePreference(store,PREFERENCE_SHOW_GRID,SHOW_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_RULERS,SHOW_RULERS_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_GUIDES,SHOW_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_DIALOG_SIZE,SHOW_DIALOG_SIZE_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GRID,SNAP_TO_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GEOMETRY,SNAP_TO_GEOMETRY_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GUIDES,SNAP_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GLUE_TO_GUIDES,GLUE_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER.asString(GRID_SPACING_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER.asString(GRID_ORIGIN_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_STYLE,GRID_STYLE_DEFAULT);
        if(isZoomSupported()) {
            initializePreference(store,PREFERENCE_ZOOM,ZOOM_DEFAULT);
        }
    }

    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        mImageManager = new ImageManager(this);
        initializePreferences();
    }

    public void stop(BundleContext context) throws Exception
    {
        mImageManager = null;
        super.stop(context);
    }
}
