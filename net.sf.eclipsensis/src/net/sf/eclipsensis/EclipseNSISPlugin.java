/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.dialogs.NSISPreferencePage;
import net.sf.eclipsensis.editor.template.NSISTemplateContextType;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
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
    private static File cStateLocation = null;
    
	private ArrayList mListeners = new ArrayList();
    private String mName = null;
    private String mVersion = null;
    private TemplateStore mTemplateStore;
    private ContributionContextTypeRegistry mContextTypeRegistry;
    private Locale mLocale;
    private HashMap mResourceBundles = new HashMap();
    public static final String[] BUNDLE_NAMES = new String[]{RESOURCE_BUNDLE,MESSAGE_BUNDLE};
    private ImageManager mImageManager;

	/**
	 * The constructor.
	 */
	public EclipseNSISPlugin() 
    {
		super();
		cPlugin = this;
        mLocale = Locale.getDefault();
		try {
			mResourceBundles.put(mLocale,new CompoundResourceBundle(mLocale, BUNDLE_NAMES));
		} 
        catch (MissingResourceException x) {
			x.printStackTrace();
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
    {
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
                if(Common.openConfirm(shell,getResourceString("unconfigured.confirm"))) { //$NON-NLS-1$
                    configure();
                }
                if(!isConfigured()) {
                    Common.openWarning(shell,getResourceString("unconfigured.warning")); //$NON-NLS-1$
                }
            }
            else {
                prefs.store();
            }
        }
        mImageManager = new ImageManager(this);
        
        if(isConfigured()) {
            NSISHelpURLProvider.init();
        }
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
            Common.openError(getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    osError);
            throw new CoreException(new Status(IStatus.ERROR,PLUGIN_NAME,IStatus.ERROR,osError,
                                    new RuntimeException(osError)));
        }
    }
    
    public static ImageManager getImageManager()
    {
        return getDefault().mImageManager;
    }
    
    /**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            IEclipseNSISPluginListener listener = (IEclipseNSISPluginListener) iter.next();
            listener.stopped();
            iter.remove();
        }
        mImageManager = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static EclipseNSISPlugin getDefault() {
		return cPlugin;
	}
    
    public static File getPluginStateLocation()
    {
        if(cStateLocation == null) {
            synchronized(EclipseNSISPlugin.class) {
                if(cStateLocation == null) {
                    EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
                    if(plugin != null) {
                        cStateLocation = plugin.getStateLocation().toFile();
                    }                    
                }
            }
        }
        return cStateLocation;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getFormattedString(String key, Object[] args)
    {
        return MessageFormat.format(getResourceString(key),args);
    }

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) 
    {
        return getResourceString(key, key);
	}

    /**
     * Returns the string from the plugin's resource bundle,
     * or the default value if not found.
     */
    public static String getResourceString(String key, String defaultValue) 
    {
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        if(plugin != null) {
            ResourceBundle bundle = plugin.getResourceBundle();
            try {
                return (bundle != null) ? bundle.getString(key) : defaultValue;
            }
            catch (MissingResourceException e) {
            }
        }
        return defaultValue;
    }

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() 
    {
		return getResourceBundle(mLocale);
	}

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle(Locale locale) 
    {
        if(!mResourceBundles.containsKey(locale)) {
            synchronized(this) {
                if(!mResourceBundles.containsKey(locale)) {
                    mResourceBundles.put(locale,new CompoundResourceBundle(locale, BUNDLE_NAMES));
                }                
            }
        }
        return (ResourceBundle)mResourceBundles.get(locale);
    }
    
    /**
     * Returns this plug-in's template store.
     * 
     * @return the template store of this plug-in instance
     */
    public TemplateStore getTemplateStore() 
    {
        if (mTemplateStore == null) {
            mTemplateStore= new ContributionTemplateStore(getContextTypeRegistry(), 
                            NSISPreferences.getPreferences().getPreferenceStore(), 
                            INSISPreferenceConstants.CUSTOM_TEMPLATES);
            try {
                mTemplateStore.load();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mTemplateStore;
    }

    /**
     * Returns this plug-in's context type registry.
     * 
     * @return the context type registry for this plug-in instance
     */
    public ContextTypeRegistry getContextTypeRegistry() 
    {
        if (mContextTypeRegistry == null) {
            mContextTypeRegistry= new ContributionContextTypeRegistry();
            mContextTypeRegistry.addContextType(NSISTemplateContextType.NSIS_TEMPLATE_CONTEXT_TYPE);
        }
        return mContextTypeRegistry;
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
    
    public void addListener(IEclipseNSISPluginListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
}
