/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class NSISWizardAction extends NSISScriptAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        PreferenceManager manager = workbench.getPreferenceManager();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        new WizardDialog(shell,new NSISWizard()).open();
    }
}
