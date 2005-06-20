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
import net.sf.eclipsensis.installoptions.model.Position;

public class SetConstraintCommand extends org.eclipse.gef.commands.Command
{
    private Position mNewPos;
    private Position mOldPos;
    private Position mGraphicalPos;

    private InstallOptionsWidget mPart;

    public void execute()
    {
        mPart.setPosition(mNewPos);
    }

    public boolean canExecute()
    {
        return mGraphicalPos.left >= 0 && mGraphicalPos.top >= 0;
    }

    public String getLabel()
    {
        if (mOldPos.getSize().equals(mNewPos.getSize())) {
            return InstallOptionsPlugin.getResourceString("set.constraint.command.location.name"); //$NON-NLS-1$
        }
        return InstallOptionsPlugin.getResourceString("set.constraint.command.resize.name"); //$NON-NLS-1$
    }

    public void undo()
    {
        mPart.setPosition(mOldPos);
    }

    public void setPosition(Position pos)
    {
        mGraphicalPos = pos;
        mNewPos = mPart.toModel(pos);
    }

    public void setPart(InstallOptionsWidget part)
    {
        mPart = part;
        mOldPos = mPart.getPosition().getCopy();
    }
}