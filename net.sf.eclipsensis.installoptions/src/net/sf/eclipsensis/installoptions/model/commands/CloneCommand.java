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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class CloneCommand extends Command
{

    private List mParts, mNewTopLevelParts;

    private InstallOptionsDialog mParent;

    private Map mBounds, mIndices;

    private ChangeGuideCommand mVerticalGuideCommand, mHorizontalGuideCommand;

    private InstallOptionsGuide mHorizontalGuide, mVerticalGuide;

    private int mHorizontalAlignment, mVerticalAlignment;

    private Font mFont;

    public CloneCommand()
    {
        super(InstallOptionsPlugin.getResourceString("clone.command.name")); //$NON-NLS-1$
        mParts = new LinkedList();
        mFont = Display.getDefault().getSystemFont();
    }

    public void addPart(InstallOptionsWidget part, Rectangle newBounds)
    {
        mParts.add(part);
        if (mBounds == null) {
            mBounds = new HashMap();
        }
        newBounds = FigureUtility.pixelsToDialogUnits(newBounds,mFont);
        mBounds.put(part, newBounds);
    }

    public void addPart(InstallOptionsWidget part, int index)
    {
        mParts.add(part);
        if (mIndices == null) {
            mIndices = new HashMap();
        }
        mIndices.put(part, new Integer(index));
    }

    protected void clonePart(InstallOptionsElement oldPart, InstallOptionsDialog newParent, Rectangle newBounds, int index)
    {
        InstallOptionsElement newPart = null;

        if (oldPart instanceof InstallOptionsWidget) {
            try {
                newPart = (InstallOptionsWidget)((InstallOptionsWidget)oldPart).clone();
                if (newBounds != null) {
                    ((InstallOptionsWidget)newPart).getPosition().setLocation(newBounds.getTopLeft());
                }
    
                if (index < 0) {
                    newParent.addChild((InstallOptionsWidget)newPart);
                }
                else {
                    newParent.addChild((InstallOptionsWidget)newPart, index);
                }
            }
            catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        else if (oldPart instanceof InstallOptionsDialog) {
            Iterator i = ((InstallOptionsDialog)oldPart).getChildren().iterator();
            while (i.hasNext()) {
                clonePart((InstallOptionsElement)i.next(), (InstallOptionsDialog)newPart, null, -1);
            }
        }

        if (newParent == mParent) {
            mNewTopLevelParts.add(newPart);
        }
    }

    public void execute()
    {
        mNewTopLevelParts = new LinkedList();

        Iterator i = mParts.iterator();

        InstallOptionsWidget part = null;
        while (i.hasNext()) {
            part = (InstallOptionsWidget)i.next();
            if (mBounds != null && mBounds.containsKey(part)) {
                clonePart(part, mParent, (Rectangle)mBounds.get(part), -1);
            }
            else if (mIndices != null && mIndices.containsKey(part)) {
                clonePart(part, mParent, null, ((Integer)mIndices.get(part)).intValue());
            }
            else {
                clonePart(part, mParent, null, -1);
            }
        }

        if (mHorizontalGuide != null) {
            mHorizontalGuideCommand = new ChangeGuideCommand((InstallOptionsWidget)mParts.get(0),
                    true);
            mHorizontalGuideCommand.setNewGuide(mHorizontalGuide, mHorizontalAlignment);
            mHorizontalGuideCommand.execute();
        }

        if (mVerticalGuide != null) {
            mVerticalGuideCommand = new ChangeGuideCommand((InstallOptionsWidget)mParts.get(0),
                    false);
            mVerticalGuideCommand.setNewGuide(mVerticalGuide, mVerticalAlignment);
            mVerticalGuideCommand.execute();
        }
    }

    public void setParent(InstallOptionsDialog parent)
    {
        this.mParent = parent;
    }

    public void redo()
    {
        for (Iterator iter = mNewTopLevelParts.iterator(); iter.hasNext();) {
            mParent.addChild((InstallOptionsWidget)iter.next());
        }
        if (mHorizontalGuideCommand != null) {
            mHorizontalGuideCommand.redo();
        }
        if (mVerticalGuideCommand != null) {
            mVerticalGuideCommand.redo();
        }
    }

    public void setGuide(InstallOptionsGuide guide, int alignment, boolean isHorizontal)
    {
        if (isHorizontal) {
            mHorizontalGuide = guide;
            mHorizontalAlignment = alignment;
        }
        else {
            mVerticalGuide = guide;
            mVerticalAlignment = alignment;
        }
    }

    public void undo()
    {
        if (mHorizontalGuideCommand != null) {
            mHorizontalGuideCommand.undo();
        }
        if (mVerticalGuideCommand != null) {
            mVerticalGuideCommand.undo();
        }
        for (Iterator iter = mNewTopLevelParts.iterator(); iter.hasNext();) {
            mParent.removeChild((InstallOptionsWidget)iter.next());
        }
    }

}