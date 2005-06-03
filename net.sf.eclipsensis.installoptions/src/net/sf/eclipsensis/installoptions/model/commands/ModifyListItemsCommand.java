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

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsCombobox;

import org.eclipse.gef.commands.Command;

public class ModifyListItemsCommand extends Command
{
    private InstallOptionsWidgetEditPart mEditPart;
    private InstallOptionsCombobox mModel;
    private List mOldListItems;
    private List mNewListItems;
    
    public ModifyListItemsCommand(InstallOptionsWidgetEditPart editPart, List newListItems)
    {
        mEditPart = editPart;
        mModel = (InstallOptionsCombobox)mEditPart.getModel();
        mNewListItems = newListItems;
        mOldListItems = mModel.getListItems();
        setLabel(InstallOptionsPlugin.getFormattedString("modify.listitems.command.label", new Object[]{mModel.getType()}));
    }
    
    public void execute()
    {
        mModel.setListItems(mNewListItems);
    }
    
    public void undo()
    {
        mModel.setListItems(mOldListItems);
    }
}
