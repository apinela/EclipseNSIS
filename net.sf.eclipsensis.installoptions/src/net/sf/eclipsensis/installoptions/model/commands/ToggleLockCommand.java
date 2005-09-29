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
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.gef.commands.Command;

public class ToggleLockCommand extends Command
{
    private boolean mShouldLock;
    private InstallOptionsWidget[] mParts;

    public ToggleLockCommand(InstallOptionsWidget[] parts, boolean shouldLock) 
    {
        mParts = parts;
        mShouldLock = shouldLock;
        setLabel(InstallOptionsPlugin.getResourceString((mShouldLock?"lock.command.name":"unlock.command.name"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void execute() 
    {
        setLocked(mShouldLock);
    }

    private void setLocked(boolean shouldLock)
    {
        for (int i = 0; i < mParts.length; i++) {
            mParts[i].setLocked(shouldLock);
        }
    }

    public void undo() 
    {
        setLocked(!mShouldLock);
    }
}