package net.sf.eclipsensis.update;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.eclipsensis.update.scheduler.Scheduler;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseNSISUpdatePlugin extends AbstractUIPlugin 
{
    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.update.EclipseNSISUpdatePluginResources"; //$NON-NLS-1$
    public static final String PLUGIN_CONTEXT_PREFIX = "net.sf.eclipsensis."; //$NON-NLS-1$

    //The shared instance.
	private static EclipseNSISUpdatePlugin cPlugin;
    private static File cStateLocation = null;
    private static Image cShellImage;
	
    //Resource bundle.
    private ResourceBundle mResourceBundle;
    private String mPluginId;

    /**
	 * The constructor.
	 */
	public EclipseNSISUpdatePlugin() 
    {
        cPlugin = this;
        try {
            mResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
        } catch (MissingResourceException x) {
            mResourceBundle = null;
        }
	}

    public static synchronized File getPluginStateLocation()
    {
        if(cStateLocation == null) {
            EclipseNSISUpdatePlugin plugin = getDefault();
            if(plugin != null) {
                cStateLocation = plugin.getStateLocation().toFile();
            }
        }
        return cStateLocation;
    }

    public void start(BundleContext context) throws Exception
    {
        mPluginId = context.getBundle().getSymbolicName();
        super.start(context);
    }

    /**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
    {
        Scheduler scheduler = Scheduler.getInstance();
        if(scheduler != null) {
            scheduler.shutDown();
        }
		super.stop(context);
		cPlugin = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static EclipseNSISUpdatePlugin getDefault() 
    {
		return cPlugin;
	}

    public static String getResourceString(String key) 
    {
        return getResourceString(key, key);
    }

    public static String getResourceString(String key, String defaultValue) 
    {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() 
    {
        return mResourceBundle;
    }

    public void log(Throwable t)
    {
        ILog log = getLog();
        if(log != null) {
            IStatus status;
            if(t instanceof CoreException) {
                status = ((CoreException)t).getStatus();
            }
            else {
                String message = t.getMessage();
                status = new Status(IStatus.ERROR,getPluginId(),IStatus.ERROR, message==null?t.getClass().getName():message,t);
            }
            log.log(status);
        }
        else {
            t.printStackTrace();
        }
    }

    public String getPluginId()
    {
        return mPluginId;
    }

    public static Image getShellImage()
    {
        if(cShellImage == null) {
            Display.getDefault().syncExec(new Runnable() {
                public void run()
                {
                    cShellImage = getImageDescriptor(getResourceString("update.icon")).createImage(Display.getCurrent()); //$NON-NLS-1$
                }
            });
        }
        return cShellImage;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(getDefault().getPluginId(), path);
    }
}
