/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.NSISWizardDialog;
import net.sf.eclipsensis.wizard.NSISScriptWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class NSISWizardAction extends NSISAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        final Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        final NSISWizardDialog[] wizardDialog = new NSISWizardDialog[1];
        BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
            public void run()
            {
                try {
                    wizardDialog[0] = new NSISWizardDialog(shell, new NSISScriptWizard());
                    wizardDialog[0].create();
                }
                catch (Exception e) {
                    wizardDialog[0] = null;
                    EclipseNSISPlugin.getDefault().log(e);
                }                
            }
        });
        if(wizardDialog[0] != null) {
            wizardDialog[0].open();
        }
    }

    public boolean isEnabled()
    {
        return (mPlugin != null && mPlugin.isConfigured());
    }
}
