/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardTemplatePage extends AbstractNSISWizardStartPage
{
    public static final String NAME = "nsisWizardTemplate"; //$NON-NLS-1$

    private NSISWizardTemplate mTemplate = null;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardTemplatePage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.template.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.template.description")); //$NON-NLS-1$
    }

    protected boolean hasRequiredFields()
    {
        return true;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_scrtmpltdlg_context"; //$NON-NLS-1$
    }

    protected Control createPageControl(Composite parent)
    {
        mTemplate = mWizard.getTemplate();
        final Composite composite = new Composite(parent, SWT.NONE);

        final GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);
        ((GridLayout)composite.getLayout()).numColumns=2;

        Text t = NSISWizardDialogUtil.createText(composite,mTemplate.getName(),"template.dialog.name.label",true,null,true); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mTemplate.setName(((Text)e.widget).getText());
                validatePage(0xffff);
            }
        });

        Label l = NSISWizardDialogUtil.createLabel(composite,"template.dialog.description.label",true,null,false); //$NON-NLS-1$
        GridData data = (GridData)NSISWizardDialogUtil.getLayoutControl(l).getLayoutData();
        data.horizontalSpan=2;

        t = NSISWizardDialogUtil.createText(composite,mTemplate.getDescription(),SWT.BORDER|SWT.MULTI|SWT.WRAP,2,true,null);
        Dialog.applyDialogFont(t);
        data = (GridData)t.getLayoutData();
        data.horizontalAlignment=GridData.FILL;
        data.grabExcessHorizontalSpace=true;
        data.heightHint = Common.calculateControlSize(t,0,10).y;

        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mTemplate.setDescription(((Text)e.widget).getText());
            }
        });

        Button b = NSISWizardDialogUtil.createCheckBox(composite,"template.dialog.enabled.label",true,true,null,false); //$NON-NLS-1$
        data = (GridData)b.getLayoutData();
        data.horizontalSpan=2;
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mTemplate.setEnabled(((Button)e.widget).getSelection());
            }
        });

        validatePage(0xffff);

        return composite;
    }

    public boolean validatePage(int flag)
    {
        boolean b = !Common.isEmpty(mTemplate.getName());
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("wizard.template.missing.name.error")); //$NON-NLS-1$
        }
        return b;
    }

}
