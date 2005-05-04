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
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditPart;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.StructuredSelection;

public class PasteCommand extends Command
{
    private InstallOptionsDialogEditPart mPart;
    private Rectangle mPasteBounds;
    private List mPasteList = new ArrayList();
     
    public PasteCommand()
    {
        super(InstallOptionsPlugin.getResourceString("paste.command.name")); //$NON-NLS-1$
    }

    public void setParent(InstallOptionsDialogEditPart parent)
    {
        mPart = parent;
    }

    public void execute()
    {
        CopyCommand.CopyContents mCopyContents = (CopyCommand.CopyContents)Clipboard.getDefault().getContents();
        mPasteBounds = new Rectangle(mCopyContents.getBounds());
        for (Iterator iter = mCopyContents.getChildren().iterator(); iter.hasNext();) {
            InstallOptionsWidget model;
            try {
                model = (InstallOptionsWidget)((InstallOptionsWidget)iter.next()).clone();
                mPasteList.add(model);
            }
            catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        redo();
    }

    private void calculatePasteBounds()
    {
        FigureCanvas canvas = (FigureCanvas)mPart.getViewer().getControl();
        org.eclipse.swt.graphics.Rectangle dim = canvas.getClientArea();
        Point p = new Point((dim.width < mPasteBounds.width?dim.x:dim.x+(dim.width-mPasteBounds.width)/2-1),
                            (dim.height < mPasteBounds.height?dim.y:dim.y+(dim.height-mPasteBounds.height)/2)-1);
        int delX = p.x-mPasteBounds.x;
        int delY = p.y-mPasteBounds.y;
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            InstallOptionsWidget model = (InstallOptionsWidget)iter.next();
            Position pos = model.getPosition();
            pos = model.toGraphical(pos);
            pos.setLocation(pos.left+delX,pos.top+delY);
            pos = model.toModel(pos);
            model.getPosition().set(pos.left,pos.top,pos.right,pos.bottom);
        }
        mPasteBounds.x = p.x;
        mPasteBounds.y = p.y;
    }

    public void redo()
    {
        calculatePasteBounds();
        InstallOptionsDialog dialog = (InstallOptionsDialog)mPart.getModel();
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            dialog.addChild((InstallOptionsWidget)iter.next());
        }
        List list = mPart.getChildren();
        ArrayList selectionList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            InstallOptionsEditPart child = (InstallOptionsEditPart)iter.next();
            if(mPasteList.contains(child.getModel())) {
                selectionList.add(child);
            }
        }
        if(selectionList.size() > 0) {
            mPart.getViewer().setSelection(new StructuredSelection(selectionList));
        }
    }

    public void undo()
    {
        InstallOptionsDialog dialog = (InstallOptionsDialog)mPart.getModel();
        for (Iterator iter = mPasteList.iterator(); iter.hasNext();) {
            dialog.removeChild((InstallOptionsWidget)iter.next());
        }
    }
}