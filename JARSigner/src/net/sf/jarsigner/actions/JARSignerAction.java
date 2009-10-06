/*******************************************************************************
 * Copyright (c) 2005-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.actions;

import net.sf.eclipsensis.utilities.UtilitiesPlugin;
import net.sf.eclipsensis.utilities.job.IJobStatusRunnable;
import net.sf.jarsigner.JARSignerPlugin;
import net.sf.jarsigner.dialogs.JARSignerOptionsDialog;
import net.sf.jarsigner.util.JARSigner;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

public class JARSignerAction implements IObjectActionDelegate
{
    ISelection mSelection = null;
    IWorkbenchPart mPart = null;

    /**
     * Constructor for JNIGenAction.
     */
    public JARSignerAction() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        mPart = targetPart;
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action)
    {
        if (mSelection != null && !mSelection.isEmpty() && mSelection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection)mSelection;
            try {
                JARSignerOptionsDialog dialog = new JARSignerOptionsDialog(Display.getDefault().getActiveShell(),sel.toList());
                if(dialog.open() == Window.OK) {
                    final JARSigner jarSigner = new JARSigner(dialog.getVMInstall(), dialog.getToolsJar(),sel.toList(),
                            dialog.getKeyStore(),dialog.getStorePass(),dialog.getAlias());
                    jarSigner.setInternalSF(dialog.isInternalSF());
                    jarSigner.setKeyPass(dialog.getKeyPass());
                    jarSigner.setSectionsOnly(dialog.isSectionsOnly());
                    jarSigner.setSigFile(dialog.getSigFile());
                    jarSigner.setSignedJar(dialog.getSignedJar());
                    jarSigner.setVerbose(dialog.isVerbose());
                    jarSigner.setStoreType(dialog.getStoreType());
                    jarSigner.setIgnoreErrors(dialog.isIgnoreErrors());
                    if(dialog.isSupportsTimestamping())
                    {
                        jarSigner.setUseTimestamping(dialog.isUseTimestamping());
                        if(dialog.isTSACertOption())
                        {
                            jarSigner.setTSACert(dialog.getTSACert());
                        }
                        else
                        {
                            jarSigner.setTSA(dialog.getTSA());
                        }
                    }
                    if(dialog.isSupportsAltSigning())
                    {
                        jarSigner.setUseAltSigning(dialog.isUseAltSigning());
                        jarSigner.setAltSigner(dialog.getAltSigner());
                        jarSigner.setAltSignerPath(dialog.getAltSignerPath());
                    }
                    UtilitiesPlugin.getDefault().getJobScheduler().scheduleJob(getClass(),
                            JARSignerPlugin.getResourceString("jarsigner.job.title"), //$NON-NLS-1$
                            new IJobStatusRunnable() {
                        public IStatus run(IProgressMonitor monitor)
                        {
                            return jarSigner.run(monitor);
                        }
                    });
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                MessageDialog.openError(Display.getDefault().getActiveShell(),UtilitiesPlugin.getResourceString("error.title"),e.getMessage()); //$NON-NLS-1$
            }
        }
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        mSelection = selection;
    }

}
