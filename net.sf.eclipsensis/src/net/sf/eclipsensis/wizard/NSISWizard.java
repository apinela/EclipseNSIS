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

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public abstract class NSISWizard extends Wizard implements INewWizard, INSISWizardConstants
{
    private NSISWizardSettings mSettings = null;
    private ArrayList mSettingsListeners = new ArrayList();
    
    /**
	 * Constructor for NSISWizard.
	 */
	public NSISWizard() 
    {
		super();
        setTitleBarColor(ColorManager.WHITE);
	}
	
    void initSettings()
    {
        setSettings(new NSISWizardSettings());
    }

    /**
	 * Adding the page to the wizard.
	 */
	public final void addPages() 
    {
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            initSettings();
    		addStartPage();
            addPage(new NSISWizardGeneralPage());
            addPage(new NSISWizardAttributesPage());
            addPage(new NSISWizardPresentationPage());
            addPage(new NSISWizardContentsPage());
            addPage(new NSISWizardCompletionPage());
        }
        else {
            String error = EclipseNSISPlugin.getFormattedString("wizard.unconfigured.error", new Object[]{getWindowTitle()}); //$NON-NLS-1$
            Common.openError(getShell(), error);
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
   
    protected void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
        mSettings.setWizard(this);
        AbstractNSISInstallGroup installer = (AbstractNSISInstallGroup)mSettings.getInstaller();
        installer.setExpanded(true,true);
        installer.resetChildTypes(true);
        for(Iterator iter=mSettingsListeners.iterator(); iter.hasNext(); ) {
            ((INSISWizardSettingsListener)iter.next()).settingsChanged();
        }
        IWizardPage[] pages = getPages();
        if(!Common.isEmptyArray(pages)) {
            for (int i = 0; i < pages.length; i++) {
                ((AbstractNSISWizardPage)pages[i]).validatePage(0xFFFF);
            }
            getContainer().updateButtons();
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

    protected abstract void addStartPage();

    protected NSISWizardTemplate mTemplate = null;

    /**
     * @return Returns the template.
     */
    protected NSISWizardTemplate getTemplate()
    {
        return mTemplate;
    }

    /**
     * @param template The template to set.
     */
    protected void setTemplate(NSISWizardTemplate template)
    {
        mTemplate = template;
    }
}