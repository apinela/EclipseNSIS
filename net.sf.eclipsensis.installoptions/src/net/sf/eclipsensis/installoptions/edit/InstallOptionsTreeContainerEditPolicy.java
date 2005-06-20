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

import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.CreateCommand;
import net.sf.eclipsensis.installoptions.model.commands.ReorderPartCommand;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

public class InstallOptionsTreeContainerEditPolicy extends TreeContainerEditPolicy
{
    private EditPart mEditPart;
    
    public InstallOptionsTreeContainerEditPolicy(EditPart editPart)
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
            return super.getCommand(request);
        }
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
     */
    protected Command getAddCommand(ChangeBoundsRequest request)
    {
        return null;
    }

    protected Command createCreateCommand(InstallOptionsWidget child, Rectangle r, int index, String label)
    {
        CreateCommand cmd = new CreateCommand();
        Rectangle rect;
        if (r == null) {
            rect = new Rectangle();
            rect.setSize(new Dimension(-1, -1));
        }
        else {
            rect = r;
        }
        cmd.setLocation(rect);
        cmd.setParent((InstallOptionsDialog)getHost().getModel());
        cmd.setChild(child);
        cmd.setLabel(label);
        if (index >= 0) {
            cmd.setIndex(index);
        }
        return cmd;
    }

    protected Command getCreateCommand(CreateRequest request)
    {
        InstallOptionsWidget child = (InstallOptionsWidget)request.getNewObject();
        int index = findIndexOfTreeItemAt(request.getLocation());
        return createCreateCommand(child, null, index, "Create InstallOptionsWidget");//$NON-NLS-1$
    }

    protected Command getMoveChildrenCommand(ChangeBoundsRequest request)
    {
        CompoundCommand command = new CompoundCommand();
        List editparts = request.getEditParts();
        List children = getHost().getChildren();
        int newIndex = findIndexOfTreeItemAt(request.getLocation());

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = (EditPart)editparts.get(i);
            int tempIndex = newIndex;
            int oldIndex = children.indexOf(child);
            if (oldIndex == tempIndex || oldIndex + 1 == tempIndex) {
                command.add(UnexecutableCommand.INSTANCE);
                return command;
            }
            else if (oldIndex < tempIndex) {
                tempIndex--;
            }
            command.add(new ReorderPartCommand((InstallOptionsWidget)child.getModel(),
                    (InstallOptionsDialog)getHost().getModel(), oldIndex, tempIndex));
        }
        return command;
    }
}
