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

import java.text.Collator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplateManager;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardWelcomePage extends AbstractNSISWizardStartPage
{
    public static final String NAME = "nsisWizardWelcome"; //$NON-NLS-1$

    private NSISWizardTemplate mTemplate = null;
    private boolean mCreateFromTemplate = false;
    private boolean mUsingTemplate = false;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardWelcomePage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.welcome.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.welcome.description")); //$NON-NLS-1$
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizwelcome_context"; //$NON-NLS-1$
    }

    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        final Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.header", true, null, false); //$NON-NLS-1$
        l.setFont(JFaceResources.getBannerFont());
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label l2 = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.text", true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)l2.getLayoutData();
        Dialog.applyDialogFont(l2);
        gridData.widthHint = Common.calculateControlSize(l2,80,0).x;

        NSISWizardDialogUtil.createLabel(composite,"wizard.required.text", true, null, true); //$NON-NLS-1$

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

        validatePage(1);

        return composite;
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
        Label l = NSISWizardDialogUtil.createLabel(composite,"create.from.template.label",b.getSelection(),m,true); //$NON-NLS-1$
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
        l = NSISWizardDialogUtil.createLabel(composite,"wizard.template.description.label",true,m,false); //$NON-NLS-1$
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
        final NSISWizardTemplateManager templateManager = ((NSISScriptWizard)mWizard).getTemplateManager();
        viewer.setInput(templateManager.getTemplates());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        viewer.setSorter(new ViewerSorter(collator));

        ViewerFilter filter = new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof NSISWizardTemplate) {
                    NSISWizardTemplate template = (NSISWizardTemplate)element;
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
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof NSISWizardTemplate) {
                        mTemplate = (NSISWizardTemplate)obj;
                        t.setText(mTemplate.getDescription());
                    }
                }
                else {
                    mTemplate = null;
                }
                validatePage(0xffff);
            }
        });
        
        viewer.getList().addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                if(canFlipToNextPage()) {
                    IWizardPage nextPage = getNextPage();
                    if(nextPage != null) {
                        getContainer().showPage(nextPage);
                    }
                }
            }
        });

        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mCreateFromTemplate = b.getSelection();
                validatePage(0xffff);
            }
        });

        m.updateSlaves();

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isPreviousPage() && !isCurrentPage()) {
                    if(getSelectedPage() instanceof AbstractNSISWizardPage) {
                        if(mWizard instanceof NSISScriptWizard) {
                            NSISScriptWizard scriptWizard = (NSISScriptWizard)mWizard;
                            if(!mCreateFromTemplate) {
                                if(mUsingTemplate) {
                                    scriptWizard.initSettings();
                                    scriptWizard.setTemplate(null);
                                    mUsingTemplate = false;
                                }
                            }
                            else {
                                if(mTemplate != null) {
                                    scriptWizard.loadTemplate(mTemplate);
                                    mTemplate = null;
                                    mUsingTemplate = true;
                                }
                            }
                        }
                    }
                }
            }
        });
        return group;
    }

    public boolean validatePage(int flag)
    {
        boolean b = !mCreateFromTemplate || mTemplate != null;
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("select.template.error")); //$NON-NLS-1$
        }
        return b;
    }
}
