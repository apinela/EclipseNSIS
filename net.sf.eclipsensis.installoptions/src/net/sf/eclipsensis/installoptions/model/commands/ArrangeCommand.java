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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class ArrangeCommand extends Command
{
    private int mType;
    private InstallOptionsDialogEditPart mParent;
    private InstallOptionsDialog mDialog;
    
    private List mSelectedParts;
    private List mSelectedChildren;

    private List mOldChildren;

    public ArrangeCommand(int type)
    {
        mType = type;
        String name;
        switch(mType) {
            case IInstallOptionsConstants.SEND_BACKWARD:
                name = "send.backward.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.SEND_TO_BACK:
                name = "send.to.back.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.BRING_FORWARD:
                name = "bring.forward.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.BRING_TO_FRONT:
            default:
                name = "bring.to.front.command.name"; //$NON-NLS-1$
                break;
        }
        setLabel(InstallOptionsPlugin.getResourceString(name)); //$NON-NLS-1$
    }

    public boolean canExecute()
    {
        if(mDialog != null) {
            if(!Common.isEmptyCollection(mSelectedChildren)) {
                mDialog.setSelection(mSelectedChildren);
                return canMove();
            }
        }
        return false;
    }
    
    protected boolean canMove()
    {
        switch(mType) {
            case IInstallOptionsConstants.SEND_BACKWARD:
                return mDialog.canSendBackward();
            case IInstallOptionsConstants.SEND_TO_BACK:
                return mDialog.canSendToBack();
            case IInstallOptionsConstants.BRING_FORWARD:
                return mDialog.canBringForward();
            case IInstallOptionsConstants.BRING_TO_FRONT:
            default:
                return mDialog.canBringToFront();
        }
    }
    
    public void execute()
    {
        mOldChildren = new ArrayList(((InstallOptionsDialog)mParent.getModel()).getChildren());
        redo();
    }

    public void setParent(InstallOptionsDialogEditPart parent)
    {
        mParent = parent;
        mDialog = (InstallOptionsDialog)mParent.getModel();
        
        mSelectedParts = Collections.EMPTY_LIST;
        mSelectedChildren = Collections.EMPTY_LIST;
        
        IStructuredSelection ssel = (IStructuredSelection)mParent.getViewer().getSelection();
        if(ssel.size() > 0) {
            if(ssel.size() > 1 || ssel.getFirstElement() != mParent) {
                mSelectedParts = new ArrayList(ssel.toList());
                mSelectedChildren = new ArrayList();
                for(Iterator iter = mSelectedParts.iterator(); iter.hasNext();) {
                    mSelectedChildren.add(((InstallOptionsWidgetEditPart)iter.next()).getModel());
                }
            }
        }
    }


    public void redo()
    {
        mDialog.setSelection(mSelectedChildren);
        move();
        mParent.getViewer().setSelection(new StructuredSelection(mSelectedParts));
    }

    protected void move()
    {
        switch(mType) {
            case IInstallOptionsConstants.SEND_BACKWARD:
                mDialog.sendBackward();
                break;
            case IInstallOptionsConstants.SEND_TO_BACK:
                mDialog.sendToBack();
                break;
            case IInstallOptionsConstants.BRING_FORWARD:
                mDialog.bringForward();
                break;
            case IInstallOptionsConstants.BRING_TO_FRONT:
            default:
                mDialog.bringToFront();
        }
    }

    public void undo()
    {
        mDialog.setChildren(mOldChildren);
        mParent.getViewer().setSelection(new StructuredSelection(mSelectedParts));
    }
}