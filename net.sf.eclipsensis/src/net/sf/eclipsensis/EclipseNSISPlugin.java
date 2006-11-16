/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.console.NSISConsole;
import net.sf.eclipsensis.dialogs.MinimalProgressMonitorDialog;
import net.sf.eclipsensis.dialogs.NSISConfigWizardDialog;
import net.sf.eclipsensis.editor.template.NSISTemplateContextType;
import net.sf.eclipsensis.filemon.FileMonitor;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.Version;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseNSISPlugin extends AbstractUIPlugin implements INSISConstants
{
    private static EclipseNSISPlugin cPlugin;
    private static File cStateLocation = null;
    private static String cInvalidException = null;
    private static Image cShellImage;

    private BundleContext mBundleContext = null;
    private String mName = null;
    private String mVersion = null;
    private TemplateStore mTemplateStore;
    private ContributionContextTypeRegistry mContextTypeRegistry;
    private Locale mLocale;
    private Map mResourceBundles = new HashMap();
    public static final String[] BUNDLE_NAMES = new String[]{RESOURCE_BUNDLE,MESSAGE_BUNDLE};
    private ImageManager mImageManager;
    private boolean mIsNT = false;
    private String mJavaVendor;
    private Version mJavaVersion;
    private Stack mServices = new Stack();
    private JobScheduler mJobScheduler = new JobScheduler();

    private NSISConsole mConsole = null;

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
            log(x);
		}
        mImageManager = new ImageManager(this);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
    {
        super.start(context);
        mBundleContext = context;
        if(!isDebugging()) {
            if(Boolean.getBoolean("net.sf.eclipsensis/debug")) { //$NON-NLS-1$
                setDebugging(true);
            }
        }
        cShellImage = mImageManager.getImage(getResourceString("nsis.icon")); //$NON-NLS-1$
        mName = (String)getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mVersion = (String)getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
        if(cInvalidException != null) {
            throw new CoreException(new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,cInvalidException,
                                    new RuntimeException(cInvalidException)));
        }
        validateOS();
        validateVM();
        if(!isConfigured()) {
            // First try autoconfigure
            NSISPreferences.INSTANCE.setNSISHome(WinAPI.RegQueryStrValue(INSISConstants.NSIS_REG_ROOTKEY,INSISConstants.NSIS_REG_SUBKEY,INSISConstants.NSIS_REG_VALUE));
            if(!isConfigured()) {
                final IWorkbenchWindow wwindow = getWorkbench().getActiveWorkbenchWindow();
                final Runnable configOp = new Runnable() {
                    public void run()
                    {
                        Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
                        if(Common.openConfirm(shell,getResourceString("unconfigured.confirm"),getShellImage())) { //$NON-NLS-1$
                            configure();
                        }
                        if(!isConfigured()) {
                            Common.openWarning(shell,getResourceString("unconfigured.warning"),getShellImage()); //$NON-NLS-1$
                        }
                    }
                };
                if(wwindow.getShell().isVisible()) {
                    configOp.run();
                }
                else {
                    getWorkbench().addWindowListener(new IWindowListener(){
                        private void schedule()
                        {
                            getJobScheduler().scheduleUIJob(EclipseNSISPlugin.getResourceString("starting.eclipsensis.message"), //$NON-NLS-1$
                                                            new IJobStatusRunnable() {
                                                                public IStatus run(IProgressMonitor monitor)
                                                                {
                                                                    configOp.run();
                                                                    return Status.OK_STATUS;
                                                                }
                                                            });
                        }

                        public void windowActivated(IWorkbenchWindow window)
                        {
                            if(window == wwindow && window.getShell().isVisible()) {
                                getWorkbench().removeWindowListener(this);
                                schedule();
                            }
                        }

                        public void windowDeactivated(IWorkbenchWindow window)
                        {
                        }

                        public void windowClosed(IWorkbenchWindow window)
                        {
                            if(window == wwindow) {
                                getWorkbench().removeWindowListener(this);
                            }
                        }

                        public void windowOpened(IWorkbenchWindow window)
                        {
                            if(window == wwindow && window.getShell().isVisible()) {
                                getWorkbench().removeWindowListener(this);
                                schedule();
                            }
                        }
                    });
                }
            }
            else {
                NSISPreferences.INSTANCE.store();
            }
        }

        FileMonitor.INSTANCE.start();
        startServices();
        mJobScheduler.start();
        mConsole = new NSISConsole();
