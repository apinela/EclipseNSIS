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

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPolicy;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

public class InstallOptionsButtonEditPolicy extends InstallOptionsWidgetEditPolicy
{
    public Command getCommand(Request request) {
        return super.getCommand(request);
    }

    public EditPart getTargetEditPart(Request request) {
        return super.getTargetEditPart(request);
    }
}
