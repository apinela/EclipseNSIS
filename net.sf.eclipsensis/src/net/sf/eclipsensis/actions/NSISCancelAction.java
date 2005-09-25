/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.jface.action.IAction;

public class NSISCancelAction extends NSISScriptAction 
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	public void run(IAction action) {
        try {
            MakeNSISRunner.cancel();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
	}

    public void started()
    {
        if(mAction != null && !mAction.isEnabled()) {
            mAction.setEnabled(true);
        }
    }

    public void stopped()
    {
        if(mAction != null && mAction.isEnabled()) {
            mAction.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return (MakeNSISRunner.isCompiling());
        }
        return false;
    }
}