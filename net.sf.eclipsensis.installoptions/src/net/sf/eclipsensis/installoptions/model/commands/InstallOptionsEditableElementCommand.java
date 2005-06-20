/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsEditableElement;

import org.eclipse.gef.commands.Command;

public class InstallOptionsEditableElementCommand extends Command
{
    private String mNewState;
    private String mOldState;
    private InstallOptionsEditableElement mEditable;

    public InstallOptionsEditableElementCommand(InstallOptionsEditableElement editable, String state) 
    {
        mEditable = editable;
        setLabel(InstallOptionsPlugin.getFormattedString("editable.element.command.label",  //$NON-NLS-1$
                                                         new Object[]{mEditable.getType()}));
        if (state != null) {
            mNewState = state;
        }
        else {
            mNewState = "";  //$NON-NLS-1$
        }
        mOldState = mEditable.getState();
    }

    public void execute() {
        mEditable.setState(mNewState);
    }

    public void undo() {
        mEditable.setState(mOldState);
    }
}
