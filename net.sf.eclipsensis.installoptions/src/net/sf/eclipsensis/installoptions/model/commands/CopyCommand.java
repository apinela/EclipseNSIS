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

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.Clipboard;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

public class CopyCommand extends Command
{
    protected List mCopies;
    protected Object mOldContents = null;
    protected CopyContents mNewContents = new CopyContents();
    private Rectangle mBounds;
    private int mMinX = Integer.MAX_VALUE;
    private int mMaxX = Integer.MIN_VALUE;
    private int mMinY = Integer.MAX_VALUE;
    private int mMaxY = Integer.MIN_VALUE;

    public CopyCommand()
    {
        this(InstallOptionsPlugin.getResourceString("copy.command.name")); //$NON-NLS-1$
    }

    protected CopyCommand(String label)
    {
        super(label);
        mCopies = new ArrayList();
    }

    public void addPart(InstallOptionsWidgetEditPart part)
    {
        Rectangle bounds = part.getFigure().getBounds();
        mMinX = Math.min(mMinX,bounds.x);
        mMaxX = Math.max(mMaxX,bounds.x+bounds.width-1);
        mMinY = Math.min(mMinY,bounds.y);
        mMaxY = Math.max(mMaxY,bounds.y+bounds.height-1);
        
        InstallOptionsWidgetEditPart newPart;
        try {
            mCopies.add((InstallOptionsWidget)((InstallOptionsWidget)part.getModel()).clone());
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void execute()
    {
        mBounds = new Rectangle(mMinX,mMinY,mMaxX-mMinX+1,mMaxY-mMinY+1);
        redo();
    }
    
    public void redo()
    {
        Clipboard clipboard = (Clipboard)Clipboard.getDefault();
        mOldContents = clipboard.getContents();
        clipboard.setContents(mNewContents);
    }

    public void undo()
    {
        Clipboard.getDefault().setContents(mOldContents);
    }
    
    public class CopyContents
    {
        public Rectangle getBounds()
        {
            return mBounds;
        }

        public List getChildren()
        {
            return mCopies;
        }
    }
}