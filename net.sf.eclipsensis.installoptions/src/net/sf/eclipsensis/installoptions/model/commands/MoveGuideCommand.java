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

import java.util.HashMap;
import java.util.Iterator;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.Position;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;

public class MoveGuideCommand extends Command
{
    private int mPositionDelta;
    private HashMap mOldPositions = new HashMap();
    private InstallOptionsGuide mGuide;

    public MoveGuideCommand(InstallOptionsGuide guide, int positionDelta)
    {
        super(InstallOptionsPlugin.getResourceString("move.guide.command.name")); //$NON-NLS-1$
        this.mGuide = guide;
        mPositionDelta = positionDelta;
    }

    public void execute()
    {
        mGuide.setPosition(mGuide.getPosition() + mPositionDelta);
        Iterator iter = mGuide.getParts().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            Position pos = widget.getPosition();
            mOldPositions.put(widget,pos);
            pos = widget.toGraphical(pos);
            int alignment = mGuide.getAlignment(widget);
            if (mGuide.isHorizontal()) {
                pos.setLocation(pos.left,calculatePosition(mGuide.getPosition(),alignment, pos));//pos.top+mPositionDelta);
            }
            else {
                pos.setLocation(/*pos.left+mPositionDelta*/calculatePosition(mGuide.getPosition(),alignment, pos),pos.top);
            }
            pos = widget.toModel(pos);
            widget.setPosition(pos);
        }
    }

    public boolean canExecute()
    {
        int guidePos = mGuide.getPosition() + mPositionDelta;
        Iterator iter = mGuide.getParts().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            Position pos = widget.toGraphical(widget.getPosition());
            int alignment = mGuide.getAlignment(widget);
            int position = calculatePosition(guidePos, alignment, pos);
            if(position < 0) {
                return false;
            }
        }
        return super.canExecute();
    }
    
    public void undo()
    {
        mGuide.setPosition(mGuide.getPosition() - mPositionDelta);
        Iterator iter = mGuide.getParts().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget part = (InstallOptionsWidget)iter.next();
            part.setPosition((Position)mOldPositions.get(part));
        }
    }

    private int calculatePosition(int guidePos, int alignment, Position pos)
    {
        int position;
        int dim = (mGuide.isHorizontal()?pos.getSize().height:pos.getSize().width);
        switch(alignment) {
            case -1:
                position = guidePos;
                break;
            case 0:
                position = guidePos-dim/2;
                break;
            case 1:
                position = guidePos-dim;
                break;
            default:
                position = (mGuide.isHorizontal()?pos.getBounds().y:pos.getBounds().x);
        }
        return position;
    }
}