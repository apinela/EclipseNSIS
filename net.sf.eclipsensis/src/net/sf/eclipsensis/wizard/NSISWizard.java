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
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.core.runtime.CoreException;
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
    private ArrayList mListeners = new ArrayList();
    
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
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), EclipseNSISPlugin.getResourceString("error.title"), realException.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	private void doFinish(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating ", 2);
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
                try {
                    Thread.sleep(5000);
//				IWorkbenchPage page =
//					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				try {
//					IDE.openEditor(page, (IFile)null, true);
//				} catch (PartInitException e) {
//				}
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
			}
		});
		monitor.worked(1);
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
        notifyListeners(page, nextPage, true);
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
        notifyListeners(page, prevPage, false);
        return prevPage;
    }

    private void notifyListeners(IWizardPage leavePage, IWizardPage enterPage, boolean forward)
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            INSISWizardListener listener = (INSISWizardListener) iter.next();
            listener.aboutToLeave(leavePage, forward);
            listener.aboutToEnter(enterPage, forward);
        }
    }

    public void addNSISWizardListener(INSISWizardListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(INSISWizardListener listener)
    {
        mListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}