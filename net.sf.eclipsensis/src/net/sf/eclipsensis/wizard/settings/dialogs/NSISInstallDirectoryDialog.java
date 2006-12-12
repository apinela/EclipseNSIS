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
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.NSISInstallDirectory;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallDirectoryDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();

    static {
        cProperties.add("destination"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("overwriteMode"); //$NON-NLS-1$
        cProperties.add("recursive"); //$NON-NLS-1$
    }

    public NSISInstallDirectoryDialog(NSISWizard wizard, NSISInstallDirectory item)
    {
        super(wizard, item);
        mStore.setDefault("overwriteMode",OVERWRITE_ON); //$NON-NLS-1$
        mStore.setDefault("recursive",true); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    protected List getProperties()
    {
        return cProperties;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_folderdlg_context"; //$NON-NLS-1$
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

        final Text t = NSISWizardDialogUtil.createDirectoryBrowser(composite,mStore.getString("name"), //$NON-NLS-1$
                            "wizard.source.directory.label",true,null,true); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,
                                                         NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES),
                                                         mStore.getString("destination"), //$NON-NLS-1$
                                                         false,"wizard.destination.label",true,null,true); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination",c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        GridData gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.OVERWRITE_MODE_NAMES,mStore.getInt("overwriteMode"), //$NON-NLS-1$
                true,"wizard.overwrite.label",true,null,false); //$NON-NLS-1$
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("overwrite",c2.getSelectionIndex()); //$NON-NLS-1$
            }
        });

        final Button cb1 = NSISWizardDialogUtil.createCheckBox(composite,"wizard.recursive.label",mStore.getBoolean("recursive"),true,null,false); //$NON-NLS-1$ //$NON-NLS-2$
        cb1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("recursive",cb1.getSelection()); //$NON-NLS-1$
            }
        });
        ((GridData)cb1.getLayoutData()).horizontalSpan = 3;
        return composite;
    }

    protected boolean hasRequiredFields()
    {
        return true;
    }

    protected String checkForErrors()
    {
        if(!IOUtility.isValidPath(IOUtility.decodePath(mStore.getString("name")))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.directory.name"); //$NON-NLS-1$
        }
        else if(!IOUtility.isValidNSISPathName(mStore.getString("destination"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.directory.destination"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }
}
