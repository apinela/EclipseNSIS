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

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

public class NSISHelpAction extends ActionDelegate implements IEditorActionDelegate, INSISConstants
{
    public static final String PLUGIN_HELP_URL = new StringBuffer("/").append(PLUGIN_NAME).append( //$NON-NLS-1$
                            "/").append(PLUGIN_HELP_LOCATION_PREFIX).append("Overview.html").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * 
     */
    public NSISHelpAction()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(PLUGIN_HELP_URL);
    }
}
