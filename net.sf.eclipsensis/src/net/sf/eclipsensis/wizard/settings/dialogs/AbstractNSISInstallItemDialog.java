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

import java.text.MessageFormat;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.INSISInstallElement;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.*;

public abstract class AbstractNSISInstallItemDialog extends Dialog
{
    protected INSISInstallElement mItem;
    protected IPreferenceStore mStore;
    private boolean mComplete = true;
    
    public AbstractNSISInstallItemDialog(Shell parentShell, INSISInstallElement item)
    {
        super(parentShell);
        setBlockOnOpen(true);
        mItem = item;
        mStore = new PreferenceStore();
        Common.beanToStore(mItem, mStore, getProperties());
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected final Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        Control control = createControl(composite);
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected final Control createButtonBar(Composite parent)
    {
        Control control = super.createButtonBar(parent);
        setComplete(validate());
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText(MessageFormat.format(EclipseNSISPlugin.getResourceString("wizard.installitem.dialog.title.format"), //$NON-NLS-1$
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

    public final void setComplete(boolean complete)
    {
        getButton(IDialogConstants.OK_ID).setEnabled(complete);
    }

    protected abstract Control createControl(Composite parent);
    protected abstract boolean validate();
    protected abstract List getProperties();
}
