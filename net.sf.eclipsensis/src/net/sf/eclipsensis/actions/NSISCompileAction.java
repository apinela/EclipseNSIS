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

import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.NSISCompileTestUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISCompileAction extends NSISScriptAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	final public void run(IAction action) {
        if(mPlugin != null) {
            action.setEnabled(false);
            NSISCompileTestUtility.INSTANCE.compile(mInput, shouldTest());
        }
	}

    protected void started(IPath script)
    {
        if(mAction != null && mAction.isEnabled()) {
            mAction.setEnabled(false);
        }
    }

    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null && !mAction.isEnabled()) {
            mAction.setEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return (!MakeNSISRunner.isCompiling());
        }
        return false;
    }

    protected boolean shouldTest()
    {
        return false;
    }
}