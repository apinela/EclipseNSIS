/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.eclipsensis.console.NSISConsole;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseNSISPlugin extends AbstractUIPlugin implements INSISConstants
{
    //The shared instance.
	private static EclipseNSISPlugin cPlugin;
    
	//Resource bundle.
	private ResourceBundle mResourceBundle;
    private Bundle mBundle = null;

	/**
	 * The constructor.
	 */
	public EclipseNSISPlugin() {
		super();
		cPlugin = this;
		try {
			mResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		} catch (MissingResourceException x) {
			mResourceBundle = null;
		}
	}

    /**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
        mBundle = context.getBundle();
        MakeNSISRunner.startup();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
        MakeNSISRunner.shutdown();
	}

	/**
	 * Returns the shared instance.
	 */
	public static EclipseNSISPlugin getDefault() {
		return cPlugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return mResourceBundle;
	}
    
    public boolean isConfigured()
    {
        return (NSISPreferences.getPreferences().getNSISExe() != null);
    }
    
    public void testInstaller(String exeName)
    {
        if(exeName != null) {
            File exeFile = new File(exeName);
            if (exeFile.exists()) {
                File workDir = exeFile.getParentFile();
                try {
                    Runtime.getRuntime().exec(new String[]{exeName},null,workDir);
                }
                catch(IOException ex) {
                    NSISConsole console = NSISConsole.getConsole();
                    if(console != null) {
                        console.clear();
                        console.add(NSISConsoleLine.error(ex.getMessage()));
                    }
                    else {
                        MessageDialog.openError(getWorkbench().getActiveWorkbenchWindow().getShell(),
                                                getResourceString("error.title"),ex.getMessage()); //$NON-NLS-1$
                    }
                }
            }
        }
    }
}
