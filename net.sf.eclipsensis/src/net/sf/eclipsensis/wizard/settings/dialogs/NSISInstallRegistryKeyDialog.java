/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.NSISInstallRegistryItem;
import net.sf.eclipsensis.wizard.settings.NSISInstallRegistryKey;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallRegistryKeyDialog extends AbstractNSISInstallItemDialog
{
    protected static ArrayList cProperties = new ArrayList();

    public NSISInstallRegistryKeyDialog(NSISWizard wizard, NSISInstallRegistryKey item)
    {
        this(wizard, (NSISInstallRegistryItem)item);
    }

    protected NSISInstallRegistryKeyDialog(NSISWizard wizard, NSISInstallRegistryItem item)
    {
        super(wizard, item);
    }

    static {
        cProperties.add("rootKey"); //$NON-NLS-1$
        cProperties.add("subKey"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    protected List getProperties()
    {
        return cProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        if(mStore.getInt("rootKey") >= NSISWizardDisplayValues.HKEY_NAMES.length) { //$NON-NLS-1$
            mStore.setValue("rootKey", -1); //$NON-NLS-1$
        }
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.HKEY_NAMES,mStore.getInt("rootKey"), //$NON-NLS-1$
                            true,"wizard.root.key.label",true,null,false); //$NON-NLS-1$
        c1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("rootKey",c1.getSelectionIndex()); //$NON-NLS-1$
                validate();
            }
        });
        final Text t = NSISWizardDialogUtil.createText(composite,mStore.getString("subKey"),"wizard.sub.key.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                           null,true);
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("subKey",t.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        return composite;
    }

    protected boolean hasRequiredFields()
    {
        return true;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_regkeydlg_context"; //$NON-NLS-1$
    }

    protected String checkForErrors()
    {
        int rootKey = mStore.getInt("rootKey"); //$NON-NLS-1$
        if(rootKey < 0 || rootKey >= NSISWizardDisplayValues.HKEY_NAMES.length) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.root.key"); //$NON-NLS-1$
        }
        else {
            String subKey = mStore.getString("subKey").trim(); //$NON-NLS-1$
            if(Common.isEmpty(subKey) || subKey.endsWith("\\") || subKey.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.sub.key"); //$NON-NLS-1$
            }
            else {
                return ""; //$NON-NLS-1$
            }
        }
    }
}
