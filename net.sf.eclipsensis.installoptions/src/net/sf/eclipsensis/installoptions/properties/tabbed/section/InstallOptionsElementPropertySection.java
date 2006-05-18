/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import net.sf.eclipsensis.installoptions.model.InstallOptionsElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public abstract class InstallOptionsElementPropertySection extends AbstractPropertySection
{
	private InstallOptionsElement mElement;
    private InstallOptionsCommandHelper mCommandHelper;

    private TabbedPropertySheetPage mPage;
    private Composite mParent;
    
    public void createControls(Composite parent, TabbedPropertySheetPage page)
    {
        super.createControls(parent, page);
        mPage = page;
        mParent = getWidgetFactory().createComposite(parent);
        mParent.setLayout(new GridLayout(1,false));
    }

    public void dispose()
    {
        super.dispose();
        mPage = null;
        if(mParent != null && !mParent.isDisposed()) {
            mParent.dispose();
        }
    }

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public final void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object input = ((IStructuredSelection)selection).getFirstElement();
            InstallOptionsElement element = getElement(input);
            if(element != null && !Common.objectsAreEqual(mElement, element)) {
                mElement = element;
                CommandStack stack = null;
                if(input instanceof EditPart) {
                    stack = ((EditPart)input).getViewer().getEditDomain().getCommandStack();
                }
                if(mCommandHelper != null) {
                    if(!Common.objectsAreEqual(stack, mCommandHelper.getCommandStack())) {
                        mCommandHelper.dispose();
                        createCommandHelper(stack);
                    }
                }
                else {
                    createCommandHelper(stack);
                }
                inputChanged(mElement);
            }
        }
	}
    
    private InstallOptionsElement getElement(Object input)
    {
        if(input instanceof InstallOptionsElement) {
            return (InstallOptionsElement)input;
        }
        else if(input instanceof EditPart) {
            return getElement(((EditPart)input).getModel());
        }
        return null;
    }

    /**
     * @param stack
     */
    private void createCommandHelper(CommandStack stack)
    {
        mCommandHelper = new InstallOptionsCommandHelper(stack) {
            protected void refresh()
            {
                InstallOptionsElementPropertySection.this.refresh();
            }
        };
    }

    private void inputChanged(InstallOptionsElement newElement)
    {
        if(newElement != null && mParent != null && !mParent.isDisposed()) {
            Control[] controls = mParent.getChildren();
            if(!Common.isEmptyArray(controls)) {
                for (int i = 0; i < controls.length; i++) {
                    if(controls[i] != null && !controls[i].isDisposed()) {
                        controls[i].dispose();
                    }
                }
            }
            mParent.layout(true, true);
            Control c = createSection(newElement, mParent, mPage, mCommandHelper);
            if(c != null) {
                c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
                mParent.getShell().layout(new Control[] {c});
            }
        }
    }

    public boolean shouldUseExtraSpace() 
    {
        return true;
    }

    protected abstract Control createSection(InstallOptionsElement element, Composite parent, TabbedPropertySheetPage page, InstallOptionsCommandHelper commandHelper);
}
