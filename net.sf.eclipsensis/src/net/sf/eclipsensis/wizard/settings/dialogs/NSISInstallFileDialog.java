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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.NSISInstallFile;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallFileDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();
    
    static {
        cProperties.add("destination");
        cProperties.add("name");
        cProperties.add("overwriteMode");
    }

    public NSISInstallFileDialog(Shell parentShell, NSISInstallFile item)
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
        
        final Text t = NSISWizardDialogUtil.createFileBrowser(composite,mStore.getString("name"),false,
                            Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"wizard.source.file.filternames"),
                            Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"wizard.source.file.filters"),
                            "wizard.source.file.label",true,null,true);
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t.getText());
                setComplete(validate());
            }
        });
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,NSISKeywords.PREDEFINED_PATH_VARIABLES,mStore.getString("destination"),
                                                         false,"wizard.destination.label",true,null,false);
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination",c1.getText());
                setComplete(validate());
            }
        });
        gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.OVERWRITE_MODE_NAMES,mStore.getInt("overwriteMode"),
                true,"wizard.overwrite.label",true,null,false);
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("overwrite",c2.getSelectionIndex());
            }
        });
        return composite;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#validate()
     */
    protected boolean validate()
    {
        return Common.isValidFile(mStore.getString("name")) && Common.isValidNSISPrefixedPathName(mStore.getString("destination"));
    }
}
