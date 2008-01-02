/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.model.InstallOptionsListItems;

import org.eclipse.gef.commands.Command;

public class ModifyListItemsCommand extends Command
{
    private InstallOptionsListItems mModel;
    private List mOldListItems;
    private List mNewListItems;

    public ModifyListItemsCommand(InstallOptionsListItems model, List newListItems)
    {
        mModel = model;
        mNewListItems = newListItems;
        mOldListItems = mModel.getListItems();
        setLabel(InstallOptionsPlugin.getFormattedString("modify.listitems.command.label", new Object[]{mModel.getType()})); //$NON-NLS-1$
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
