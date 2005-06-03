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

import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;

public class ChangeGuideCommand extends Command
{

    private InstallOptionsWidget mPart;

    private InstallOptionsGuide mOldGuide, mNewGuide;

    private int mOldAlign, mNewAlign;

    private boolean mHorizontal;

    public ChangeGuideCommand(InstallOptionsWidget part, boolean horizontalGuide)
    {
        super();
        this.mPart = part;
        mHorizontal = horizontalGuide;
    }

    protected void changeGuide(InstallOptionsGuide oldGuide, InstallOptionsGuide newGuide,
            int newAlignment)
    {
        if (oldGuide != null && oldGuide != newGuide) {
            oldGuide.detachPart(mPart);
        }
        // You need to re-attach the part even if the oldGuide and the newGuide
        // are the same
        // because the alignment could have changed
        if (newGuide != null) {
            newGuide.attachPart(mPart, newAlignment);
        }
    }

    public void execute()
    {
        // Cache the old values
        mOldGuide = mHorizontal?mPart.getHorizontalGuide():mPart.getVerticalGuide();
        if (mOldGuide != null)
            mOldAlign = mOldGuide.getAlignment(mPart);

        redo();
    }

    public void redo()
    {
        changeGuide(mOldGuide, mNewGuide, mNewAlign);
    }

    public void setNewGuide(InstallOptionsGuide guide, int alignment)
    {
        mNewGuide = guide;
        mNewAlign = alignment;
    }

    public void undo()
    {
        changeGuide(mNewGuide, mOldGuide, mOldAlign);
    }

}