/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.console.NSISConsole;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.jface.action.IAction;

public class NSISCancelAction extends NSISAction 
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	public void run(IAction action) {
        try {
            MakeNSISRunner.cancel();
            NSISConsole console = NSISConsole.getConsole();
            console.add(NSISConsoleLine.error(EclipseNSISPlugin.getResourceString("cancel.message"))); //$NON-NLS-1$
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
	}

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#started()
     */
    public void started()
    {
        if(mAction != null) {
            mAction.setEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#stopped()
     */
    public void stopped()
    {
        if(mAction != null) {
            mAction.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return (MakeNSISRunner.isRunning());
        }
        return false;
    }
}