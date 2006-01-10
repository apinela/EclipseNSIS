/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.io.*;
import java.nio.channels.FileLock;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.filemon.FileMonitor;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ListDialog;

class NSISInstallUpdateJob extends NSISUpdateJob
{
    public static final int INSTALL_ERROR = -1;
    public static final int INSTALL_SUCCESS = 0;
    public static final int INSTALL_CANCEL = 1;
    public static final int INSTALL_ABORTED = 2;
    
    private static MessageFormat cNotifyFormat = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.complete.message")); //$NON-NLS-1$
    
    private String mVersion;
    private File mSetupExe;

    NSISInstallUpdateJob(String version, File setupExe, NSISUpdateJobSettings settings)
    {
        super(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("installing.update.message")).format(new String[]{version}), settings); //$NON-NLS-1$
        mVersion = version;
        mSetupExe = setupExe;
    }
    
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated();
    }
    
    private boolean isLocked(File file)
    {
        if(IOUtility.isValidFile(file)) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file,true);
                FileLock lock = null;
                try {
                    lock = fos.getChannel().tryLock();
                    return lock == null;
                }
                catch (IOException e) {
                    return true;
                }
                finally {
                    if(lock != null) {
                        try {
                            lock.release();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch(FileNotFoundException fnfe) {
                return true;
            }
            finally {
                IOUtility.closeIO(fos);
            }
        }
        return false;
    }

    protected IStatus doRun(final IProgressMonitor monitor)
    {
        if(IOUtility.isValidFile(mSetupExe)) {
            monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            boolean fileMonStopped = false;
            try {
                final String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
                final NSISUpdateJobSettings settings = getSettings();
                boolean automated = settings.isAutomated();
                if(!Common.isEmpty(NSISPreferences.INSTANCE.getNSISHome())) {
                    fileMonStopped = FileMonitor.INSTANCE.stop();
                    if(!Common.isEmpty(nsisHome)) {
                        final List list = new ArrayList();
                        checkForLocks(new File(nsisHome), list);
                        if(list.size() > 0) {
                            final boolean[] retry = {false};
                            Display.getDefault().syncExec(new Runnable() {
                                public void run()
                                {
                                    ListDialog dialog = new ListDialog(Display.getCurrent().getActiveShell()) {

                                        protected int getTableStyle()
                                        {
                                            return super.getTableStyle() | SWT.READ_ONLY;
                                        }
                                        
                                    };
                                    dialog.setContentProvider(new CollectionContentProvider());
                                    dialog.setLabelProvider(new LabelProvider());
                                    dialog.setTitle(EclipseNSISUpdatePlugin.getResourceString("warning.title")); //$NON-NLS-1$
                                    dialog.setMessage(EclipseNSISUpdatePlugin.getResourceString("locked.files.message")); //$NON-NLS-1$
                                    dialog.setInput(list);
                                    retry[0] = (dialog.open() == Window.OK);
                                }
                            });
                            if(!retry[0]) {
                                return Status.CANCEL_STATUS;
                            }
                            automated = false;
                        }
                    }
                }
                final List cmd = new ArrayList();
                cmd.add(mSetupExe.getAbsolutePath());
                if(automated) {
                    cmd.add("/S"); //Silent //$NON-NLS-1$
                    if(!Common.isEmpty(nsisHome)) {
                        cmd.add("/D="+nsisHome); //$NON-NLS-1$
                    }
                }
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                int rv = INSTALL_SUCCESS;
                final boolean[] terminated = { false };
                try {
                    final Process p = Runtime.getRuntime().exec((String[])cmd.toArray(Common.EMPTY_STRING_ARRAY));
                    new Thread(new Runnable() {
                        public void run()
                        {
                            while(!terminated[0]) {
                                if(monitor.isCanceled()) {
                                    displayExec(new Runnable() {
                                        public void run()
                                        {
                                            Common.openWarning(Display.getCurrent().getActiveShell(), 
                                                    EclipseNSISUpdatePlugin.getResourceString("cancel.not.supported.message"),  //$NON-NLS-1$
                                                    EclipseNSISUpdatePlugin.getShellImage());
                                        }
                                    });
                                    return;
                                }
                                try {
                                    Thread.sleep(10);
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },EclipseNSISUpdatePlugin.getResourceString("install.thread.name")).start(); //$NON-NLS-1$
                    rv = p.waitFor();
                }
                catch (Exception e) {
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                    rv = INSTALL_ERROR;
                }
                finally {
                    terminated[0] = true;
                }
                switch(rv) {
                    case INSTALL_SUCCESS:
                        mSetupExe.delete();
                        String newNSISHome = WinAPI.RegQueryStrValue(INSISConstants.NSIS_REG_ROOTKEY, INSISConstants.NSIS_REG_SUBKEY, INSISConstants.NSIS_REG_VALUE);
                        if (!Common.isEmpty(newNSISHome)) {
                            if (nsisHome == null || !newNSISHome.equalsIgnoreCase(nsisHome)) {
                                NSISPreferences.INSTANCE.setNSISHome(newNSISHome);
                                NSISPreferences.INSTANCE.store();
                            }
                        }
                        if (automated && settings.isInstall()) {
                            displayExec(new Runnable() {
                                public void run()
                                {
                                    Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"), //$NON-NLS-1$
                                            cNotifyFormat.format(new String[]{mVersion}), EclipseNSISUpdatePlugin.getShellImage());
                                }
                            });
                        }
                        break;
                    case INSTALL_CANCEL:
                        return Status.CANCEL_STATUS;
                    case INSTALL_ABORTED:
                        throw new RuntimeException(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.aborted.error")).format(new String[]{mVersion})); //$NON-NLS-1$
                    case INSTALL_ERROR:
                    default:
                        throw new RuntimeException(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.exec.error")).format(new String[]{mVersion})); //$NON-NLS-1$
                }
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
                return new Status(IStatus.ERROR,EclipseNSISUpdatePlugin.getDefault().getPluginId(),IStatus.ERROR,e.getMessage(),e);
            }
            finally {
                if(fileMonStopped) {
                    FileMonitor.INSTANCE.start();
                }
            }
        }
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

    private void checkForLocks(File folder, List list)
    {
        if(IOUtility.isValidDirectory(folder)) {
            File[] children = folder.listFiles();
            if(!Common.isEmptyArray(children)) {
                for (int i = 0; i < children.length; i++) {
                    if(children[i].isFile()) {
                        if(isLocked(children[i])) {
                            list.add(children[i].getAbsolutePath());
                        }
                    }
                    else {
                        checkForLocks(children[i], list);
                    }
                }
            }
        }
    }

    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.update.error")).format(new String[] {mVersion,e.getMessage()}); //$NON-NLS-1$
    }
}
