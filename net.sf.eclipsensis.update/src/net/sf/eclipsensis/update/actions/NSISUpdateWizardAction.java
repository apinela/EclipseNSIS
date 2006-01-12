package net.sf.eclipsensis.update.actions;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.wizard.NSISUpdateWizard;
import net.sf.eclipsensis.update.wizard.NSISUpdateWizardDialog;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class NSISUpdateWizardAction implements IWorkbenchWindowActionDelegate 
{
	private IWorkbenchWindow mWindow;

	public void run(IAction action) 
    {
        final Shell shell = mWindow.getShell();
        final NSISUpdateWizardDialog[] wizardDialog = new NSISUpdateWizardDialog[1];
        BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
            public void run()
            {
                try {
                    NSISUpdateWizard wizard = new NSISUpdateWizard();
                    wizard.setValidateNSISConfig(false);
                    wizard.setWindowTitle(EclipseNSISUpdatePlugin.getResourceString("wizard.window.title")); //$NON-NLS-1$
                    wizardDialog[0] = new NSISUpdateWizardDialog(shell, wizard);
                    wizardDialog[0].create();
                }
                catch (Exception e) {
                    wizardDialog[0] = null;
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                }                
            }
        });
        if(wizardDialog[0] != null) {
            wizardDialog[0].open();
        }
	}

	public void selectionChanged(IAction action, ISelection selection) 
    {
	}

	public void dispose() 
    {
	}

	public void init(IWorkbenchWindow window) 
    {
		this.mWindow = window;
	}
}