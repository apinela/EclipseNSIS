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
import net.sf.eclipsensis.console.NSISConsole;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.makensis.IMakeNSISRunListener;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

public abstract class NSISAction extends ActionDelegate implements IEditorActionDelegate, INSISConstants, IMakeNSISRunListener
{
    protected EclipseNSISPlugin mPlugin = null;
    protected NSISEditor mEditor = null;
    protected NSISConsole mConsole = null;
    protected IAction mAction = null;
    protected IFile mFile = null;
    
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
        MakeNSISRunner.addListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    public void dispose()
    {
        super.dispose();
        mAction = null;
        MakeNSISRunner.removeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        mEditor = (NSISEditor)targetEditor;
        if(mEditor != null) {
            mEditor.setAction(mAction.getId(),mAction);
            mEditor.addAction(this);
            IEditorInput editorInput = mEditor.getEditorInput();
            if(editorInput !=null && editorInput instanceof IFileEditorInput) {
                mFile = ((IFileEditorInput)editorInput).getFile();
            }
            mConsole = NSISConsole.getConsole();
        }
        updateActionState();
    }

    public void updateActionState()
    {
        if(mAction != null) {
            try {
                boolean enabled = isEnabled();
                mAction.setEnabled(enabled);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {  
        if(selection !=null && selection instanceof IStructuredSelection) {
            //This is for the popup context menu handling
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if(!selection.isEmpty()) {
                mFile = (IFile)(structuredSelection).getFirstElement();
            }
            else {
                mFile = null;
            }
            updateActionState();
        }
    }
    
    public boolean isEnabled()
    {
        return (mPlugin != null && mPlugin.isConfigured());
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#started()
     */
    public void started()
    {
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#stopped()
     */
    public void stopped()
    {
    }
}