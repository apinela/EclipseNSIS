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
import net.sf.eclipsensis.installoptions.model.InstallOptionsCheckBox;

import org.eclipse.gef.commands.Command;

public class ToggleStateCommand extends Command
{
    private String mNewState;
    private String mOldState;
    private InstallOptionsCheckBox mModel;

    public ToggleStateCommand(InstallOptionsCheckBox model, String state) 
    {
        mModel = model;
        setLabel(InstallOptionsPlugin.getFormattedString("toggle.state.command.label", new Object[]{mModel.getType()})); //$NON-NLS-1$
        if (state != null) {
            mNewState = state;
        }
        else {
            mNewState = "";  //$NON-NLS-1$
        }
        mOldState = mModel.getState();
    }

    public void execute() {
        mModel.setState(mNewState);
    }

    public void undo() {
        mModel.setState(mOldState);
    }
}