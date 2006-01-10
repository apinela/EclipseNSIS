/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.actions;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.actions.NSISAction;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.update.jobs.NSISCheckUpdateJob;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;

public class NSISUpdateAction extends NSISAction implements INSISHomeListener
{
    public NSISUpdateAction()
    {
        super();
        NSISPreferences.INSTANCE.addListener(this);
    }

    public void dispose()
    {
        NSISPreferences.INSTANCE.removeListener(this);
        super.dispose();
    }

    public void init(IAction action)
    {
        super.init(action);
        updateState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        new NSISCheckUpdateJob().schedule();
    }

    public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
    {
        updateState();
    }

    private void updateState()
    {
        if(mAction != null) {
            mAction.setEnabled(EclipseNSISPlugin.getDefault().isConfigured());
        }
    }
}
