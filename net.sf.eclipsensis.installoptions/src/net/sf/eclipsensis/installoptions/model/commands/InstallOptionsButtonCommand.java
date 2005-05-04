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

import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;

import org.eclipse.gef.commands.Command;

public class InstallOptionsButtonCommand extends Command
{
    private String mNewText;
    private String mOldText;
    private InstallOptionsButton mButton;

    public InstallOptionsButtonCommand(InstallOptionsButton button, String text) 
    {
        mButton = button;
        if (text != null) {
            mNewText = text;
        }
        else {
            mNewText = "";  //$NON-NLS-1$
        }
        mOldText = mButton.getText();
    }

    public void execute() {
        mButton.setText(mNewText);
    }

    public void undo() {
        mButton.setText(mOldText);
    }
}
