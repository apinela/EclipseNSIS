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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.commands.CutCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchImages;

public class CutAction extends SelectionAction
{
    /**
     * @param part
     */
    public CutAction(IWorkbenchPart part)
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
        setText(InstallOptionsPlugin.getResourceString("cut.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("cut.action.tooltip")); //$NON-NLS-1$
        setId(ActionFactory.CUT.getId());
        setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        setEnabled(false);
    }

    public Command createCutCommand(List objects) {
        if (objects.isEmpty()) {
            return null;
        }

        CutCommand cutCommand = new CutCommand();
        cutCommand.setParent(((InstallOptionsDesignEditor)getWorkbenchPart()).getInstallOptionsDialog());
        //cutCommand.setParent(objects.get(0));
        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if(object instanceof InstallOptionsWidgetEditPart) {
                cutCommand.addPart((InstallOptionsWidgetEditPart)object);
            }
            else {
                return null;
            }
        }
        
        return cutCommand;
    }

    protected boolean calculateEnabled() {
        Command cmd = createCutCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    public void run() {
        execute(createCutCommand(getSelectedObjects()));
    }
}
