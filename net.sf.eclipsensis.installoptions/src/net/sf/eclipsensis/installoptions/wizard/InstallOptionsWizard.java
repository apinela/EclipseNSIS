/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.wizard;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class InstallOptionsWizard extends Wizard implements INewWizard 
{
	private IStructuredSelection mSelection;
	private IWorkbench mWorkbench;
	private InstallOptionsWizardPage mPage;
    
    /** (non-Javadoc)
     * Method declared on Wizard.
     */
    public void addPages() 
    {
    	mPage = new InstallOptionsWizardPage(mWorkbench, mSelection);
    	addPage(mPage);
    }
    
    /** (non-Javadoc)
     * Method declared on INewWizard
     */
    public void init(IWorkbench workbench,IStructuredSelection selection) 
    {
    	mWorkbench = workbench;
    	mSelection = selection;
    	setWindowTitle(InstallOptionsPlugin.getResourceString("wizard.window.title")); //$NON-NLS-1$
    	setDefaultPageImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("wizard.title.image"))); //$NON-NLS-1$
    }
    
    /** (non-Javadoc)
     * Method declared on IWizard
     */
    public boolean performFinish() 
    {
    	return mPage.finish();
    }
}
