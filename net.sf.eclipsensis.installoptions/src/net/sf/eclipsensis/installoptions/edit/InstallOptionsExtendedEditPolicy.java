/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;

public abstract class InstallOptionsExtendedEditPolicy extends GraphicalEditPolicy
{
    public static final String ROLE = "ExtendedEditPolicy"; //$NON-NLS-1$
    private EditPart mEditPart;
    
    public InstallOptionsExtendedEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }
    
    
    public Command getCommand(Request request)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            if (IInstallOptionsConstants.REQ_EXTENDED_EDIT == request.getType()) {
                return getExtendedEditCommand((ExtendedEditRequest)request);
            }
            else {
                return super.getCommand(request);
            }
        }
    }

    public boolean understandsRequest(Request request) 
    {
        if (IInstallOptionsConstants.REQ_EXTENDED_EDIT.equals(request.getType())) {
            if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
                return false;
            }
            else {
                return true;
            }
        }
        return super.understandsRequest(request);
    }

    protected abstract Command getExtendedEditCommand(ExtendedEditRequest request);
}
