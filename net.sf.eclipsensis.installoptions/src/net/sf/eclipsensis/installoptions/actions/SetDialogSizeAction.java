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
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.DialogSize;

import org.eclipse.gef.EditDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;

public class SetDialogSizeAction extends Action
{
    private IEditorPart mEditor = null;
    private DialogSize mDialogSize = null;
    /**
     * @param text
     */
    public SetDialogSizeAction(DialogSize dialogSize)
    {
        super((dialogSize==null?InstallOptionsPlugin.getResourceString("empty.dialog.size.name"):InstallOptionsPlugin.getFormattedString("set.dialog.size.action.name.format",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    new Object[]{dialogSize.getName(),
                                                                 new Integer(dialogSize.getSize().width),
                                                                 new Integer(dialogSize.getSize().height)})));
        mDialogSize = dialogSize;
    }

    public void setEditor(IEditorPart editor)
    {
        mEditor = editor;
    }

    public void run()
    {
        if(isEnabled()) {
            ((InstallOptionsDesignEditor)mEditor).getGraphicalViewer().setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,mDialogSize.getSize());
        }
    }

    public boolean isEnabled()
    {
        if (mDialogSize != null && mEditor != null && mEditor instanceof InstallOptionsDesignEditor && 
                !((InstallOptionsDesignEditor)mEditor).isDisposed() && ((InstallOptionsDesignEditor)mEditor).getGraphicalViewer() != null) {
            EditDomain domain = (EditDomain)mEditor.getAdapter(EditDomain.class);
            if(domain instanceof InstallOptionsEditDomain && ((InstallOptionsEditDomain)domain).isReadOnly()) {
                return false;
            }
            return true;
        }
        return false;
    }
}