//        mJobScheduler.scheduleJob("parse", new IJobStatusRunnable() {
//
//            public IStatus run(IProgressMonitor monitor)
//            {
//                try {
//                    NSISParser.getInstance().processScript(new File("c:\\temp\\dummy.nsi"));
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return Status.OK_STATUS;
//            }
//
//        });
	}

    private void startServices()
    {
        final String[] services = Common.loadArrayProperty(getResourceBundle(),"services"); //$NON-NLS-1$
        if(!Common.isEmptyArray(services)) {
            final IRunnableWithProgress op = new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor)
                {
                    monitor = new DisplayUpdateProgressMonitor(monitor);
                    try {
                        String taskName = EclipseNSISPlugin.getResourceString("starting.eclipsensis.message"); //$NON-NLS-1$
                        monitor.beginTask(taskName,services.length+1);
                        for (int i = 0; i < services.length; i++) {
                            NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor,taskName,1);
                            try {
                                Class clasz = Class.forName(services[i]);
                                Constructor constructor = clasz.getConstructor(null);
                                IEclipseNSISService service = (IEclipseNSISService)constructor.newInstance(null);
                                service.start(subMonitor);
                                mServices.push(service);
                            }
                            catch (Exception e) {
                                log(e);
                            }
                            finally {
                                subMonitor.done();
                            }
                        }
                        monitor.subTask(EclipseNSISPlugin.getResourceString("starting.makensis.message")); //$NON-NLS-1$
                        Runnable runnable = new Runnable() {
                            public void run()
                            {
                                MakeNSISRunner.startup();
                            }
                        };
                        if(Display.getCurrent() == null) {
                            Display.getDefault().syncExec(runnable);
                        }
                        else {
                            runnable.run();
                        }

                        monitor.worked(1);
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally {
                        monitor.done();
                    }
                }
            };

            run(false, false, op);
        }
    }

    public void registerService(IEclipseNSISService service)
    {
        if(!mServices.contains(service)) {
            if(!service.isStarted()) {
                service.start(new NullProgressMonitor());
            }
            mServices.push(service);
        }
    }

    public void run(final boolean fork, final boolean cancelable, final IRunnableWithProgress runnable)
    {
        try {
            if(Display.getCurrent() == null) {
                //fork and cancelable are meaningless here
                runnable.run(new NullProgressMonitor());
            }
            else {
                boolean useWorkbenchWindow = false;
                try {
                    useWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().isVisible();
                }
                catch (Exception e) {
                    useWorkbenchWindow = false;
                }
                if(!useWorkbenchWindow) {
                    OutputStream os = null;
                    if(mBundleContext != null && mBundleContext.getBundle().getState() == Bundle.STARTING) {
                        try {
                            ServiceReference[] ref = mBundleContext.getServiceReferences(OutputStream.class.getName(), null);
                            for (int i = 0; i < ref.length; i++) {
                                String name = (String) ref[i].getProperty("name"); //$NON-NLS-1$
                                if (name != null && name.equals("splashstream")) {  //$NON-NLS-1$
                                    Object result = mBundleContext.getService(ref[i]);
                                    mBundleContext.ungetService(ref[i]);
                                    os = (OutputStream) result;
                                    break;
                                }
                            }
                        }
                        catch (InvalidSyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    if(os != null) {
                        //Startup in progress- overlay the splash screen
                        String splashFile = System.getProperty("osgi.splashLocation"); //$NON-NLS-1$
                        if(!Common.isEmpty(splashFile)) {
                            File file = new File(splashFile);
                            if(IOUtility.isValidFile(file)) {
                                ImageDescriptor desc = ImageDescriptor.createFromURL(file.toURI().toURL());
                                final Image image = desc.createImage();
                                Rectangle rect = image.getBounds();
                                String foregroundColor = null;
                                IProduct product = Platform.getProduct();
                                if (product != null) {
                                    foregroundColor = product.getProperty(IProductConstants.STARTUP_FOREGROUND_COLOR);
                                }
                                RGB fgRGB;
                                try {
                                    fgRGB = ColorManager.getRGB(Integer.parseInt(foregroundColor, 16));
                                } catch (Exception ex) {
                                    fgRGB = ColorManager.getRGB(13817855); // D2D7FF=white
                                }
                                Monitor monitor = Display.getCurrent().getPrimaryMonitor();
                                Point pt = Geometry.centerPoint(monitor.getBounds());
                                MinimalProgressMonitorDialog dialog = new MinimalProgressMonitorDialog(Display.getCurrent().getActiveShell(),rect.width,rect.width);
                                dialog.setBGImage(image);
                                dialog.setForegroundRGB(fgRGB);
                                dialog.create();
                                Shell shell = dialog.getShell();
                                shell.setLocation(shell.getLocation().x,pt.y+rect.height/2);
                                shell.addDisposeListener(new DisposeListener() {
                                    public void widgetDisposed(DisposeEvent e)
                                    {
                                        image.dispose();
                                    }
                                });
                                dialog.run(fork, cancelable, runnable);
                                return;
                            }
                        }
                    }
                    new MinimalProgressMonitorDialog(Display.getDefault().getActiveShell()).run(fork, cancelable, runnable);
                }
                else {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(fork, cancelable, runnable);
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
    }

    private void configure()
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                Shell shell = Display.getDefault().getActiveShell();
                new NSISConfigWizardDialog(shell).open();
            }
        });
    }

    public boolean isNT()
    {
        return mIsNT;
    }

    private void validateOS() throws CoreException
    {
        String[] supportedOS = Common.loadArrayProperty(getResourceBundle(),"supported.os"); //$NON-NLS-1$
        List ntOS = Common.loadListProperty(getResourceBundle(),"nt.os"); //$NON-NLS-1$
        if(!Common.isEmptyArray(supportedOS)) {
            String osName = System.getProperty("os.name"); //$NON-NLS-1$
            String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
            mIsNT = ntOS.contains(osName);
            for(int i=0; i<supportedOS.length; i++) {
                String[] tokens = Common.tokenize(supportedOS[i],'#');
                String os = tokens[0];
                Version minOSVersion = null;
                Version maxOSVersion = null;
                if(tokens.length >= 2) {
                    minOSVersion = new Version(tokens[1]);
                    if(tokens.length >= 3) {
                        maxOSVersion = new Version(tokens[3]);
                    }
                }
                if(osName.equalsIgnoreCase(os)) {
                    if(minOSVersion != null) {
                        Version version = new Version(osVersion);
                        if(version.compareTo(minOSVersion) >= 0) {
                            if(maxOSVersion != null) {
                                if(maxOSVersion.compareTo(version) >= 0) {
                                    return;
                                }
                                break;
                            }
                            return;
                        }
                        break;
                    }
                    return;
                }
            }
            String osError = getResourceString("unsupported.os.error"); //$NON-NLS-1$
            Common.openError(getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    osError, getShellImage());
            cInvalidException = osError;
            throw new CoreException(new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,osError,
                                    new RuntimeException(osError)));
        }
    }


    private void validateVM() throws CoreException
    {
        mJavaVendor = System.getProperty("java.vendor"); //$NON-NLS-1$
        if(Common.isEmpty(mJavaVendor)) {
            mJavaVendor = System.getProperty("java.vm.vendor"); //$NON-NLS-1$
        }
        String version = System.getProperty("java.version"); //$NON-NLS-1$
        if(Common.isEmpty(version)) {
            version = System.getProperty("java.vm.version"); //$NON-NLS-1$
        }
        mJavaVersion = new Version(version,"._"); //$NON-NLS-1$
        Map vms = Common.loadMapProperty(getResourceBundle(),(mIsNT?"nt.vms":"9x.vms"),'\u00FF'); //$NON-NLS-1$ //$NON-NLS-2$
        version = (String)vms.get(mJavaVendor);
        if(version != null) {
            String[] tokens = Common.tokenize(version,'-');
            if(tokens.length == 2) {
                if(mJavaVersion.compareTo(new Version(tokens[0])) >= 0 &&
                   mJavaVersion.compareTo(new Version(tokens[1])) <= 0) {
                    return;
                }
            }
            else {
                if(new Version(tokens[0]).equals(mJavaVersion)) {
                    return;
                }
            }
        }
        String vmError = getFormattedString((mIsNT?"unsupported.nt.vms.error":"unsupported.9x.vms.error"),new String[]{System.getProperty("os.name")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Common.openError(getWorkbench().getActiveWorkbenchWindow().getShell(),
                         vmError, getShellImage());
        cInvalidException = vmError;
        throw new CoreException(new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,vmError,
                                new RuntimeException(vmError)));
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
        mJobScheduler.stop();
        MakeNSISRunner.shutdown();
        NullProgressMonitor monitor = new NullProgressMonitor();
        while(mServices.size() > 0) {
            IEclipseNSISService service = (IEclipseNSISService)mServices.pop();
            service.stop(monitor);
        }
        FileMonitor.INSTANCE.stop();
        mConsole.destroy();
        mConsole = null;
        mBundleContext = null;
		super.stop(context);
	}

	public BundleContext getBundleContext()
    {
        return mBundleContext;
    }

    public NSISConsole getConsole()
    {
        return mConsole;
    }

    public JobScheduler getJobScheduler()
    {
        return mJobScheduler;
    }

    /**
	 * Returns the shared instance.
	 */
	public static EclipseNSISPlugin getDefault() {
		return cPlugin;
	}

    public static synchronized File getPluginStateLocation()
    {
        if(cStateLocation == null) {
            EclipseNSISPlugin plugin = getDefault();
            if(plugin != null) {
                cStateLocation = plugin.getStateLocation().toFile();
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
     * Returns the string from the plugin bundle's resource bundle,
     * or 'key' if not found.
     */
    public static String getBundleResourceString(String key)
    {
        return Platform.getResourceString(getDefault().getBundle(), key);
    }

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key)
   {
        return getResourceString(key, key);
	}

    public static String getResourceString(Locale locale, String key)
    {
        return getResourceString(locale, key, key);
    }

    public static String getResourceString(String key, String defaultValue)
    {
        return getResourceString(Locale.getDefault(),key,defaultValue);
    }

    public static String getResourceString(Locale locale, String key, String defaultValue)
    {
        EclipseNSISPlugin plugin = getDefault();
        if(plugin != null && key != null) {
            ResourceBundle bundle = plugin.getResourceBundle(locale);
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
    public synchronized ResourceBundle getResourceBundle(Locale locale)
    {
        if(!mResourceBundles.containsKey(locale)) {
            mResourceBundles.put(locale,new CompoundResourceBundle(locale, BUNDLE_NAMES));
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
                            NSISPreferences.INSTANCE.getPreferenceStore(),
                            INSISPreferenceConstants.CUSTOM_TEMPLATES);
            try {
                mTemplateStore.load();
            }
            catch (IOException e) {
                log(e);
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
     * @return Returns the name.
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

    public String getJavaVendor()
    {
        return mJavaVendor;
    }

    public Version getJavaVersion()
    {
        return mJavaVersion;
    }

    public boolean isConfigured()
    {
        return (NSISPreferences.INSTANCE.getNSISExe() != null);
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
                status = new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR, message==null?t.getClass().getName():message,t);
            }
            log.log(status);
        }
        else {
            t.printStackTrace();
        }
    }

    public void log(String message)
    {
        ILog log = getLog();
        if(log != null) {
            log.log(new Status(IStatus.INFO,PLUGIN_ID,IStatus.INFO,message,null));
        }
        else {
            System.out.println(message);
        }
    }

    public static Image getShellImage()
    {
        return cShellImage;
    }
}
