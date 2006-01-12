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

import net.sf.eclipsensis.config.NSISConfigSelectionWizard;

import org.eclipse.swt.widgets.Shell;

public class NSISConfigWizardDialog extends AbstractNSISWizardDialog
{
    public NSISConfigWizardDialog(Shell parentShell)
    {
        super(parentShell, new NSISConfigSelectionWizard());
    }

    protected String getHelpContextId()
    {
        // TODO Set help context id
        return null;
    }
}