/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.INSISInstallElement;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class AbstractNSISInstallItemDialog extends StatusMessageDialog
{
    protected INSISInstallElement mItem;
    protected IPreferenceStore mStore;
    
    public AbstractNSISInstallItemDialog(Shell parentShell, INSISInstallElement item)
    {
        super(parentShell);
        mItem = item;
        mStore = new PreferenceStore();
        Common.beanToStore(mItem, mStore, getProperties());
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected final Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Control control = createControlContents(composite);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(65);
        control.setLayoutData(gd);
        
        Dialog.applyDialogFont(composite);
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create()
    {
        super.create();
        validate();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText(EclipseNSISPlugin.getFormattedString("wizard.installitem.dialog.title.format", //$NON-NLS-1$
                         new String[]{mItem.getType()}));
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        if(validate()) {
            Common.storeToBean(mItem, mStore, getProperties());
            super.okPressed();
        }
    }

    protected final boolean validate()
    {
        DialogStatus status = getStatus();
        String error = checkForErrors();
        if(Common.isEmpty(error)) {
            status.setOK();
        }
        else {
            status.setError(error);
        }
        refreshStatus();
        return status.getSeverity() == IStatus.OK;
    }

    protected abstract String checkForErrors();
    protected abstract Control createControlContents(Composite parent);
    protected abstract List getProperties();
}
