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
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallRegistryValue;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallRegistryValueDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();
    private String[] mHKEYNames;

    static {
        cProperties.add("rootKey"); //$NON-NLS-1$
        cProperties.add("subKey"); //$NON-NLS-1$
        cProperties.add("valueType"); //$NON-NLS-1$
        cProperties.add("value"); //$NON-NLS-1$
        cProperties.add("data"); //$NON-NLS-1$
    }

    public NSISInstallRegistryValueDialog(NSISWizard wizard, NSISInstallRegistryValue item)
    {
        super(wizard, item);
        mHKEYNames = NSISWizardDisplayValues.getHKEYNames();
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

        if(mStore.getInt("rootKey") >= mHKEYNames.length) { //$NON-NLS-1$
            mStore.setValue("rootKey","-1"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,mHKEYNames,mStore.getInt("rootKey"), //$NON-NLS-1$
                            true,"wizard.root.key.label",true,null,false); //$NON-NLS-1$
        c1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("rootKey",c1.getSelectionIndex()); //$NON-NLS-1$
                validate();
            }
        });
        final Text t1 = NSISWizardDialogUtil.createText(composite,mStore.getString("subKey"),"wizard.sub.key.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                           null,true);
        t1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("subKey",t1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("value"),"wizard.value.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                null,false);
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("value",t2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.REG_VALUE_TYPES,mStore.getInt("valueType"), //$NON-NLS-1$
                true,"wizard.value.type.label",true,null,false); //$NON-NLS-1$
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                validate();
            }
        });

        final Text t3 = NSISWizardDialogUtil.createText(composite,mStore.getString("data"),"wizard.data.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                null,false);
        t3.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("data",t3.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        t3.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e)
            {
                int index = c2.getSelectionIndex();
                if(index == INSISWizardConstants.REG_DWORD) {
                    char[] chars = e.text.toCharArray();
                    for(int i=0; i< chars.length; i++) {
                        if(!Character.isDigit(chars[i])) {
                            e.doit = false;
                            return;
                        }
                    }
                }
            }
        });

        final Font regularFont;
        final Font requiredFont;
        final Label l = (Label)t3.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)l.getLayoutData()).horizontalAlignment = GridData.FILL;
            regularFont = l.getFont();
            FontData[] fd = regularFont.getFontData();
            fd[0].setStyle(SWT.BOLD);
            requiredFont = new Font(l.getDisplay(),fd);
            if(c2.getSelectionIndex() == INSISWizardConstants.REG_DWORD) {
                l.setFont(requiredFont);
            }
            l.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    requiredFont.dispose();
                }
            });
        }
        else {
            regularFont = null;
            requiredFont = null;
        }
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int index = c2.getSelectionIndex();
                mStore.setValue("valueType",index); //$NON-NLS-1$
                if(index == INSISWizardConstants.REG_DWORD) {
                    l.setFont(requiredFont);
                    try {
                        Integer.parseInt(t3.getText());
                    }
                    catch(Exception ex) {
                        t3.setText(""); //$NON-NLS-1$
                    }
                }
                else {
                    l.setFont(regularFont);
                }
            }
        });
        return composite;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_regval_context"; //$NON-NLS-1$
    }

    protected String checkForErrors()
    {
        int rootKey = mStore.getInt("rootKey"); //$NON-NLS-1$
        if(rootKey < 0 || rootKey >= 7) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.root.key"); //$NON-NLS-1$
        }
        else {
            String subKey = mStore.getString("subKey").trim(); //$NON-NLS-1$
            if(Common.isEmpty(subKey) || subKey.endsWith("\\") || subKey.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.sub.key"); //$NON-NLS-1$
            }
            else if(mStore.getInt("valueType") != INSISWizardConstants.REG_SZ && Common.isEmpty(mStore.getString("data"))) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value"); //$NON-NLS-1$
            }
            else {
                return ""; //$NON-NLS-1$
            }
        }
    }
}
