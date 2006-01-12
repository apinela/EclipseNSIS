/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.wizard;

import java.text.Collator;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplate;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplateManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class InstallOptionsWizardPage extends WizardPage
{
    public static final String NAME = "installOptionsWizardPage"; //$NON-NLS-1$
    private boolean mCreateFromTemplate = false;
	
    /**
     * Creates the page for the readme creation wizard.
     *
     * @param workbench  the workbench on which the page should be created
     * @param selection  the current selection
     */
    public InstallOptionsWizardPage()
    {
    	super(NAME);
    	this.setTitle(InstallOptionsPlugin.getResourceString("wizard.page.title")); //$NON-NLS-1$
    	this.setDescription(InstallOptionsPlugin.getResourceString("wizard.page.description")); //$NON-NLS-1$
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX+"installoptions_wizard_context"); //$NON-NLS-1$

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        final Label l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("wizard.page.header"), true, null, false); //$NON-NLS-1$
        l.setFont(JFaceResources.getBannerFont());
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label l2 = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("wizard.page.text"), true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)l2.getLayoutData();
        Dialog.applyDialogFont(l2);
        gridData.widthHint = Common.calculateControlSize(l2,80,0).x;

        createTemplatesGroup(composite);

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

        setPageComplete(validatePage());
    }

    private Group createTemplatesGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, null,null,false);
        ((GridLayout)group.getLayout()).makeColumnsEqualWidth = true;
        GridData data = (GridData)group.getLayoutData();
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = GridData.FILL;

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.from.template.button.text",false,true,null,false); //$NON-NLS-1$

        MasterSlaveController m = new MasterSlaveController(b);
        SashForm form = new SashForm(group,SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        form.setLayoutData(data);

        MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                return true;
            }

            public void enabled(Control control, boolean flag)
            {
                int id = (flag?SWT.COLOR_LIST_BACKGROUND:SWT.COLOR_WIDGET_BACKGROUND);
                control.setBackground(getShell().getDisplay().getSystemColor(id));
            }
        };

        Composite composite = new Composite(form,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Label l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("create.from.template.label"),b.getSelection(),m,false); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final List list = new List(composite,SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        list.setLayoutData(data);
        m.addSlave(list, mse);

        composite = new Composite(form,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("template.description.label"),true,m,false); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final StyledText t = new StyledText(composite,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        t.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        t.setCursor(null);
        t.setCaret(null);
        m.addSlave(t, mse);

        final ListViewer viewer = new ListViewer(list);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new CollectionLabelProvider());
        viewer.setInput(InstallOptionsTemplateManager.INSTANCE.getTemplates());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        viewer.setSorter(new ViewerSorter(collator));

        ViewerFilter filter = new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof InstallOptionsTemplate) {
                    InstallOptionsTemplate template = (InstallOptionsTemplate)element;
                    return template.isEnabled() && !template.isDeleted();
                }
                return true;
            }
        };
        viewer.addFilter(filter);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection sel = event.getSelection();
                InstallOptionsWizard wizard = (InstallOptionsWizard)getWizard();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof InstallOptionsTemplate) {
                        wizard.setTemplate((InstallOptionsTemplate)obj);
                        t.setText(wizard.getTemplate().getDescription());
                    }
                }
                else {
                    wizard.setTemplate(null);
                }
                setPageComplete(validatePage());
            }
        });

        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mCreateFromTemplate = b.getSelection();
                setPageComplete(validatePage());
            }
        });

        m.updateSlaves();
        return group;
    }

    public boolean validatePage()
    {
        boolean b = !mCreateFromTemplate || ((InstallOptionsWizard)getWizard()).getTemplate() != null;
        if(b) {
            setErrorMessage(null);
        }
        else {
            setErrorMessage(InstallOptionsPlugin.getResourceString("select.template.error")); //$NON-NLS-1$
        }
        return b;
    }
}
