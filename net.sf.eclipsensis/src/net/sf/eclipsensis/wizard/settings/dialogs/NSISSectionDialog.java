/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.wizard.settings.NSISSection;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISSectionDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();
    
    static {
        cProperties.add("bold");
        cProperties.add("defaultUnselected");
        cProperties.add("description");
        cProperties.add("hidden");
        cProperties.add("name");
    }

    public NSISSectionDialog(Shell parentShell, NSISSection item)
    {
        super(parentShell, item);
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
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        composite.setLayoutData(gd);
        
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        final Text t = NSISWizardDialogUtil.createText(composite,mStore.getString("name"),
                            "wizard.name.label",true,null,true);
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t.getText());
                setComplete(validate());
            }
        });
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.description.label",true,null,false);
        ((GridData)l.getLayoutData()).horizontalSpan = 2;
        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("description"),SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL,1,true,null);
        gd = (GridData)t2.getLayoutData();
        gd.horizontalSpan = 2;
        gd.verticalSpan = 4;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.heightHint = 100;
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("description",t2.getText());
                setComplete(validate());
            }
        });
        int textLimit;
        try {
            textLimit = Integer.parseInt(NSISPreferences.getPreferences().getNSISOption("NSIS_MAX_STRLEN"));
        }
        catch(Exception ex){
            textLimit = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }
        t2.setTextLimit(textLimit);
        
        Composite composite2 = new Composite(parent, SWT.NONE);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        composite2.setLayoutData(gd);
        
        layout = new GridLayout(3,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button cb1 = NSISWizardDialogUtil.createCheckBox(composite2,"wizard.hidden.label",mStore.getBoolean("hidden"),true,null,false);
        cb1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("hidden",cb1.getSelection());
            }
        });
        ((GridData)cb1.getLayoutData()).horizontalSpan = 1;
        
        MasterSlaveController m = new MasterSlaveController(cb1,true);
        final Button cb2 = NSISWizardDialogUtil.createCheckBox(composite2,"wizard.bold.label",mStore.getBoolean("bold"),true,m,false);
        cb2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("bold",cb2.getSelection());
            }
        });
        ((GridData)cb2.getLayoutData()).horizontalSpan = 1;

        final Button cb3 = NSISWizardDialogUtil.createCheckBox(composite2,"wizard.unselected.label",mStore.getBoolean("defaultUnselected"),true,m,false);
        cb3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("defaultUnselected",cb3.getSelection());
            }
        });
        ((GridData)cb3.getLayoutData()).horizontalSpan = 1;

        m.updateSlaves();
        
        return composite;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#validate()
     */
    protected boolean validate()
    {
        return true;
    }
}