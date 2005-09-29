/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsDirectEditPolicy;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.model.InstallOptionsUneditableElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsUneditableElementCommand;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DirectEditRequest;

public class UneditableElementDirectEditPolicy extends InstallOptionsDirectEditPolicy
{
    public UneditableElementDirectEditPolicy(EditPart editPart)
    {
        super(editPart);
    }
    
    public Command getCommand(Request request)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            return super.getCommand(request);
        }
    }

    protected Command getDirectEditCommand(DirectEditRequest edit) 
    {
        String text = (String)edit.getCellEditor().getValue();
        InstallOptionsUneditableElementEditPart control = (InstallOptionsUneditableElementEditPart)getHost();
        InstallOptionsUneditableElementCommand command = new InstallOptionsUneditableElementCommand((InstallOptionsUneditableElement)control.getModel(),text);
        return command;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
     */
    protected void showCurrentEditValue(DirectEditRequest request)
    {
    }
}
