/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.gef.commands.Command;

public class ToggleEnablementCommand extends Command
{
    private boolean mShouldEnable;
    private InstallOptionsWidget[] mParts;

    public ToggleEnablementCommand(InstallOptionsWidget[] parts, boolean shouldEnable)
    {
        mParts = parts;
        mShouldEnable = shouldEnable;
        setLabel(InstallOptionsPlugin.getResourceString((mShouldEnable?"enable.command.name":"disable.command.name"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void execute()
    {
        setEnablement(mShouldEnable);
    }

    private void setEnablement(boolean shouldEnable)
    {
        for (int i = 0; i < mParts.length; i++) {
            List flags = new ArrayList(mParts[i].getFlags());
            if(shouldEnable) {
                flags.remove(InstallOptionsModel.FLAGS_DISABLED);
            }
            else {
                if(!flags.contains(InstallOptionsModel.FLAGS_DISABLED)) {
                    flags.add(InstallOptionsModel.FLAGS_DISABLED);
                }
            }
            mParts[i].setFlags(flags);
        }
    }

    public void undo()
    {
        setEnablement(!mShouldEnable);
    }
}
