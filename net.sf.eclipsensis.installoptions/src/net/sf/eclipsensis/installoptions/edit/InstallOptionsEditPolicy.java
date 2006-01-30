/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.DeleteCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class InstallOptionsEditPolicy extends ComponentEditPolicy
{
    protected Command createDeleteCommand(GroupRequest request)
    {
        Object parent = getHost().getParent().getModel();
        DeleteCommand deleteCmd = new DeleteCommand();
        deleteCmd.setParent((InstallOptionsDialog)parent);
        deleteCmd.setChild((InstallOptionsWidget)getHost().getModel());
        return deleteCmd;
    }
}