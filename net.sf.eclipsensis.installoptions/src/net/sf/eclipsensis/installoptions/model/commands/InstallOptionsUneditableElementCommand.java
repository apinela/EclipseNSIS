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
import net.sf.eclipsensis.installoptions.model.InstallOptionsUneditableElement;

import org.eclipse.gef.commands.Command;

public class InstallOptionsUneditableElementCommand extends Command
{
    private String mNewText;
    private String mOldText;
    private InstallOptionsUneditableElement mUneditable;

    public InstallOptionsUneditableElementCommand(InstallOptionsUneditableElement uneditable, String text) 
    {
        mUneditable = uneditable;
        setLabel(InstallOptionsPlugin.getFormattedString("uneditable.element.command.label", 
                                                         new Object[]{mUneditable.getType()}));
        if (text != null) {
            mNewText = text;
        }
        else {
            mNewText = "";  //$NON-NLS-1$
        }
        mOldText = mUneditable.getText();
    }

    public void execute() {
        mUneditable.setText(mNewText);
    }

    public void undo() {
        mUneditable.setText(mOldText);
    }
}
