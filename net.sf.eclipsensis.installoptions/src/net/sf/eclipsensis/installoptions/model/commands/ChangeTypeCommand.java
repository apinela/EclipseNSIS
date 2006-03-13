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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.Position;

public class ChangeTypeCommand extends DeleteCommand
{
    private InstallOptionsWidget mNewChild;
    private boolean mFirstTime = true;
    
    public void setNewChild(InstallOptionsWidget newChild)
    {
        mNewChild = newChild;
    }

    protected String getName()
    {
        return InstallOptionsPlugin.getResourceString("change.type.command.name"); //$NON-NLS-1$
    }

    public void redo()
    {
        detachFromGuides(mChild);
        mIndex = mChild.getIndex();
        mParent.replaceChild(mChild, mNewChild);
        Position oldPos = mChild.getPosition();
        Position newPos = mNewChild.getPosition();
        if(oldPos != null && newPos != null && oldPos.equals(newPos)) {
            reattachToGuides(mNewChild);
        }
    }

    public void execute()
    {
        redo();
        if(mFirstTime) {
            mParent.setSelection(mNewChild);
            mFirstTime = false;
        }
    }

    public void undo()
    {
        Position oldPos = mChild.getPosition();
        Position newPos = mNewChild.getPosition();
        if(oldPos != null && newPos != null && oldPos.equals(newPos)) {
            detachFromGuides(mNewChild);
        }
        mParent.replaceChild(mNewChild, mChild);
        reattachToGuides(mChild);
    }
}
