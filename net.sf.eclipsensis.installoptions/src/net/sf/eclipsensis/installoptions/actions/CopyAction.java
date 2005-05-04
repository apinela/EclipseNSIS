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

import java.util.Iterator;
import java.util.List;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.commands.CopyCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchImages;

public class CopyAction extends SelectionAction
{
    /**
     * @param part
     */
    public CopyAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(false);
    }

    /**
     * Initializes this action's text and images.
     */
    protected void init() 
    {
        super.init();
        setText(GEFMessages.CopyAction_Label);
        setToolTipText(GEFMessages.CopyAction_Tooltip);
        setId(ActionFactory.COPY.getId());
        setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        setEnabled(false);
    }

    public Command createCopyCommand(List objects) {
        if (objects.isEmpty()) {
            return null;
        }

        CopyCommand copyCommand = new CopyCommand();
        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if(object instanceof InstallOptionsWidgetEditPart) {
                copyCommand.addPart((InstallOptionsWidgetEditPart)object);
            }
            else {
                return null;
            }
        }
        
        return copyCommand;
    }

    protected boolean calculateEnabled() {
        Command cmd = createCopyCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    public void run() {
        execute(createCopyCommand(getSelectedObjects()));
    }
}
