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
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

public class CreateCommand extends org.eclipse.gef.commands.Command
{
    private InstallOptionsWidget mChild;

    private Rectangle mRect;

    private InstallOptionsDialog mParent;

    private int mIndex = -1;

    public CreateCommand()
    {
        super(InstallOptionsPlugin.getResourceString("create.command.name")); //$NON-NLS-1$
    }

    public void execute()
    {
        if (mRect != null) {
            Insets expansion = getInsets();
            if (!mRect.isEmpty())
                mRect.expand(expansion);
            else {
                mRect.x -= expansion.left;
                mRect.y -= expansion.top;
            }
            Position p  = mChild.getPosition();
            p.setLocation(mRect.getLocation());
            if (!mRect.isEmpty())
                p.setSize(mRect.getSize());
        }
        redo();
    }

    private Insets getInsets()
    {
        return new Insets();
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    public void redo()
    {
        mParent.addChild(mChild, mIndex);
    }

    public void setChild(InstallOptionsWidget subpart)
    {
        mChild = subpart;
    }

    public void setIndex(int index)
    {
        this.mIndex = index;
    }

    public void setLocation(Rectangle r)
    {
        mRect = r;
    }

    public void setParent(InstallOptionsDialog newParent)
    {
        mParent = newParent;
    }

    public void undo()
    {
        mParent.removeChild(mChild);
    }
}
