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

import java.util.Arrays;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.template.InstallOptionsTemplate;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class InstallOptionsWizard extends Wizard implements INewWizard
{
    private static final Image cShellImage = InstallOptionsPlugin.getShellImage();

    private InstallOptionsTemplate mTemplate = null;
	private IStructuredSelection mSelection;
	private IWorkbench mWorkbench;
    private IPageChangedListener mPageChangedListener = new IPageChangedListener() {
        private Image mOldImage;
        private Image[] mOldImages;

        public void pageChanged(PageChangedEvent event)
        {
            Shell shell = getContainer().getShell();
            Image image = shell.getImage();
            if(Arrays.asList(getPages()).contains(event.getSelectedPage())) {
                if(image != cShellImage) {
                    mOldImage = image;
                    mOldImages = shell.getImages();
                    shell.setImage(cShellImage);
                }
            }
            else {
                if(image == cShellImage) {
                    shell.setImage(mOldImage);
                    shell.setImages(mOldImages);
                }
            }
        }
    };

    /** (non-Javadoc)
     * Method declared on Wizard.
     */
    public void addPages()
    {
    	addPage(new InstallOptionsWizardPage());
        addPage(new InstallOptionsWizardPage2(mWorkbench, mSelection));
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

    public void setContainer(IWizardContainer wizardContainer)
    {
        if(getContainer() instanceof IPageChangeProvider) {
            ((IPageChangeProvider)getContainer()).removePageChangedListener(mPageChangedListener);
        }
        super.setContainer(wizardContainer);
        if(getContainer() instanceof IPageChangeProvider) {
            ((IPageChangeProvider)getContainer()).addPageChangedListener(mPageChangedListener);
        }
    }

    protected InstallOptionsTemplate getTemplate()
    {
        return mTemplate;
    }

    protected void setTemplate(InstallOptionsTemplate template)
    {
        mTemplate = template;
    }

    /** (non-Javadoc)
     * Method declared on IWizard
     */
    public boolean performFinish()
    {
    	return ((InstallOptionsWizardPage2)getPages()[getPageCount()-1]).finish();
    }
}
