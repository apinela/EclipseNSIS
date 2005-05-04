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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class ToggleDialogSizeVisibilityAction extends Action
{
    public static final String ID = "net.sf.eclipsensis.installoptions.toggle_dialog_size_visibiliy"; //$NON-NLS-1$
    private InstallOptionsDesignEditor mEditor;

    public ToggleDialogSizeVisibilityAction(InstallOptionsDesignEditor editor)
    {
        super(InstallOptionsPlugin.getResourceString("show.dialog.size.action.name"), AS_CHECK_BOX); //$NON-NLS-1$
        mEditor = editor;
        setToolTipText(InstallOptionsPlugin.getResourceString("show.dialog.size.tooltip")); //$NON-NLS-1$
        setId(ID);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.dialog.size.icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setEnabled(isEnabled());
        setChecked(isChecked());
    }
    
    public void run()
    {
        InstallOptionsDialog dialog = mEditor.getInstallOptionsDialog();
        if(dialog != null) {
            dialog.setDialogSizeVisible(!dialog.isDialogSizeVisible());
        }
    }
    
    public boolean isEnabled()
    {
        return (mEditor.getInstallOptionsDialog() != null);
    }
    
    public boolean isChecked()
    {
        InstallOptionsDialog dialog = mEditor.getInstallOptionsDialog();
        return (dialog != null && dialog.isDialogSizeVisible());
    }
}
