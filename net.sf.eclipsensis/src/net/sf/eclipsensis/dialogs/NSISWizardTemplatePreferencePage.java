/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.AbstractTemplateSettings;
import net.sf.eclipsensis.wizard.NSISTemplateWizard;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplateManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class NSISWizardTemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private AbstractTemplateSettings mTemplateSettings;

    /**
     * 
     */
    public NSISWizardTemplatePreferencePage()
    {
        super();
        setDescription(EclipseNSISPlugin.getResourceString("wizard.template.preferences.description")); //$NON-NLS-1$
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_scrtmpltprefs_context"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite ancestor)
    {
        mTemplateSettings =  new AbstractTemplateSettings(ancestor, SWT.NONE, new NSISWizardTemplateManager()) {
            private Button mAddButton;

            protected void createButtons(Composite parent)
            {
                mAddButton= new Button(parent, SWT.PUSH);
                mAddButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.new.label")); //$NON-NLS-1$
                mAddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                mAddButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        add();
                    }
                });
                super.createButtons(parent);
            }

            private void add() 
            {
                NSISWizardTemplate template = new NSISWizardTemplate(""); //$NON-NLS-1$
                Dialog dialog= createDialog(template);
                if (dialog.open() != Window.CANCEL) {
                    getTemplateManager().addTemplate(template);
                    getTableViewer().refresh(true);
                    getTableViewer().setChecked(template, template.isEnabled());
                    getTableViewer().setSelection(new StructuredSelection(template));           
                }
            }

            protected AbstractTemplate createTemplate(String name)
            {
                return new NSISWizardTemplate(name);
            }

            protected Dialog createDialog(AbstractTemplate template)
            {
                final NSISWizardTemplate wizardTemplate = (NSISWizardTemplate)template;
                final NSISTemplateWizardDialog[] wizardDialog = new NSISTemplateWizardDialog[1];
                BusyIndicator.showWhile(getShell().getDisplay(),new Runnable() {
                    public void run()
                    {
                        wizardDialog[0] = new NSISTemplateWizardDialog(getShell(),new NSISTemplateWizard(wizardTemplate));
                        wizardDialog[0].create();
                    }
                });
                return wizardDialog[0];
            }
        };
        
        return mTemplateSettings;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }
    
    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() 
    {
        mTemplateSettings.performDefaults();
    }

    /*
     * @see PreferencePage#performOk()
     */ 
    public boolean performOk() 
    {
        if(mTemplateSettings.performOk()) {
            return super.performOk();
        }
        return false;
    }   
}
