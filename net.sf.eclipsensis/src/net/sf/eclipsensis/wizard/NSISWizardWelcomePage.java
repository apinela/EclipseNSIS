/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISWizardTemplateDialog;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class NSISWizardWelcomePage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardWelcome"; //$NON-NLS-1$
    
    /**
     * @param pageName
     * @param title
     */
    public NSISWizardWelcomePage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.welcome.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.welcome.description")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.header", true, null, false); //$NON-NLS-1$
        l.setFont(JFaceResources.getBannerFont());
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Label l2 = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.text", true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)l2.getLayoutData();
        gridData.widthHint = WIDTH_HINT;

        NSISWizardDialogUtil.createLabel(composite,"wizard.required.text", true, null, true); //$NON-NLS-1$

        Composite composite2 = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginWidth = 0;
        composite2.setLayout(layout2);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite2.setLayoutData(data);
        
        final ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Label l3 = NSISWizardDialogUtil.createLabel(composite2,"load.wizard.template.label", true, null, false); //$NON-NLS-1$
        data = (GridData)l3.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = true;
        
        final Button button = new Button(composite2, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("load.wizard.template.button.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                NSISWizardTemplateDialog dialog = new NSISWizardTemplateDialog(getShell(),NSISWizardTemplateDialog.MODE_LOAD);
                dialog.setTemplateName(mWizard.getTemplateName());
                if(dialog.open() == Window.OK) {
                    String templateName = dialog.getTemplateName();
                    if(!Common.isEmpty(templateName)) {
                        try {
                            mWizard.loadTemplate(templateName);
                        }
                        catch(Exception ex) {
                            MessageDialog.openError(getShell(),bundle.getString("error.title"),ex.toString()); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        button.setLayoutData(data);
        
        composite.addListener (SWT.Resize,  new Listener () {
            boolean init = false;
            
            public void handleEvent (Event e) {
                if(init) {
                    Point size = composite.getSize();
                    gridData.widthHint = size.x - 2*layout.marginWidth;
                    composite.layout();
                }
                else {
                    init=true;
                }
            }
        });

        validatePage(1);
    }

    public boolean validatePage(int flag)
    {
        setPageComplete(true);
        return true;
    }
}
