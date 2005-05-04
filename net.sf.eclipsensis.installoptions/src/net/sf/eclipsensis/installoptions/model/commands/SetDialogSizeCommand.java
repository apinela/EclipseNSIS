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
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.*;

public class SetDialogSizeCommand extends Command
{
    private InstallOptionsDesignEditor mEditor;
    private Dimension mNewSize;
    private Dimension mOldSize;

    /**
     * 
     */
    public SetDialogSizeCommand()
    {
        super(InstallOptionsPlugin.getResourceString("set.dialog.size.command.name")); //$NON-NLS-1$
    }

    
    public void setEditor(InstallOptionsDesignEditor editor)
    {
        mEditor = editor;
    }

    public void setNewSize(Dimension newSize)
    {
        mNewSize = newSize.getCopy();
    }

    public void execute()
    {
        mOldSize = mEditor.getInstallOptionsDialog().getSize();
        redo();
    }
    
    private void setSize(Dimension size)
    {
        ISelection sel = mEditor.getGraphicalViewer().getSelection();
        InstallOptionsDialogEditPart editPart = null;
        if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection)sel).getFirstElement();
            if(obj instanceof InstallOptionsDialogEditPart) {
                editPart = (InstallOptionsDialogEditPart)obj;
            }
        }
        mEditor.getInstallOptionsDialog().setSize(size.getCopy());
        if(editPart != null) {
            mEditor.getGraphicalViewer().setSelection(new StructuredSelection(editPart));
        }
    }
    
    public void redo()
    {
        setSize(mNewSize);
    }
    
    public void undo()
    {
        setSize(mOldSize);
    }
}
