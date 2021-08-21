/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner;

import java.io.*;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.utilities.util.Common;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JARSignerPlugin extends AbstractUIPlugin {

    //The shared instance.
    private static JARSignerPlugin cPlugin;
    private ResourceBundle mResourceBundle;

    /**
     * The constructor.
     */
    public JARSignerPlugin()
    {
        cPlugin = this;
        try {
            mResourceBundle = ResourceBundle.getBundle("net.sf.jarsigner.JARSignerPluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            mResourceBundle = null;
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        cPlugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static JARSignerPlugin getDefault()
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
            return bundle != null ? bundle.getString(key) : key;
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

    public static KeyStore loadKeyStore(String keyStoreLocation, String storePassword)
    {
        InputStream is = null;
        try {
            File file = new File(keyStoreLocation);
            if(Common.isValidFile(file)) {
                is = new BufferedInputStream(new FileInputStream(file));
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(is,storePassword.toCharArray());
                return ks;
            }
        }
        catch (Exception e) {
//            e.printStackTrace();
        }
        finally {
            if(is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
