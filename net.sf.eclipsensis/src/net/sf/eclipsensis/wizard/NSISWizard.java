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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup;
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
    private String mTemplateName = ""; //$NON-NLS-1$
    private ArrayList mSettingsListeners = new ArrayList();
    private static File cTemplateFolder = null;
    
    static {
        cTemplateFolder = new File(EclipseNSISPlugin.getPluginStateLocation(),"wizard"); //$NON-NLS-1$
        if(cTemplateFolder.exists()) {
            if(!cTemplateFolder.isDirectory()) {
                cTemplateFolder.delete();
            }
        }
        cTemplateFolder.mkdirs();
    }

    /**
     * @return Returns the templateFolder.
     */
    public static File getTemplateFolder()
    {
        return cTemplateFolder;
    }

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
        mSettings.setWizard(this);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() 
    {
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
    		addPage(new NSISWizardWelcomePage());
            addPage(new NSISWizardGeneralPage());
            addPage(new NSISWizardAttributesPage());
            addPage(new NSISWizardPresentationPage());
            addPage(new NSISWizardContentsPage());
            addPage(new NSISWizardCompletionPage());
        }
        else {
            String error = EclipseNSISPlugin.getResourceString("wizard.unconfigured.error"); //$NON-NLS-1$
            MessageDialog.openError(getShell(),mWindowTitle,
                                    error);
            throw new UnsupportedOperationException(error);
        }
	}

    /**
     * @return Returns the settings.
     */
    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }
   
    /**
     * @return Returns the templateName.
     */
    public String getTemplateName()
    {
        return mTemplateName;
    }

    void saveTemplate(String templateName) throws IOException
    {
        Common.writeObjectToXMLFile(new File(cTemplateFolder,templateName+WIZARD_TEMPLATE_EXTENSION), mSettings);
        mTemplateName = templateName;
    }
    
    void loadTemplate(String templateName) throws IOException, ClassNotFoundException
    {
        mSettings = (NSISWizardSettings)Common.readObjectFromXMLFile(new File(cTemplateFolder,templateName+WIZARD_TEMPLATE_EXTENSION));
        mSettings.setWizard(this);
        AbstractNSISInstallGroup installer = (AbstractNSISInstallGroup)mSettings.getInstaller();
        installer.setExpanded(true,true);
        installer.resetChildTypes(true);
        mTemplateName = templateName;
        for(Iterator iter=mSettingsListeners.iterator(); iter.hasNext(); ) {
            ((INSISWizardSettingsListener)iter.next()).settingsChanged();
        }
        IWizardPage[] pages = getPages();
        for (int i = 0; i < pages.length; i++) {
            ((AbstractNSISWizardPage)pages[i]).validatePage(0xFFFF);
        }
        getContainer().updateButtons();
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

    public void addSettingsListener(INSISWizardSettingsListener listener)
    {
        mSettingsListeners.add(listener);
    }

    public void removeSettingsListener(INSISWizardSettingsListener listener)
    {
        mSettingsListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}