/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.checkbox;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy;
import net.sf.eclipsensis.installoptions.model.InstallOptionsCheckBox;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.commands.ToggleStateCommand;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

public class InstallOptionsCheckBoxExtendedEditPolicy extends InstallOptionsExtendedEditPolicy
{
    public InstallOptionsCheckBoxExtendedEditPolicy(EditPart editPart)
    {
        super(editPart);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy#getExtendedEditCommand(net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest)
     */
    protected Command getExtendedEditCommand(ExtendedEditRequest request)
    {
        ToggleStateCommand command = new ToggleStateCommand((InstallOptionsCheckBox)request.getEditPart().getModel(),
                                                                                    (String)request.getNewValue());
        return command;
    }

    protected String getExtendedEditProperty()
    {
        return InstallOptionsModel.PROPERTY_STATE;
    }
}
