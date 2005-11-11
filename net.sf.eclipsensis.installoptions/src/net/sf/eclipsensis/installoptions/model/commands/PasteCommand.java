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
import net.sf.eclipsensis.installoptions.actions.Clipboard;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Display;

public class PasteCommand extends Command
{
    private InstallOptionsDialog mParent;
    private List mSelection = null;
    private Rectangle mPasteBounds;
    private List mPasteList = new ArrayList();
    private Rectangle mClientArea;

    public PasteCommand()
    {
        super(InstallOptionsPlugin.getResourceString("paste.command.name")); //$NON-NLS-1$
    }

    public void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }

    public void setSelection(List selection)
    {
        mSelection = (selection != null?selection:Collections.EMPTY_LIST);
    }

    public void setClientArea(org.eclipse.swt.graphics.Rectangle clientArea)
    {
        mClientArea = FigureUtility.pixelsToDialogUnits(new Rectangle(clientArea.x,clientArea.y,clientArea.width,clientArea.height),Display.getDefault().getSystemFont());
    }

    public void execute()
    {
        CopyCommand.CopyContents mCopyContents = (CopyCommand.CopyContents)Clipboard.getDefault().getContents();
        if(mCopyContents != null) {
            mPasteBounds = new Rectangle(mCopyContents.getBounds());
            mPasteList.clear();
            for (Iterator iter = mCopyContents.getChildren().iterator(); iter.hasNext();) {
                mPasteList.add(((InstallOptionsWidget)iter.next()).clone());
            }
        }
        redo();
    }

    private void calculatePasteBounds(Dimension size)
    {
        Point p;
        if(mClientArea != null) {
            p = new Point((mClientArea.width < mPasteBounds.width?mClientArea.x:mClientArea.x+(mClientArea.width-mPasteBounds.width)/2-1),
                                (mClientArea.height < mPasteBounds.height?mClientArea.y:mClientArea.y+(mClientArea.height-mPasteBounds.height)/2)-1);
        }
        else {
            p = new Point(mPasteBounds.x + 5, mPasteBounds.y + 5);
        }
        int delX = p.x-mPasteBounds.x;
        int delY = p.y-mPasteBounds.y;
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            InstallOptionsWidget model = (InstallOptionsWidget)iter.next();
            Position pos = model.getPosition();
            pos = model.toGraphical(pos, size);
            if(!model.isLocked()) {
                pos.setLocation(pos.left+delX,pos.top+delY);
            }
            pos = model.toModel(pos, size);
            model.getPosition().set(pos.left,pos.top,pos.right,pos.bottom);
        }
        mPasteBounds.x = p.x;
        mPasteBounds.y = p.y;
    }

    public void redo()
    {
        calculatePasteBounds(mParent.getDialogSize());
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            mParent.addChild((InstallOptionsWidget)iter.next());
        }
        List list = mParent.getChildren();
        mPasteList.retainAll(list);
        mParent.setSelection(mPasteList);
    }

    public void undo()
    {
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            mParent.removeChild((InstallOptionsWidget)iter.next());
        }
        if(mSelection != null) {
            mParent.setSelection(mSelection);
        }
    }
}