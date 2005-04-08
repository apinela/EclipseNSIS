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

import java.text.Collator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallShortcut;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallShortcutDialog extends AbstractNSISInstallItemDialog implements INSISWizardConstants
{
    private static ArrayList cProperties = new ArrayList();
    
    static {
        cProperties.add("location"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("shortcutType"); //$NON-NLS-1$
        cProperties.add("path"); //$NON-NLS-1$
        cProperties.add("url"); //$NON-NLS-1$
    }

    public NSISInstallShortcutDialog(NSISWizard wizard, NSISInstallShortcut item)
    {
        super(wizard, item);
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_shortcutdlg_context"; //$NON-NLS-1$
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
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,null,"", //$NON-NLS-1$
                                                          false,"wizard.location.label",true,null,true); //$NON-NLS-1$
        GridData gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        ArrayList input = new ArrayList(Arrays.asList(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES));
        String temp = EclipseNSISPlugin.getResourceString("wizard.additional.shortcut.locations",""); //$NON-NLS-1$ //$NON-NLS-2$
        if(!Common.isEmpty(temp)) {
            String[] additionalPaths = Common.tokenize(temp,','); //$NON-NLS-1$
            for (int i = 0; i < additionalPaths.length; i++) {
                if(!input.contains(additionalPaths[i]))
                input.add(additionalPaths[i]);
            }
        }
        ComboViewer cv = new ComboViewer(c1);
        cv.setContentProvider(new CollectionContentProvider());
        cv.setLabelProvider(new CollectionLabelProvider());
        Collator coll = Collator.getInstance();
        coll.setStrength(Collator.PRIMARY);
        cv.setSorter(new ViewerSorter(coll));
        cv.setInput(input);
        c1.setText(mStore.getString("location")); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("location",c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Text t1 = NSISWizardDialogUtil.createText(composite,mStore.getString("name"),"wizard.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        t1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Button[] radio = NSISWizardDialogUtil.createRadioGroup(composite,NSISWizardDisplayValues.SHORTCUT_TYPE_NAMES,mStore.getInt("shortcutType"), //$NON-NLS-1$
                            "wizard.shortcut.type.label",true,null,false); //$NON-NLS-1$
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
                    mStore.setValue("shortcutType",n); //$NON-NLS-1$
                    validate();
                }
            }            
        };
        for (int i = 0; i < radio.length; i++) {
            radio[i].addSelectionListener(sa);
        }
        MasterSlaveController m1 = new MasterSlaveController(radio[SHORTCUT_URL]);
        MasterSlaveController m2 = new MasterSlaveController(radio[SHORTCUT_INSTALLELEMENT]);
        
        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("url"),"wizard.url.label",true,m1,true); //$NON-NLS-1$ //$NON-NLS-2$
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("url",t2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        
        final Combo c2 = NSISWizardDialogUtil.createContentBrowser(composite, "wizard.path.label", mStore.getString("path"), mWizard, true, m2, true); //$NON-NLS-1$ //$NON-NLS-2$

        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("path",c2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        m2.updateSlaves();
        m1.updateSlaves();
        
        return composite;
    }
    
    protected String checkForErrors()
    {
        String subKey = mStore.getString("subKey").trim(); //$NON-NLS-1$
        if(!Common.isValidNSISPathName(mStore.getString("location"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.location"); //$NON-NLS-1$
        }
        else if(!Common.isValidFileName(mStore.getString("name"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.name"); //$NON-NLS-1$
        }
        else {
            int n = mStore.getInt("shortcutType"); //$NON-NLS-1$
            if((n == SHORTCUT_INSTALLELEMENT && !Common.isValidNSISPathName(mStore.getString("path")))) { //$NON-NLS-1$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.file"); //$NON-NLS-1$
            }
            else if((n == SHORTCUT_URL && !Common.isValidURL(mStore.getString("url")))) { //$NON-NLS-1$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.url"); //$NON-NLS-1$
            }
            else {
                return ""; //$NON-NLS-1$
            }
        }
    }
}
