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
import net.sf.eclipsensis.dialogs.NSISPreferencePage;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseNSISPlugin extends AbstractUIPlugin implements INSISConstants
{
    //The shared instance.
    public static final int NSIS_REG_ROOTKEY = WinAPI.HKEY_LOCAL_MACHINE;
	public static final String NSIS_REG_SUBKEY = "SOFTWARE\\NSIS"; //$NON-NLS-1$
    public static final String NSIS_REG_VALUE = ""; //$NON-NLS-1$
    private static EclipseNSISPlugin cPlugin;
    
	//Resource bundle.
	private ResourceBundle mResourceBundle;
    private NSISHelpURLProvider mHelpURLProvider = null;
    private String mName;
    private String mVersion;

	/**
	 * The constructor.
	 */
	public EclipseNSISPlugin() 
    {
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
        mName = (String)getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mVersion = (String)getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
        validateOS();
        if(!isConfigured()) {
            // First try autoconfigure
            NSISPreferences prefs = NSISPreferences.getPreferences();
            prefs.setNSISHome(WinAPI.RegQueryStrValue(NSIS_REG_ROOTKEY,NSIS_REG_SUBKEY,NSIS_REG_VALUE));
            if(!isConfigured()) {
                Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
                if(MessageDialog.openConfirm(shell,mName,getResourceString("unconfigured.confirm"))) { //$NON-NLS-1$
                    configure();
                }
                if(!isConfigured()) {
                    MessageDialog.openWarning(shell,mName,getResourceString("unconfigured.warning")); //$NON-NLS-1$
                }
            }
            else {
                prefs.store();
            }
        }
        MakeNSISRunner.startup();
        mHelpURLProvider = new NSISHelpURLProvider();
	}
    
    private void configure()
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                NSISPreferencePage.show();
            }
        });
    }

    private void validateOS() throws CoreException
    {
        String[] supportedOS = Common.loadArrayProperty(getResourceBundle(),"supported.os"); //$NON-NLS-1$
        if(!Common.isEmptyArray(supportedOS)) {
            String osName = System.getProperty("os.name"); //$NON-NLS-1$
            for(int i=0; i<supportedOS.length; i++) {
                if(osName.equalsIgnoreCase(supportedOS[i])) {
                    return;
                }
            }
            String osError = getResourceString("unsupported.os.error"); //$NON-NLS-1$
            MessageDialog.openError(getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    mName,osError);
            throw new CoreException(new Status(IStatus.ERROR,PLUGIN_NAME,IStatus.ERROR,osError,
                                    new RuntimeException(osError)));
        }
    }
    
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
        MakeNSISRunner.shutdown();
        if(mHelpURLProvider != null) {
            mHelpURLProvider.dispose();
            mHelpURLProvider = null;
        }
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
        return getResourceString(key, key);
	}

    /**
     * Returns the string from the plugin's resource bundle,
     * or the default value if not found.
     */
    public static String getResourceString(String key, String defaultValue) {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : defaultValue;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return mResourceBundle;
	}
    
    /**
     * @return Returns the mName.
     */
    public String getName()
    {
        return mName;
    }
    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        return mVersion;
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

    /**
     * @return Returns the helpURLProvider.
     */
    public NSISHelpURLProvider getHelpURLProvider()
    {
        return mHelpURLProvider;
    }
}
