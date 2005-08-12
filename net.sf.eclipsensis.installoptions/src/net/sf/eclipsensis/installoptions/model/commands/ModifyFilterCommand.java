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
import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;

import org.eclipse.gef.commands.Command;

public class ModifyFilterCommand extends Command
{
    private InstallOptionsFileRequest mModel;
    private List mOldFilter;
    private List mNewFilter;
    
    public ModifyFilterCommand(InstallOptionsFileRequest model, List newListItems)
    {
        mModel = model;
        mNewFilter = newListItems;
        mOldFilter = mModel.getFilter();
        setLabel(InstallOptionsPlugin.getFormattedString("modify.filter.command.label", new Object[]{mModel.getType()})); //$NON-NLS-1$
    }
    
    public void execute()
    {
        mModel.setFilter(mNewFilter);
    }
    
    public void undo()
    {
        mModel.setFilter(mOldFilter);
    }
}
