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

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallShortcutDialog extends AbstractNSISInstallItemDialog implements INSISWizardConstants
{
    private static ArrayList cProperties = new ArrayList();
    
    static {
        cProperties.add("location");
        cProperties.add("name");
        cProperties.add("shortcutType");
        cProperties.add("path");
        cProperties.add("url");
    }

    public NSISInstallShortcutDialog(Shell parentShell, NSISInstallShortcut item)
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
        
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,NSISKeywords.PREDEFINED_PATH_VARIABLES,mStore.getString("location"),
                                                          false,"wizard.location.label",true,null,true);
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("location",c1.getText());
                setComplete(validate());
            }
        });
        gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;

        final Text t1 = NSISWizardDialogUtil.createText(composite,mStore.getString("name"),"wizard.name.label",true,null,true);
        t1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t1.getText());
                setComplete(validate());
            }
        });

        final Button[] radio = NSISWizardDialogUtil.createRadioGroup(composite,NSISWizardDisplayValues.SHORTCUT_TYPE_NAMES,mStore.getInt("shortcutType"),
                            "wizard.shortcut.type.label",true,null,false);
        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Button b = (Button)e.widget;
                if(b.getSelection()) {
                    int n=-1;
                    if(b == radio[0]) {
                        n = 0;
                    }
                    else if(b == radio[1]) {
                        n = 1;
                    }
                    mStore.setValue("shortcutType",n);
                    setComplete(validate());
                }
            }            
        };
        for (int i = 0; i < radio.length; i++) {
            radio[i].addSelectionListener(sa);
        }
        MasterSlaveController m1 = new MasterSlaveController(radio[SHORTCUT_URL]);
        MasterSlaveController m2 = new MasterSlaveController(radio[SHORTCUT_INSTALLELEMENT]);
        
        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("url"),"wizard.url.label",true,m1,true);
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("url",t2.getText());
                setComplete(validate());
            }
        });
        
        final Combo c2 = NSISWizardDialogUtil.createContentBrowser(composite, "wizard.path.label", mStore.getString("path"), mItem.getSettings(), true, m2, true);

        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("path",c2.getText());
                setComplete(validate());
            }
        });

        m2.updateSlaves();
        m1.updateSlaves();
        
        return composite;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#validate()
     */
    protected boolean validate()
    {
        if(Common.isValidNSISPrefixedPathName(mStore.getString("location")) && Common.isValidFileName(mStore.getString("name"))) {
            int n = mStore.getInt("shortcutType");
            if((n == SHORTCUT_INSTALLELEMENT && Common.isValidNSISPrefixedPathName(mStore.getString("path")))||
               (n == SHORTCUT_URL && Common.isValidURL(mStore.getString("url")))) {
                return true;
            }
        }
        return false;
    }
}
