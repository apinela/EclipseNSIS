package net.sf.jarsigner;

import java.io.*;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.jdt.launching.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

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

    public static List tokenize(String text, char separator)
    {
        ArrayList list = new ArrayList();
        if(text != null && text.length() > 0) {
            char[] chars = text.toCharArray();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] != separator) {
                    buf.append(chars[i]);
                }
                else {
                    list.add(buf.toString());
                    buf.delete(0,buf.length());
                }
            }
            list.add(buf.toString().trim());
        }
        return list;
    }

    public static Version parseVersion(String ver)
    {
        int[] parts={0,0,0};
    
        List list = tokenize(ver,'.');
        int n = Math.min(parts.length,list.size());
        String temp="";
        for(int i=0; i<n; i++) {
            outer: {
                String token = (String)list.get(i);
                char[] chars = token.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    if(!Character.isDigit(chars[j])) {
                        parts[i] = (j>0?Integer.parseInt(token.substring(0,j)):0);
                        temp=token.substring(j);
                        break outer;
                    }
                }
                parts[i] = Integer.parseInt(token);
            }
        }
        StringBuffer buf = new StringBuffer(temp);
        for(int i=n; i<list.size(); i++) {
            buf.append(".").append(list.get(i));
        }
        
        return new Version(parts[0], parts[1], parts[2], buf.toString());
    }
    
    public IVMInstall getVMInstall()
    {
        Version minVersion = new Version(1,2,0);
        if(mVMInstall == null) {
            IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
            if(types != null) {
                outer:
                for (int i = 0; i < types.length; i++) {
                    IVMInstall[] installs = types[i].getVMInstalls();
                    if(installs != null) {
                        for (int j = 0; j < installs.length; j++) {
                            if(installs[j] instanceof IVMInstall2) {
                                Version version = parseVersion(((IVMInstall2)installs[j]).getJavaVersion());
                                if(version.compareTo(minVersion) >= 0) {
                                    mVMInstall = installs[j];
                                    minVersion = version;
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
