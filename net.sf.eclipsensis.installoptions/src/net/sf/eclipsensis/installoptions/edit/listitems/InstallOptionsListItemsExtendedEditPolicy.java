/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listitems;

import java.util.List;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy;
import net.sf.eclipsensis.installoptions.model.InstallOptionsListItems;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.commands.ModifyListItemsCommand;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

public class InstallOptionsListItemsExtendedEditPolicy extends InstallOptionsExtendedEditPolicy
{
    public InstallOptionsListItemsExtendedEditPolicy(EditPart editPart)
    {
        super(editPart);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy#getExtendedEditCommand(net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest)
     */
    protected Command getExtendedEditCommand(ExtendedEditRequest request)
    {
        ModifyListItemsCommand command = new ModifyListItemsCommand((InstallOptionsListItems)request.getEditPart().getModel(),
                                                                              (List)request.getNewValue());
        return command;
    }

    protected String getExtendedEditProperty()
    {
        return InstallOptionsModel.PROPERTY_LISTITEMS;
    }
}
