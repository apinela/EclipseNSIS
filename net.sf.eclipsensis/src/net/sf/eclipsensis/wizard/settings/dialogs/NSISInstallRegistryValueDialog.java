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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.NumberVerifyListener;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallRegistryValue;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class NSISInstallRegistryValueDialog extends NSISInstallRegistryKeyDialog
{
    static {
        cProperties.add("valueType"); //$NON-NLS-1$
        cProperties.add("value"); //$NON-NLS-1$
        cProperties.add("data"); //$NON-NLS-1$
    }

    public NSISInstallRegistryValueDialog(NSISWizard wizard, NSISInstallRegistryValue item)
    {
        super(wizard, item);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControlContents(Composite parent)
    {
        Composite composite = (Composite)super.createControlContents(parent);
        final Text t1 = NSISWizardDialogUtil.createText(composite,mStore.getString("value"),"wizard.value.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                null,false);
        t1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("value",t1.getText().trim()); //$NON-NLS-1$
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

        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("data"),"wizard.data.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                null,false);
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("data",t2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        t2.addVerifyListener(new NumberVerifyListener() {
            public void verifyText(VerifyEvent e)
            {
                int index = c2.getSelectionIndex();
                if(index == INSISWizardConstants.REG_DWORD) {
                    super.verifyText(e);
                }
                else if(index == INSISWizardConstants.REG_BIN) {
                    char[] chars = e.text.toCharArray();
                    for(int i=0; i< chars.length; i++) {
                        if(!Character.isDigit(chars[i]) && (chars[i]<'a' || chars[i]>'f') && (chars[i]<'A' || chars[i]>'F')) {
                            e.display.beep();
                            e.doit = false;
                            return;
                        }
                    }
                }
            }
        });

        final Font regularFont;
        final Font requiredFont;
        final Label l = (Label)t2.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)NSISWizardDialogUtil.getLayoutControl(l).getLayoutData()).horizontalAlignment = GridData.FILL;
            regularFont = l.getFont();
            FontData[] fd = regularFont.getFontData();
            for (int i = 0; i < fd.length; i++) {
                fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
            }
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
                        Integer.parseInt(t2.getText());
                    }
                    catch(Exception ex) {
                        t2.setText(""); //$NON-NLS-1$
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
        String error = super.checkForErrors();
        if(Common.isEmpty(error)) {
            String data = mStore.getString("data"); //$NON-NLS-1$
            switch(mStore.getInt("valueType")) { //$NON-NLS-1$
                case INSISWizardConstants.REG_BIN:
                    if(Common.isEmpty(data) || (data.length() % 2) != 0) {
                        return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value"); //$NON-NLS-1$
                    }
                    break;
                case INSISWizardConstants.REG_DWORD:
                case INSISWizardConstants.REG_EXPAND_SZ:
                    if(Common.isEmpty(data)) {
                        return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value"); //$NON-NLS-1$
                    }
            }
            return ""; //$NON-NLS-1$
        }
        return error;
    }
}
