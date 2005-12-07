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

import net.sf.eclipsensis.util.NSISCompileTestUtility;

import org.eclipse.jface.action.IAction;

public class NSISTestAction extends NSISScriptAction
{
    protected void started()
    {
        if(mAction != null) {
            mAction.setEnabled(false);
        }
    }

    protected void stopped()
    {
        if(mAction != null) {
            mAction.setEnabled(isEnabled());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        return (super.isEnabled() && NSISCompileTestUtility.INSTANCE.canTest(mInput));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        NSISCompileTestUtility.INSTANCE.test(mInput);
    }
}
