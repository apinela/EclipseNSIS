/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class NSISWizardDialog extends WizardDialog
{
    /**
     * @param parentShell
     * @param newWizard
     */
    public NSISWizardDialog(Shell parentShell, IWizard newWizard)
    {
        super(parentShell, newWizard);
    }

    public void create()
    {
        super.create();
        NSISWizard wiz = (NSISWizard)getWizard();
        String helpContextId = wiz.getHelpContextId();
        if(helpContextId != null) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getContents(),helpContextId);
        }
    }
}
