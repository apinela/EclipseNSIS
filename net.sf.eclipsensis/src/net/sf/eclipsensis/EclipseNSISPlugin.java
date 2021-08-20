/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import net.sf.eclipsensis.console.NSISConsole;
import net.sf.eclipsensis.dialogs.MinimalProgressMonitorDialog;
import net.sf.eclipsensis.dialogs.NSISConfigWizardDialog;
import net.sf.eclipsensis.editor.template.NSISTemplateContextType;
import net.sf.eclipsensis.filemon.FileMonitor;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISEditorPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.CompoundResourceBundle;
import net.sf.eclipsensis.util.DisplayUpdateProgressMonitor;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.util.NSISValidator;
import net.sf.eclipsensis.util.NestedProgressMonitor;
import net.sf.eclipsensis.util.Version;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseNSISPlugin extends AbstractUIPlugin implements INSISConstants {

    private static EclipseNSISPlugin cPlugin;
    private static File cStateLocation = null;
    private static String cInvalidException = null;
    private static Image cShellImage;

    private BundleContext mBundleContext = null;
    private String mName = null;
    private Version mVersion = null;
    private TemplateStore mTemplateStore;
    private ContributionContextTypeRegistry mContextTypeRegistry;
    private Locale mLocale;
    private Map<Locale, CompoundResourceBundle> mResourceBundles = new HashMap<>();
    public static final String[] BUNDLE_NAMES = new String[] {
            RESOURCE_BUNDLE, MESSAGE_BUNDLE};
    private ImageManager mImageManager;
    private boolean mIsNT = false;
    private boolean mIs2K = false;
    private boolean mIsVista = false;
    private boolean mIsWin7 = false;
    private boolean mIsX64 = false;
    private String mJavaVendor;
    private Version mJavaVersion;
    private Stack<IEclipseNSISService> mServices = new Stack<>();
    private JobScheduler mJobScheduler = new JobScheduler();
    private IEclipsePreferences mPreferences;

    private NSISConsole mConsole = null;

    /**
     * The constructor.
     */
    public EclipseNSISPlugin() {

        super();
        cPlugin = this;
        mLocale = Locale.getDefault();

        try {

            mResourceBundles.put(mLocale, new CompoundResourceBundle(mLocale, BUNDLE_NAMES));
        } catch (MissingResourceException x) {

            log(x);
        }
        mImageManager = new ImageManager(this);
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);
        mBundleContext = context;

        if (!isDebugging()) {

            if (Boolean.getBoolean("net.sf.eclipsensis/debug")) { //$NON-NLS-1$

                setDebugging(true);
            }
        }
        cShellImage = mImageManager.getImage(getResourceString("nsis.icon")); //$NON-NLS-1$
        mName = (String) getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mPreferences = new InstanceScope().getNode(mName);
        mVersion = new Version((String) getBundle().getHeaders().get("Bundle-Version")); //$NON-NLS-1$

        if (cInvalidException != null) {

            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, cInvalidException, new RuntimeException(cInvalidException)));
        }
        validateOS();
        validateVM();
        startEclipseNSIS();
    }

    private void startEclipseNSIS() {

        if (!isConfigured()) {

            // First try autoconfigure
            NSISPreferences.getInstance().setNSISHome(NSISValidator.getRegistryNSISHome());

            if (!isConfigured()) {

                final IWorkbenchWindow wwindow = getWorkbench().getActiveWorkbenchWindow();
                final Runnable configOp = new Runnable() {

                    @Override
                    public void run() {

                        Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();

                        if (Common.openConfirm(shell, getResourceString("unconfigured.confirm"), getShellImage())) { //$NON-NLS-1$

                            configure();
                        }

                        if (!isConfigured()) {

                            Common.openWarning(shell, getResourceString("unconfigured.warning"), getShellImage()); //$NON-NLS-1$
                        }
                    }
                };

                if (wwindow != null && wwindow.getShell().isVisible()) {

                    if (!Boolean.getBoolean("net.sf.eclipsensis.config.IsConfiguring"))  //$NON-NLS-1$
                    {

                        configOp.run();
                    }
                }
                else {

                    getWorkbench().addWindowListener(new IWindowListener() {

                        private void schedule() {

                            getJobScheduler().scheduleUIJob(EclipseNSISPlugin.getResourceString("starting.eclipsensis.message"), (monitor) -> {

                                configOp.run();
                                return Status.OK_STATUS;
                            });
                        }

                        @Override
                        public void windowActivated(IWorkbenchWindow window) {

                            if (window == wwindow && window.getShell().isVisible()) {

                                getWorkbench().removeWindowListener(this);
                                schedule();
                            }
                        }

                        @Override
                        public void windowDeactivated(IWorkbenchWindow window) {}

                        @Override
                        public void windowClosed(IWorkbenchWindow window) {

                            if (window == wwindow) {

                                getWorkbench().removeWindowListener(this);
                            }
                        }

                        @Override
                        public void windowOpened(IWorkbenchWindow window) {

                            if (window == wwindow && window.getShell().isVisible()) {

                                getWorkbench().removeWindowListener(this);
                                schedule();
                            }
                        }
                    });
                }
            }
            else {

                NSISPreferences.getInstance().store();
            }
        }

        FileMonitor.INSTANCE.start();
        startServices();
        mJobScheduler.start();
        mConsole = new NSISConsole();
    }

    private void startServices() {

        final String[] services = Common.loadArrayProperty(getResourceBundle(), "services"); //$NON-NLS-1$

        if (!Common.isEmptyArray(services)) {

            final IRunnableWithProgress op = (monitor) -> {

                IProgressMonitor monitor2 = new DisplayUpdateProgressMonitor(monitor);

                try {

                    String taskName = EclipseNSISPlugin.getResourceString("starting.eclipsensis.message"); //$NON-NLS-1$
                    monitor2.beginTask(taskName, services.length + 1);

                    for (int i = 0; i < services.length; i++) {

                        NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor2, taskName, 1);

                        try {

                            Object obj = Common.createDefaultObject(services[i]);

                            if (obj instanceof IEclipseNSISService) {

                                IEclipseNSISService service = (IEclipseNSISService) obj;
                                service.start(subMonitor);
                                mServices.push(service);
                            }
                        } catch (Exception e) {

                            log(e);
                        } finally {

                            subMonitor.done();
                        }
                    }
                    monitor2.subTask(EclipseNSISPlugin.getResourceString("starting.makensis.message"));
                    Runnable runnable = () -> {

                        MakeNSISRunner.startup();
                    };

                    if (Display.getCurrent() == null) {

                        Display.getDefault().syncExec(runnable);
                    }
                    else {

                        runnable.run();
                    }

                    monitor2.worked(1);
                } catch (Exception e) {

                    e.printStackTrace();
                } finally {

                    monitor.done();
                }
            };

            run(false, false, op);
        }
    }

    public void registerService(IEclipseNSISService service) {

        if (!mServices.contains(service)) {

            if (!service.isStarted()) {

                service.start(new NullProgressMonitor());
            }
            mServices.push(service);
        }
    }

    public void run(final boolean fork, final boolean cancelable, final IRunnableWithProgress runnable) {

        try {

            if (Display.getCurrent() == null) {

                // fork and cancelable are meaningless here
                runnable.run(new NullProgressMonitor());
            }
            else {

                boolean useWorkbenchWindow = false;

                try {

                    useWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().isVisible();
                } catch (Exception e) {

                    useWorkbenchWindow = false;
                }

                if (!useWorkbenchWindow) {

                    if (mBundleContext != null && mBundleContext.getBundle().getState() == Bundle.STARTING) {

                        // Startup in progress- overlay the splash screen
                        String splashFile = System.getProperty("org.eclipse.equinox.launcher.splash.location"); //$NON-NLS-1$

                        if (!Common.isEmpty(splashFile)) {

                            File file = new File(splashFile);

                            if (IOUtility.isValidFile(file)) {

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
                                MinimalProgressMonitorDialog dialog =
                                        new MinimalProgressMonitorDialog(Display.getCurrent().getActiveShell(), rect.width, rect.width);
                                dialog.setBGImage(image);
                                dialog.setForegroundRGB(fgRGB);
                                dialog.create();
                                Shell shell = dialog.getShell();
                                shell.setLocation(shell.getLocation().x, pt.y + rect.height / 2);
                                shell.addDisposeListener(new DisposeListener() {

                                    @Override
                                    public void widgetDisposed(DisposeEvent e) {

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
        } catch (Exception e) {

            log(e);
        }
    }

    private void configure() {

        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {

                Shell shell = Display.getDefault().getActiveShell();
                new NSISConfigWizardDialog(shell).open();
            }
        });
    }

    public boolean isWinNT() {

        return mIsNT;
    }

    public boolean isWin2K() {

        return mIs2K;
    }

    public boolean isWinVista() {

        return mIsVista;
    }

    public boolean isWin7() {

        return mIsWin7;
    }

    public boolean isX64() {

        return mIsX64;
    }

    private void validateOS() throws CoreException {

        String[] supportedOS = Common.loadArrayProperty(getResourceBundle(), "supported.os"); //$NON-NLS-1$
        List<String> winNTOS = Common.loadListProperty(getResourceBundle(), "nt.os"); //$NON-NLS-1$
        List<String> win2KOS = Common.loadListProperty(getResourceBundle(), "2K.os"); //$NON-NLS-1$
        List<String> winVistaOS = Common.loadListProperty(getResourceBundle(), "vista.os"); //$NON-NLS-1$
        List<String> win7OS = Common.loadListProperty(getResourceBundle(), "win7.os"); //$NON-NLS-1$

        if (!Common.isEmptyArray(supportedOS)) {

            String osName = System.getProperty("os.name"); //$NON-NLS-1$
            mIsNT = winNTOS.contains(osName);
            mIs2K = win2KOS.contains(osName);
            mIsVista = winVistaOS.contains(osName);
            mIsWin7 = true;
            String arch = System.getProperty("sun.arch.data.model");
            mIsX64 = "64".equals(arch);
        }
    }


    private void validateVM() throws CoreException {

        mJavaVendor = System.getProperty("java.vendor"); //$NON-NLS-1$

        if (Common.isEmpty(mJavaVendor)) {

            mJavaVendor = System.getProperty("java.vm.vendor"); //$NON-NLS-1$
        }
        String version = System.getProperty("java.version"); //$NON-NLS-1$

        if (Common.isEmpty(version)) {

            version = System.getProperty("java.vm.version"); //$NON-NLS-1$
        }
        mJavaVersion = new Version(version, "._"); //$NON-NLS-1$
    }

    public static ImageManager getImageManager() {

        return getDefault().mImageManager;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        mJobScheduler.stop();
        MakeNSISRunner.shutdown();
        NullProgressMonitor monitor = new NullProgressMonitor();

        while (mServices.size() > 0) {

            IEclipseNSISService service = mServices.pop();
            service.stop(monitor);
        }
        FileMonitor.INSTANCE.stop();
        mConsole.destroy();
        mConsole = null;
        mBundleContext = null;
        super.stop(context);
    }

    public BundleContext getBundleContext() {

        return mBundleContext;
    }

    public NSISConsole getConsole() {

        return mConsole;
    }

    public JobScheduler getJobScheduler() {

        return mJobScheduler;
    }

    /**
     * Returns the shared instance.
     */
    public static EclipseNSISPlugin getDefault() {

        return cPlugin;
    }

    public static synchronized File getPluginStateLocation() {

        if (cStateLocation == null) {

            EclipseNSISPlugin plugin = getDefault();

            if (plugin != null) {

                cStateLocation = plugin.getStateLocation().toFile();
            }
        }
        return cStateLocation;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getFormattedString(String key, Object[] args) {

        return MessageFormat.format(getResourceString(key), args);
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getFormattedString(String key, Object arg) {

        return getFormattedString(key, new Object[] {
                arg});
    }

    /**
     * Returns the string from the plugin bundle's resource bundle,
     * or 'key' if not found.
     */
    public static String getBundleResourceString(String key) {

        return Platform.getResourceString(getDefault().getBundle(), key);
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) {

        return getResourceString(key, key);
    }

    public static String getResourceString(Locale locale, String key) {

        return getResourceString(locale, key, key);
    }

    public static String getResourceString(String key, String defaultValue) {

        return getResourceString(Locale.getDefault(), key, defaultValue);
    }

    public static String getResourceString(Locale locale, String key, String defaultValue) {

        EclipseNSISPlugin plugin = getDefault();

        if (plugin != null && key != null) {

            ResourceBundle bundle = plugin.getResourceBundle(locale);

            try {

                return bundle != null ? bundle.getString(key) : defaultValue;
            } catch (MissingResourceException e) {

            }
        }
        return defaultValue;
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {

        return getResourceBundle(mLocale);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public synchronized ResourceBundle getResourceBundle(Locale locale) {

        if (!mResourceBundles.containsKey(locale)) {

            mResourceBundles.put(locale, new CompoundResourceBundle(locale, BUNDLE_NAMES));
        }
        return mResourceBundles.get(locale);
    }

    /**
     * Returns this plug-in's template store.
     *
     * @return the template store of this plug-in instance
     */
    public TemplateStore getTemplateStore() {

        if (mTemplateStore == null) {

            mTemplateStore = new ContributionTemplateStore(getContextTypeRegistry(), NSISPreferences.getInstance().getPreferenceStore(),
                    INSISEditorPreferenceConstants.CUSTOM_TEMPLATES);

            try {

                mTemplateStore.load();
            } catch (IOException e) {

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
    public ContextTypeRegistry getContextTypeRegistry() {

        if (mContextTypeRegistry == null) {

            mContextTypeRegistry = new ContributionContextTypeRegistry();
            mContextTypeRegistry.addContextType(NSISTemplateContextType.NSIS_TEMPLATE_CONTEXT_TYPE);
        }
        return mContextTypeRegistry;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {

        return mName;
    }

    /**
     * @return Returns the version.
     */
    public Version getVersion() {

        return mVersion;
    }

    public String getJavaVendor() {

        return mJavaVendor;
    }

    public Version getJavaVersion() {

        return mJavaVersion;
    }

    public boolean isConfigured() {

        return NSISPreferences.getInstance().getNSISHome() != null;
    }

    public void log(Throwable t) {

        ILog log = getLog();

        if (log != null) {

            IStatus status;

            if (t instanceof CoreException) {

                status = ((CoreException) t).getStatus();
            }
            else {

                String message = t.getMessage();
                status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message == null ? t.getClass().getName() : message, t);
            }
            log.log(status);
        }
        else {

            t.printStackTrace();
        }
    }

    public void log(String message) {

        ILog log = getLog();

        if (log != null) {

            log.log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null));
        }
        else {

            System.out.println(message);
        }
    }

    public static Image getShellImage() {

        return cShellImage;
    }

    public void savePreferences() {

        try {

            mPreferences.flush();
        } catch (BackingStoreException e) {

            log(e);
        }
    }
}
