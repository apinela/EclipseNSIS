/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.lang.reflect.InvocationTargetException;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NSISWizard extends Wizard implements INewWizard, INSISWizardConstants
{
    private NSISWizardSettings mSettings = new NSISWizardSettings();
    private String mWindowTitle = ""; //$NON-NLS-1$
    
	/**
	 * Constructor for NSISWizard.
	 */
	public NSISWizard() 
    {
		super();
		setNeedsProgressMonitor(true);
        mWindowTitle = EclipseNSISPlugin.getResourceString("wizard.window.title"); //$NON-NLS-1$
        setWindowTitle(mWindowTitle);
        setTitleBarColor(ColorManager.WHITE);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() 
    {
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
    		addPage(new NSISWizardWelcomePage(mSettings));
            addPage(new NSISWizardGeneralPage(mSettings));
            addPage(new NSISWizardAttributesPage(mSettings));
            addPage(new NSISWizardPresentationPage(mSettings));
            addPage(new NSISWizardContentsPage(mSettings));
            addPage(new NSISWizardCompletionPage(mSettings));
        }
        else {
            String error = EclipseNSISPlugin.getResourceString("wizard.unconfigured.error"); //$NON-NLS-1$
            MessageDialog.openError(getShell(),mWindowTitle,
                                    error);
            throw new UnsupportedOperationException(error);
        }
	}

	public boolean performFinish() 
    {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException
            {
				try {
                    new NSISWizardScriptGenerator(mSettings).generate(getShell(),monitor);
                }
				catch (Exception e) {
				    throw new InvocationTargetException(e);
                }
			}
		};
		try {
			getContainer().run(true, false, op);
		} 
        catch (InterruptedException e) {
			return false;
		} 
        catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), EclipseNSISPlugin.getResourceString("error.title"), realException.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performCancel()
     */
    public boolean performCancel()
    {
        if(MessageDialog.openQuestion(getShell(),EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("wizard.cancel.confirm"))) { //$NON-NLS-1$
            return super.performCancel();
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page)
    {
        IWizardPage nextPage = super.getNextPage(page);
        if(mSettings.getInstallerType() == INSTALLER_TYPE_SILENT && nextPage != null &&  
           nextPage.getName().equals(NSISWizardPresentationPage.NAME)) {
            nextPage = super.getNextPage(nextPage);
        }
        return nextPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getPreviousPage(IWizardPage page)
    {
        IWizardPage prevPage = super.getPreviousPage(page);
        if(mSettings.getInstallerType() == INSTALLER_TYPE_SILENT && prevPage != null &&
           prevPage.getName().equals(NSISWizardPresentationPage.NAME)) {
            prevPage = super.getNextPage(prevPage);
        }
        return prevPage;
    }

    /* (non-Javadoc)
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}