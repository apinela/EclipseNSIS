package net.sf.jarsigner;

import java.io.*;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.jdt.launching.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JARSignerPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static JARSignerPlugin cPlugin;
    private ResourceBundle mResourceBundle;
    private IVMInstall mVMInstall;
	
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
	public void start(BundleContext context) throws Exception 
    {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		cPlugin = null;
	}
    
    private int[] getVMInstallVersion(IVMInstall2 vmInstall)
    {
        StringTokenizer st = new StringTokenizer(vmInstall.getJavaVersion(),".");
        int[] version = new int[st.countTokens()];
        for (int i = 0; i < version.length; i++) {
            try {
                version[i] = Integer.parseInt(st.nextToken());
            }
            catch(NumberFormatException nfe) {
                version[i] = 0;
            }
        }
        return version;
    }

    private int compareVersion(int[] v1, int[] v2)
    {
        final int minLength = Math.min(v1.length,v2.length);
        for(int i=0; i<minLength; i++) {
            int n = v1[i]-v2[i];
            if(n != 0) {
                return n;
            }
        }
        return v1.length-v2.length;
    }
    
    public IVMInstall getVMInstall()
    {
        int[] minVersion = {1,2};
        if(mVMInstall == null) {
            mVMInstall = JavaRuntime.getDefaultVMInstall();
            if(mVMInstall instanceof IVMInstall2) {
                if(compareVersion(getVMInstallVersion((IVMInstall2)mVMInstall), minVersion) < 0) {
                    mVMInstall = null;
                }
            }
            else {
                mVMInstall = null;
            }
            if(mVMInstall == null) {
                IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
                if(types != null) {
                    outer:
                    for (int i = 0; i < types.length; i++) {
                        IVMInstall[] installs = types[i].getVMInstalls();
                        if(installs != null) {
                            for (int j = 0; j < installs.length; j++) {
                                if(installs[j] instanceof IVMInstall2) {
                                    if(compareVersion(getVMInstallVersion((IVMInstall2)installs[j]), minVersion) >= 0) {
                                        mVMInstall = installs[j];
                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
        return mVMInstall;
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

    public static boolean isEmpty(String str)
    {
        return (str == null || str.trim().length() == 0);
    }
    
    public static KeyStore loadKeyStore(String keyStoreLocation, String storePassword)
    {
        InputStream is = null;
        try {
            File file = new File(keyStoreLocation);
            if(file.exists() && file.isFile()) {
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
