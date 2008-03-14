/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
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

        final Text t = NSISWizardDialogUtil.createText(composite,mTemplate==null?"":mTemplate.getName(),"template.dialog.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if(mTemplate != null) {
                    mTemplate.setName(((Text)e.widget).getText());
                    validatePage(VALIDATE_ALL);
                }
            }
        });

        Label l = NSISWizardDialogUtil.createLabel(composite,"template.dialog.description.label",true,null,false); //$NON-NLS-1$
        GridData data = (GridData)l.getLayoutData();
        data.horizontalSpan=2;

        final Text t2 = NSISWizardDialogUtil.createText(composite,mTemplate==null?"":mTemplate.getDescription(),SWT.BORDER|SWT.MULTI|SWT.WRAP,2,true,null); //$NON-NLS-1$
        Dialog.applyDialogFont(t2);
        data = (GridData)t2.getLayoutData();
        data.horizontalAlignment=GridData.FILL;
        data.grabExcessHorizontalSpace=true;
        data.heightHint = Common.calculateControlSize(t2,0,10).y;

        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if (mTemplate != null) {
                    mTemplate.setDescription(((Text)e.widget).getText());
                }
            }
        });

        final Button b = NSISWizardDialogUtil.createCheckBox(composite,"template.dialog.enabled.label",mTemplate != null?mTemplate.isEnabled():false,true,null,false); //$NON-NLS-1$
        data = (GridData)b.getLayoutData();
        data.horizontalSpan=2;
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (mTemplate != null) {
                    mTemplate.setEnabled(((Button)e.widget).getSelection());
                }
            }
        });

        if(mWizard instanceof NSISTemplateWizard) {
            ((NSISTemplateWizard)mWizard).addTemplateListener(new INSISWizardTemplateListener() {
                public void templateChanged(NSISWizardTemplate oldTemplate, NSISWizardTemplate newTemplate)
                {
                    mTemplate = newTemplate;
                    if(mTemplate != null) {
                        t.setText(mTemplate.getName());
                        t2.setText(mTemplate.getDescription());
                        b.setSelection(mTemplate.isEnabled());
                    }
                    else {
                        t.setText(""); //$NON-NLS-1$
                        t2.setText(""); //$NON-NLS-1$
                        b.setSelection(false);
                    }
                    validatePage(VALIDATE_ALL);
                }
            });
        }
        validatePage(VALIDATE_ALL);

        return composite;
    }

    public boolean validatePage(int flag)
    {
        boolean b = !Common.isEmpty(mTemplate != null?mTemplate.getName():""); //$NON-NLS-1$
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
