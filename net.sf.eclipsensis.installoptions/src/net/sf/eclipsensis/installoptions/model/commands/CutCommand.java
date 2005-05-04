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
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.*;

public class CutCommand extends CopyCommand
{
    private InstallOptionsDialog mParent;
    private Stack mUndoStack = new Stack();
    private ArrayList mOriginals = new ArrayList();
    private static final Comparator WIDGET_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            InstallOptionsWidget w1 = (InstallOptionsWidget)o1;
            InstallOptionsWidget w2 = (InstallOptionsWidget)o2;
            return w2.getIndex()-w1.getIndex();
        }
    };
    
    public CutCommand()
    {
        super(InstallOptionsPlugin.getResourceString("cut.command.name")); //$NON-NLS-1$
    }

    public void addPart(InstallOptionsWidgetEditPart part)
    {
        mOriginals.add(part.getModel());
        super.addPart(part);
    }

    public void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }

    public void execute()
    {
        Collections.sort(mOriginals, WIDGET_COMPARATOR);
        Collections.sort(mCopies, WIDGET_COMPARATOR);
        super.execute();
    }

    public void redo()
    {
        super.redo();
        for (Iterator iter = mOriginals.iterator(); iter.hasNext();) {
            CutInfo cutInfo = new CutInfo((InstallOptionsWidget)iter.next());
            cutInfo.cut();
            mUndoStack.push(cutInfo);
        }
    }
    public void undo()
    {
        while(mUndoStack.size() > 0) {
            CutInfo cutInfo = (CutInfo)mUndoStack.pop();
            cutInfo.uncut();
        }
        super.undo();
    }
    
    private class CutInfo
    {
        InstallOptionsWidget mElement;
        InstallOptionsGuide mVerticalGuide;
        int mVerticalAlign;
        InstallOptionsGuide mHorizontalGuide;
        int mHorizontalAlign;
        
        public CutInfo(InstallOptionsWidget element)
        {
            mElement = element;
            mVerticalGuide = mElement.getVerticalGuide();
            if(mVerticalGuide != null) {
                mVerticalAlign = mVerticalGuide.getAlignment(element);
            }
            mHorizontalGuide = mElement.getHorizontalGuide();
            if(mHorizontalGuide != null) {
                mHorizontalAlign = mHorizontalGuide.getAlignment(element);
            }
        }
        
        public void cut()
        {
            if(mVerticalGuide != null) {
                mVerticalGuide.detachPart(mElement);
            }
            if(mHorizontalGuide != null) {
                mHorizontalGuide.detachPart(mElement);
            }
            mParent.removeChild(mElement.getIndex());
        }
        
        public void uncut()
        {
            mParent.addChild(mElement,mElement.getIndex());
            if(mVerticalGuide != null) {
                mVerticalGuide.attachPart(mElement, mVerticalAlign);
            }
            if(mHorizontalGuide != null) {
                mHorizontalGuide.attachPart(mElement, mHorizontalAlign);
            }
        }
    }
}
