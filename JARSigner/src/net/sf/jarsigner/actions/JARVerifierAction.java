/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.actions;

import net.sf.jarsigner.JARSignerPlugin;
import net.sf.jarsigner.dialogs.JARVerifierOptionsDialog;
import net.sf.jarsigner.util.JARVerifier;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
        if (mSelection != null && !mSelection.isEmpty() && mSelection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection)mSelection;
            try {
                JARVerifierOptionsDialog dialog = new JARVerifierOptionsDialog(Display.getDefault().getActiveShell(),sel.size() > 1);
                if(dialog.open() == Window.OK) {
                    final JARVerifier jarVerifier = new JARVerifier(dialog.getToolsJar(),sel.toList());
                    jarVerifier.setVerbose(dialog.isVerbose());
                    jarVerifier.setCerts(dialog.isCerts());
                    jarVerifier.setKeyStore(dialog.getKeyStore());
                    jarVerifier.setIgnoreErrors(dialog.isIgnoreErrors());
                    Job job = new Job(JARSignerPlugin.getResourceString("jarverifier.job.title")) { //$NON-NLS-1$
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            jarVerifier.run(monitor);
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule();
                }
            }
            catch(Exception e) {
                MessageDialog.openError(Display.getDefault().getActiveShell(),JARSignerPlugin.getResourceString("error.title"),e.getMessage()); //$NON-NLS-1$
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
