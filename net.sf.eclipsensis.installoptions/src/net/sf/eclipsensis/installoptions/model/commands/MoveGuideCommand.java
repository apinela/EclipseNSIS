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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.Position;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class MoveGuideCommand extends Command
{
    private int mPositionDelta;
    private Map mOldPositions = new HashMap();
    private InstallOptionsGuide mGuide;

    public MoveGuideCommand(InstallOptionsGuide guide, int positionDelta)
    {
        super(InstallOptionsPlugin.getResourceString("move.guide.command.name")); //$NON-NLS-1$
        this.mGuide = guide;
        mPositionDelta = positionDelta;
    }

    public void execute()
    {
        boolean isHorizontal = mGuide.isHorizontal();
        int guidePos = mGuide.getPosition() + mPositionDelta;
        Font f = Display.getDefault().getSystemFont();
        guidePos = (isHorizontal?FigureUtility.pixelsToDialogUnitsY(guidePos,f):FigureUtility.pixelsToDialogUnitsX(guidePos,f));
        mGuide.setPosition((isHorizontal?FigureUtility.dialogUnitsToPixelsY(guidePos,f):FigureUtility.dialogUnitsToPixelsX(guidePos,f)));
        Iterator iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            Position pos = widget.getPosition();
            mOldPositions.put(widget,pos);
            pos = pos.getCopy();
            int alignment = mGuide.getAlignment(widget);
            if (mGuide.isHorizontal()) {
                pos.setLocation(pos.left,calculatePosition(guidePos,alignment, pos));
            }
            else {
                pos.setLocation(calculatePosition(guidePos,alignment, pos),pos.top);
            }
            widget.setPosition(pos);
        }
    }

    public boolean canExecute()
    {
        boolean isHorizontal = mGuide.isHorizontal();
        Font f = Display.getDefault().getSystemFont();
        int guidePos = mGuide.getPosition() + mPositionDelta;
        guidePos = (isHorizontal?FigureUtility.pixelsToDialogUnitsY(guidePos,f):FigureUtility.pixelsToDialogUnitsX(guidePos,f));
        if(guidePos < 0) {
            return false;
        }
        Iterator iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            Position pos = widget.getPosition();
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
        Iterator iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            widget.setPosition((Position)mOldPositions.get(widget));
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
                position = guidePos-(dim-1)/2;
                break;
            case 1:
                position = guidePos-dim+1;
                break;
            default:
                position = (mGuide.isHorizontal()?pos.getBounds().y:pos.getBounds().x);
        }
        return position;
    }
}