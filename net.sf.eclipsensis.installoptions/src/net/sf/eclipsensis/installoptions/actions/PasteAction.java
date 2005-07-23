/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.commands.PasteCommand;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;

public class PasteAction extends WorkbenchPartAction implements PropertyChangeListener
{
    /**
     * @param part
     */
    public PasteAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(true);
        ((Clipboard)Clipboard.getDefault()).addPropertyChangeListener(this);
    }

    /**
     * Initializes this action's text and images.
     */
    protected void init() 
    {
        super.init();
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText(InstallOptionsPlugin.getResourceString("paste.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("paste.action.tooltip")); //$NON-NLS-1$
        setId(ActionFactory.PASTE.getId());
        setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        setEnabled(false);
    }

    public Command createPasteCommand() {
        PasteCommand pasteCommand = null;
        IWorkbenchPart part = getWorkbenchPart();
        if(part instanceof InstallOptionsDesignEditor) {
            InstallOptionsDesignEditor editor = (InstallOptionsDesignEditor)part;
            EditDomain domain = (EditDomain)getWorkbenchPart().getAdapter(EditDomain.class);
            if(domain instanceof InstallOptionsEditDomain && ((InstallOptionsEditDomain)domain).isReadOnly()) {
                return null;
            }
            pasteCommand = new PasteCommand();
            pasteCommand.setParent((InstallOptionsDialogEditPart)editor.getGraphicalViewer().getContents());
        }
        
        return pasteCommand;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(Clipboard.CONTENTS_SET_EVENT)) {
            setEnabled(true);
        }
    }

    protected boolean calculateEnabled() {
        Command command = null;
        if(Clipboard.getDefault().getContents() != null) {
            command = createPasteCommand();
        }
        return command != null && command.canExecute();
    }

    public void run() {
        execute(createPasteCommand());
    }
}
