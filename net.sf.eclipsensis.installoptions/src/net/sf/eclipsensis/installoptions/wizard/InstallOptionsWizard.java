/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.wizard;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.template.IInstallOptionsTemplate;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.WizardShellImageChanger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class InstallOptionsWizard extends Wizard implements INewWizard
{
    private static final Image cShellImage = InstallOptionsPlugin.getShellImage();

    private IInstallOptionsTemplate mTemplate = null;
    private IPageChangedListener mPageChangedListener;

    public InstallOptionsWizard()
    {
        super();
        setNeedsProgressMonitor(true);
        setTitleBarColor(ColorManager.WHITE);
        mPageChangedListener = new WizardShellImageChanger(this, cShellImage);
    }

    /** (non-Javadoc)
     * Method declared on Wizard.
     */
    @Override
    public void addPages()
    {
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            addPage(new InstallOptionsWizardPage());
        }
        else {
            String error = EclipseNSISPlugin.getFormattedString("wizard.unconfigured.error", new Object[]{getWindowTitle()}); //$NON-NLS-1$
            Common.openError(getShell(), error, InstallOptionsPlugin.getShellImage());
            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleUIJob("", new IJobStatusRunnable() { //$NON-NLS-1$
                public IStatus run(IProgressMonitor monitor)
                {
                    IWizardContainer container = getContainer();
                    if (container != null) {
                        container.getShell().close();
                    }
                    return Status.OK_STATUS;
                }
            });
        }
    }

    /** (non-Javadoc)
     * Method declared on INewWizard
     */
    public void init(IWorkbench workbench,IStructuredSelection selection)
    {
        setWindowTitle(InstallOptionsPlugin.getResourceString("wizard.window.title")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("wizard.title.image"))); //$NON-NLS-1$
    }

    @Override
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

    protected IInstallOptionsTemplate getTemplate()
    {
        return mTemplate;
    }

    protected void setTemplate(IInstallOptionsTemplate template)
    {
        mTemplate = template;
    }

    /** (non-Javadoc)
     * Method declared on IWizard
     */
    @Override
    public boolean performFinish()
    {
        return ((InstallOptionsWizardPage)getPages()[0]).finish();
    }
}
