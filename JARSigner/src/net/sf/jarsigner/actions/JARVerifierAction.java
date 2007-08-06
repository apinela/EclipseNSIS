/*******************************************************************************
 * Copyright (c) 2005-2007 Sunil Kamath (IcemanK).
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
import net.sf.jarsigner.dialogs.JARVerifierOptionsDialog;
import net.sf.jarsigner.util.JARVerifier;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

public class JARVerifierAction implements IObjectActionDelegate
{
    ISelection mSelection = null;
    IWorkbenchPart mPart = null;

	/**
	 * Constructor for JNIGenAction.
	 */
	public JARVerifierAction() {
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
        if ((mSelection != null) && !mSelection.isEmpty() && (mSelection instanceof IStructuredSelection)) {
            IStructuredSelection sel = (IStructuredSelection)mSelection;
            try {
                JARVerifierOptionsDialog dialog = new JARVerifierOptionsDialog(Display.getDefault().getActiveShell(),sel.toList());
                if(dialog.open() == Window.OK) {
                    final JARVerifier jarVerifier = new JARVerifier(dialog.getVMInstall(), dialog.getToolsJar(),sel.toList());
                    jarVerifier.setVerbose(dialog.isVerbose());
                    jarVerifier.setCerts(dialog.isCerts());
                    jarVerifier.setKeyStore(dialog.getKeyStore());
                    jarVerifier.setIgnoreErrors(dialog.isIgnoreErrors());
                    UtilitiesPlugin.getDefault().getJobScheduler().scheduleJob(getClass(),
                            JARSignerPlugin.getResourceString("jarverifier.job.title"), //$NON-NLS-1$
                            new IJobStatusRunnable() {
                                public IStatus run(IProgressMonitor monitor)
                                {
                                    return jarVerifier.run(monitor);
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
