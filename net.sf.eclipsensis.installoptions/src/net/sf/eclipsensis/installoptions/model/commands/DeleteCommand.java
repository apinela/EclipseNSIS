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
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;

public class DeleteCommand extends Command
{
    private InstallOptionsWidget mChild;

    private InstallOptionsDialog mParent;

    private InstallOptionsGuide mVerticalGuide, mHorizontalGuide;

    private int mVerticalAlign, mHorizontalAlign;

    private int mIndex = -1;

    public DeleteCommand()
    {
        super(InstallOptionsPlugin.getResourceString("delete.command.name")); //$NON-NLS-1$
    }

    private void detachFromGuides(InstallOptionsWidget part)
    {
        if (part.getVerticalGuide() != null) {
            mVerticalGuide = part.getVerticalGuide();
            mVerticalAlign = mVerticalGuide.getAlignment(part);
            mVerticalGuide.detachPart(part);
        }
        if (part.getHorizontalGuide() != null) {
            mHorizontalGuide = part.getHorizontalGuide();
            mHorizontalAlign = mHorizontalGuide.getAlignment(part);
            mHorizontalGuide.detachPart(part);
        }

    }

    public void execute()
    {
        primExecute();
    }

    protected void primExecute()
    {
        detachFromGuides(mChild);
        mIndex = mChild.getIndex();
        mParent.removeChild(mChild);
    }

    private void reattachToGuides(InstallOptionsWidget part)
    {
        if (mVerticalGuide != null) {
            mVerticalGuide.attachPart(part, mVerticalAlign);
        }
        if (mHorizontalGuide != null) {
            mHorizontalGuide.attachPart(part, mHorizontalAlign);
        }
    }

    public void redo()
    {
        primExecute();
    }

    public void setChild(InstallOptionsWidget c)
    {
        mChild = c;
    }

    public void setParent(InstallOptionsDialog p)
    {
        mParent = p;
    }

    public void undo()
    {
        mParent.addChild(mChild, mIndex);
        reattachToGuides(mChild);
    }

}
