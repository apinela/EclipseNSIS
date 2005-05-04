/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.DialogSize;
import net.sf.eclipsensis.installoptions.model.commands.SetDialogSizeCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;

public class SetDialogSizeAction extends Action
{
    private IEditorPart mEditor = null;
    private DialogSize mDialogSize = null;
    /**
     * @param text
     */
    public SetDialogSizeAction(DialogSize dialogSize)
    {
        super((dialogSize==null?InstallOptionsPlugin.getResourceString("empty.dialog.size.name"):InstallOptionsPlugin.getFormattedString("set.dialog.size.action.name.format",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    new Object[]{dialogSize.getName(),
                                                                 new Integer(dialogSize.getSize().width),
                                                                 new Integer(dialogSize.getSize().height)})));
        mDialogSize = dialogSize;
    }

    public void setEditor(IEditorPart editor)
    {
        mEditor = editor;
    }

    private Command createSetDialogSizeCommand()
    {
        SetDialogSizeCommand command = new SetDialogSizeCommand();
        command.setEditor((InstallOptionsDesignEditor)mEditor);
        command.setNewSize(mDialogSize.getSize());
        return command;
    }

    public void run()
    {
        if(isEnabled()) {
            Command command = createSetDialogSizeCommand();
            if(command != null && command.canExecute()) {
                CommandStack commandStack = (CommandStack)mEditor.getAdapter(CommandStack.class);
                if(commandStack != null) {
                    commandStack.execute(command);
                }
                else {
                    command.execute();
                }
            }
        }
    }

    public boolean isEnabled()
    {
        return (mDialogSize != null && mEditor != null && mEditor instanceof InstallOptionsDesignEditor && 
                !((InstallOptionsDesignEditor)mEditor).isDisposed() && ((InstallOptionsDesignEditor)mEditor).getInstallOptionsDialog() != null);
    }
}