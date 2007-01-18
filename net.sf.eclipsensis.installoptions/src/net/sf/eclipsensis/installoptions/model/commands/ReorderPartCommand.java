/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.gef.commands.Command;

public class ReorderPartCommand extends Command
{
    private int oldIndex, newIndex;

    private InstallOptionsWidget child;

    private InstallOptionsDialog parent;

    public ReorderPartCommand(InstallOptionsWidget child,
            InstallOptionsDialog parent, int oldIndex, int newIndex)
    {
        super(InstallOptionsPlugin.getResourceString("reorder.part.command.name")); //$NON-NLS-1$
        this.child = child;
        this.parent = parent;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    public void execute()
    {
        parent.moveChild(child, newIndex);
    }

    public void undo()
    {
        parent.moveChild(child, oldIndex);
    }

}