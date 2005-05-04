/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.button;

import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsButtonCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

public class ButtonDirectEditPolicy extends DirectEditPolicy
{
    /**
     * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
     */
    protected Command getDirectEditCommand(DirectEditRequest edit) 
    {
        String labelText = (String)edit.getCellEditor().getValue();
        InstallOptionsButtonEditPart label = (InstallOptionsButtonEditPart)getHost();
        InstallOptionsButtonCommand command = new InstallOptionsButtonCommand((InstallOptionsButton)label.getModel(),labelText);
        return command;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
     */
    protected void showCurrentEditValue(DirectEditRequest request)
    {
    }
}
