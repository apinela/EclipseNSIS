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

import net.sf.eclipsensis.wizard.NSISScriptWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class NSISWizardAction extends NSISScriptAction
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 600;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        PreferenceManager manager = workbench.getPreferenceManager();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        WizardDialog dialog = new WizardDialog(shell,new NSISScriptWizard());
        dialog.create();
        Point size = dialog.getShell().getSize();
        Rectangle clientArea = dialog.getShell().getClientArea();
        int delX = size.x - clientArea.width;
        int delY = size.y - clientArea.height;
        dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, clientArea.width)+delX, Math.max(SIZING_WIZARD_HEIGHT, clientArea.height)+delY);
        dialog.open();
    }
}
