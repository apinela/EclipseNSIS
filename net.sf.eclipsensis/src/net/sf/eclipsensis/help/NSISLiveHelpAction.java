/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import net.sf.eclipsensis.dialogs.NSISPreferencePage;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;

public class NSISLiveHelpAction implements ILiveHelpAction
{
    /* (non-Javadoc)
     * @see org.eclipse.help.ILiveHelpAction#setInitializationString(java.lang.String)
     */
    public void setInitializationString(String data)
    {
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                NSISPreferencePage.show();
            }});
    }
}
