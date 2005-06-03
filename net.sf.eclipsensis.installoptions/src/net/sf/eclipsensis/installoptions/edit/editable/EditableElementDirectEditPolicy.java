/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.editable;

import net.sf.eclipsensis.installoptions.model.InstallOptionsEditableElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsEditableElementCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

public class EditableElementDirectEditPolicy extends DirectEditPolicy
{
    /**
     * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
     */
    protected Command getDirectEditCommand(DirectEditRequest edit) 
    {
        String text = (String)edit.getCellEditor().getValue();
        InstallOptionsEditableElementEditPart control = (InstallOptionsEditableElementEditPart)getHost();
        InstallOptionsEditableElementCommand command = new InstallOptionsEditableElementCommand((InstallOptionsEditableElement)control.getModel(),text);
        return command;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
     */
    protected void showCurrentEditValue(DirectEditRequest request)
    {
    }
}
