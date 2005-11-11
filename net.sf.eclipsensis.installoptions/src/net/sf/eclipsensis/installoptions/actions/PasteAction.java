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
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.commands.PasteCommand;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Canvas;
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
        Clipboard.getDefault().addPropertyChangeListener(this);
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

    public PasteCommand createPasteCommand() {
        PasteCommand pasteCommand = null;
        IWorkbenchPart part = getWorkbenchPart();
        if(part instanceof InstallOptionsDesignEditor) {
            EditDomain domain = (EditDomain)getWorkbenchPart().getAdapter(EditDomain.class);
            if(domain instanceof InstallOptionsEditDomain && ((InstallOptionsEditDomain)domain).isReadOnly()) {
                return null;
            }
            pasteCommand = new PasteCommand();
        }

        return pasteCommand;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(Clipboard.CONTENTS_AVAILABLE_EVENT)) {
            setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        }
    }

    protected boolean calculateEnabled() {
        Command command = null;
        if(Clipboard.getDefault().isContentsAvailable()) {
            command = createPasteCommand();
        }
        return command != null && command.canExecute();
    }

    public void run() {
        PasteCommand command = createPasteCommand();
        if(command != null) {
            InstallOptionsDesignEditor editor = (InstallOptionsDesignEditor)getWorkbenchPart();
            GraphicalViewer viewer = editor.getGraphicalViewer();
            command.setParent((InstallOptionsDialog)editor.getGraphicalViewer().getContents().getModel());
            command.setClientArea(((Canvas)viewer.getControl()).getClientArea());
            List selection = ((IStructuredSelection)viewer.getSelection()).toList();
            List modelSelection = new ArrayList();
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                EditPart part = (EditPart)iter.next();
                modelSelection.add(part.getModel());
            }
            command.setSelection(modelSelection);
            execute(command);
        }
    }
}
