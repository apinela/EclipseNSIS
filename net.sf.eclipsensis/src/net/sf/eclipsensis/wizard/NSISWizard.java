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

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public abstract class NSISWizard extends Wizard implements IAdaptable, INewWizard, INSISWizardConstants
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;
    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 450;

    private NSISWizardSettings mSettings = null;
    private ArrayList mSettingsListeners = new ArrayList();
    private IPageChangeProvider mPageChangeProvider;
    private AbstractNSISWizardPage mCurrentPage = null;
    
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

    void setCurrentPage(AbstractNSISWizardPage currentPage)
    {
        mCurrentPage = currentPage;
        if(mPageChangeProvider instanceof PageChangeProvider) {
            ((PageChangeProvider)mPageChangeProvider).firePageChanged();
        }
    }

    public Object getAdapter(Class adapter)
    {
        if(IPageChangeProvider.class.equals(adapter)) {
            if(mPageChangeProvider == null) {
                mPageChangeProvider = new PageChangeProvider();
            }
            return mPageChangeProvider;
        }
        return null;
    }

    public void setContainer(IWizardContainer wizardContainer)
    {
        if(getContainer() == mPageChangeProvider) {
            mPageChangeProvider = null;
        }
        super.setContainer(wizardContainer);
        if(getContainer() != null && getContainer() instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider)getContainer();
            if(mPageChangeProvider != null && mPageChangeProvider instanceof PageChangeProvider) {
                for(Iterator iter=((PageChangeProvider)mPageChangeProvider).getListeners().iterator(); iter.hasNext(); ) {
                    pageChangeProvider.addPageChangedListener((IPageChangedListener)iter.next());
                    iter.remove();
                }
                mPageChangeProvider = null;
            }
            mPageChangeProvider = pageChangeProvider;
        }
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

    public void createPageControls(Composite pageContainer)
    {
        super.createPageControls(pageContainer);
        Object data = pageContainer.getLayoutData();
        if(data instanceof GridData) {
            GridData d = (GridData)data;
            d.widthHint = SIZING_WIZARD_WIDTH;
            d.heightHint = SIZING_WIZARD_HEIGHT;
        }
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

    public abstract String getHelpContextId();
    
    private class PageChangeProvider implements IPageChangeProvider
    {
        private List mListeners = new ArrayList();
        
        public void addPageChangedListener(IPageChangedListener listener)
        {
            if(!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
        
        public List getListeners()
        {
            return mListeners;
        }

        public Object getSelectedPage()
        {
            return mCurrentPage;
        }

        public void removePageChangedListener(IPageChangedListener listener)
        {
            if(!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        public void firePageChanged()
        {
            PageChangedEvent pageChangedEvent = new PageChangedEvent(this, mCurrentPage);
            for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
                ((IPageChangedListener)iter.next()).pageChanged(pageChangedEvent);
            }
        }
    }
}