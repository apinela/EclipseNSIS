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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISWizardDialog extends WizardDialog
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 600;

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
            WorkbenchHelp.setHelp(getContents(),helpContextId);
        }

        Shell shell = getShell();
        Point size = shell.getSize();
        Rectangle clientArea = shell.getClientArea();
        int delX = size.x - clientArea.width;
        int delY = size.y - clientArea.height;
        shell.setSize( Math.max(SIZING_WIZARD_WIDTH, clientArea.width)+delX, Math.max(SIZING_WIZARD_HEIGHT, clientArea.height)+delY);
    }
    
    protected Control createContents(Composite parent)
    {
        // TODO Auto-generated method stub
        return super.createContents(parent);
    }
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        return super.createDialogArea(parent);
    }
}