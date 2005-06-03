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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.commands.ArrangeCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

public class ArrangeAction extends SelectionAction
{
    public static final String BRING_TO_FRONT_ID = "bring.to.front"; //$NON-NLS-1$
    public static final String BRING_FORWARD_ID = "bring.forward"; //$NON-NLS-1$
    public static final String SEND_TO_BACK_ID = "send.to.back"; //$NON-NLS-1$
    public static final String SEND_BACKWARD_ID = "send.backward"; //$NON-NLS-1$
    
    private int mType;
    /**
     * @param part
     */
    public ArrangeAction(IWorkbenchPart part, int type)
    {
        super(part);
        mType = type;
        setLazyEnablementCalculation(false);
        initUI();
    }

    /**
     * Initializes this action's text and images.
     */
    protected void initUI() 
    {
        String prefix;
        switch(mType) {
            case IInstallOptionsConstants.SEND_BACKWARD:
                prefix = SEND_BACKWARD_ID;
                break;
            case IInstallOptionsConstants.SEND_TO_BACK:
                prefix = SEND_TO_BACK_ID;
                break;
            case IInstallOptionsConstants.BRING_FORWARD:
                prefix = BRING_FORWARD_ID;
                break;
            case IInstallOptionsConstants.BRING_TO_FRONT:
            default:
                prefix = BRING_TO_FRONT_ID;
                break;
        }
        
        setId(prefix);
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }

    public Command createMoveCommand()
    {
        ArrangeCommand command = null;
        IWorkbenchPart part = getWorkbenchPart();
        if(part instanceof InstallOptionsDesignEditor) {
            InstallOptionsDesignEditor editor = (InstallOptionsDesignEditor)part;
            command = new ArrangeCommand(mType);
            command.setParent((InstallOptionsDialogEditPart)editor.getGraphicalViewer().getContents());
        }
        
        return command;
    }

    protected boolean calculateEnabled() {
        Command cmd = createMoveCommand();
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    public void run() {
        execute(createMoveCommand());
    }
}
