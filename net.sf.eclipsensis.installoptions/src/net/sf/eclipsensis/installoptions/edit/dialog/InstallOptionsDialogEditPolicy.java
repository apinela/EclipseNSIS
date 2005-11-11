/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.ReorderPartCommand;
import net.sf.eclipsensis.installoptions.requests.ReorderPartRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

public class InstallOptionsDialogEditPolicy extends ContainerEditPolicy implements IInstallOptionsConstants
{
    private EditPart mEditPart;

    public InstallOptionsDialogEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }

    protected Command getCreateCommand(CreateRequest request)
    {
        return null;
    }

    public Command getCommand(Request request)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            if(REQ_REORDER_PART.equals(request.getType())) {
                return getReorderPartCommand((ReorderPartRequest)request);
            }
            else {
                return super.getCommand(request);
            }
        }
    }

    protected Command getReorderPartCommand(ReorderPartRequest request)
    {
        EditPart editpart = request.getEditPart();
        List children = getHost().getChildren();
        int newIndex = request.getNewIndex();
        int oldIndex = children.indexOf(editpart);
        if (oldIndex == newIndex) {
            return UnexecutableCommand.INSTANCE;
        }
        return new ReorderPartCommand((InstallOptionsWidget)editpart.getModel(),
                (InstallOptionsDialog)getHost().getModel(), oldIndex, newIndex);
    }
}
