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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;

public class InstallOptionsTreeEditPolicy extends AbstractEditPolicy
{
    private EditPart mEditPart;

    public InstallOptionsTreeEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }

    public Command getCommand(Request req)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            if (REQ_MOVE.equals(req.getType())) {
                return getMoveCommand(req);
            }
            return null;
        }
    }

    protected Command getMoveCommand(Request req){
        EditPart parent = getHost().getParent();
        if(parent != null){
            req.setType(REQ_MOVE_CHILDREN);
            Command cmd = parent.getCommand(req);
            req.setType(REQ_MOVE);
            return cmd;
        }
        else {
            return UnexecutableCommand.INSTANCE;
        }
    }
}
