/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplate;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplateDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class EditTemplateAction extends Action
{
    public static final String ID = "net.sf.eclipsensis.installoptions.edit_template";
    
    private InstallOptionsTemplate mTemplate;
    /**
     * @param part
     */
    public EditTemplateAction(InstallOptionsTemplate template)
    {
        super();
        setText(InstallOptionsPlugin.getResourceString("edit.template.action.label"));
        setToolTipText(InstallOptionsPlugin.getResourceString("edit.template.action.tooltip")); //$NON-NLS-1$
        setId(ID);
        mTemplate = template;
        setEnabled(mTemplate != null);
    }

    public void run() 
    {
        InstallOptionsTemplateDialog dialog = new InstallOptionsTemplateDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), mTemplate);
        dialog.open();
    }
}
