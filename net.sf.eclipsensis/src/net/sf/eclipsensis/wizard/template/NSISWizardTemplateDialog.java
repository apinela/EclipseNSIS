/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.IOException;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardTemplateDialog extends Dialog implements INSISWizardConstants
{
    private NSISWizardSettings mSettings = null;
    private NSISWizardTemplate mTemplate = null;
    private NSISWizardTemplateManager mTemplateManager = null;
    private Text mTemplateName = null;
    private Text mTemplateDescription = null;
    private Button mTemplateEnabled = null;

    /**
     * @param parentShell
     */
    public NSISWizardTemplateDialog(Shell parentShell, NSISWizardTemplateManager templateManager, NSISWizardTemplate template, NSISWizardSettings settings)
    {
        super(parentShell);
        mSettings = settings;
        mTemplate = template;
        mTemplateManager = templateManager;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText(EclipseNSISPlugin.getResourceString("wizard.template.dialog.title")); //$NON-NLS-1$
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        ((GridLayout)composite.getLayout()).numColumns=2;
        
        mTemplateName = NSISWizardDialogUtil.createText(composite,(mTemplate==null?"":mTemplate.getName()),"wizard.template.dialog.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        mTemplateName.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) 
            {
                getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(((Text)e.widget).getText()));
            }
        });
        
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.template.dialog.description.label",true,null,false); //$NON-NLS-1$
        GridData data = (GridData)l.getLayoutData();
        data.horizontalSpan=2;
        
        mTemplateDescription = NSISWizardDialogUtil.createText(composite,(mTemplate==null?"":mTemplate.getDescription()),SWT.BORDER|SWT.MULTI|SWT.WRAP,2,true,null); //$NON-NLS-1$
        Dialog.applyDialogFont(mTemplateDescription);

        data = (GridData)mTemplateDescription.getLayoutData();
        data.heightHint = convertHeightInCharsToPixels(5);
        data.widthHint = convertWidthInCharsToPixels(60);

        mTemplateEnabled = NSISWizardDialogUtil.createCheckBox(composite,"wizard.template.dialog.boolean.label",(mTemplate==null?true:mTemplate.isEnabled()),true,null,false); //$NON-NLS-1$
        data = (GridData)mTemplateDescription.getLayoutData();
        data.horizontalSpan=2;
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        if(mTemplate == null) {
            mTemplate = new NSISWizardTemplate(mTemplateName.getText());
        }
        else {
            mTemplate.setName(mTemplateName.getText());
        }
        mTemplate.setDescription(mTemplateDescription.getText());
        mTemplate.setEnabled(mTemplateEnabled.getSelection());
        mTemplate.setSettings(mSettings);
        if(mTemplateManager.addTemplate(mTemplate)) {
            try {
                mTemplateManager.save();
                super.okPressed();
            }
            catch(IOException ioe) {
                Common.openError(getShell(),ioe.getLocalizedMessage());
            }
        }
    }
}
