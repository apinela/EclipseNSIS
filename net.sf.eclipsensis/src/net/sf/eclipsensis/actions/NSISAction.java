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
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

public abstract class NSISAction extends ActionDelegate implements IEditorActionDelegate, INSISConstants
{
    protected EclipseNSISPlugin mPlugin = null;
    protected NSISEditor mEditor = null;
    protected IAction mAction = null;
    
	/**
	 * The constructor.
	 */
	public NSISAction() 
    {
        mPlugin = EclipseNSISPlugin.getDefault();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action)
    {
        super.init(action);
        mAction = action;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    public void dispose()
    {
        super.dispose();
        mAction = null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        if(targetEditor instanceof NSISEditor) {
            mEditor = (NSISEditor)targetEditor;
            mEditor.setAction(mAction.getId(),mAction);
            mEditor.addAction(this);
        }
        else {
            mEditor = null;
        }
    }
}