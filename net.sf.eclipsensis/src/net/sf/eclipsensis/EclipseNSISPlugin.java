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
import java.util.*;

import net.sf.eclipsensis.dialogs.NSISPreferencePage;
import net.sf.eclipsensis.editor.template.NSISTemplateContextType;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
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
	private ResourceBundle mResourceBundle = null;
    private String mName = null;
    private String mVersion = null;
    private TemplateStore mTemplateStore;
    private ContributionContextTypeRegistry mContextTypeRegistry;

	/**
	 * The constructor.
	 */
	public EclipseNSISPlugin() 
    {
		super();
		cPlugin = this;
		try {
			mResourceBundle = new EclipseNSISPluginResourceBundle();
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
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            IEclipseNSISPluginListener listener = (IEclipseNSISPluginListener) iter.next();
            listener.stopped();
            iter.remove();
        }
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
	public static String getResourceString(String key) {
        return getResourceString(key, key);
	}

    /**
     * Returns the string from the plugin's resource bundle,
     * or the default value if not found.
     */
    public static String getResourceString(String key, String defaultValue) {
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
	public ResourceBundle getResourceBundle() {
		return mResourceBundle;
	}
    
    /**
     * Returns this plug-in's template store.
     * 
     * @return the template store of this plug-in instance
     */
    public TemplateStore getTemplateStore() {
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
    public ContextTypeRegistry getContextTypeRegistry() {
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
    
    private class EclipseNSISPluginResourceBundle extends ResourceBundle
    {
        private ResourceBundle mResources = null;
        private ResourceBundle mMessages = null;
        private final Locale EMPTY_LOCALE = new Locale("","",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        public EclipseNSISPluginResourceBundle()
        {
            super();
            try {
                mResources = ResourceBundle.getBundle(RESOURCE_BUNDLE);
            } catch (MissingResourceException x) {
                mResources = null;
            }
            try {
                mMessages = ResourceBundle.getBundle(MESSAGE_BUNDLE);
            } catch (MissingResourceException x) {
                mMessages = null;
            }
        }

        /* (non-Javadoc)
         * @see java.util.ResourceBundle#getKeys()
         */
        public Enumeration getKeys()
        {
            ArrayList list = null;
            if(mResources != null) {
                list = Collections.list(mResources.getKeys());
            }
            if(mMessages != null) {
                if(list == null) {
                    list = Collections.list(mMessages.getKeys());
                }
                else {
                    list.addAll(Collections.list(mMessages.getKeys()));
                }
            }
            if(list != null) {
                return Collections.enumeration(list);
            }
            else {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }
        }

        /* (non-Javadoc)
         * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
         */
        protected Object handleGetObject(String key)
        {
            if(mResources != null) {
                try {
                    return mResources.getObject(key);
                }
                catch(MissingResourceException mre) {
                }
            }
            if(mMessages != null) {
                try {
                    return mMessages.getObject(key);
                }
                catch(MissingResourceException mre) {
                }
            }
            return null;
        }
        
        /* (non-Javadoc)
         * @see java.util.ResourceBundle#getLocale()
         */
        public Locale getLocale()
        {
            if(mMessages != null) {
                return mMessages.getLocale();
            }
            else if(mResources != null) {
                return mResources.getLocale();
            }
            else {
                return EMPTY_LOCALE;
            }
        }

        /* (non-Javadoc)
         * @see java.util.ResourceBundle#setParent(java.util.ResourceBundle)
         */
        protected void setParent(ResourceBundle parent)
        {
        }
    }
}
