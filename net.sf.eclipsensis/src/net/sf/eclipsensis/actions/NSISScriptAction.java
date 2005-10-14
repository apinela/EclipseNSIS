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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.IMakeNSISRunListener;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

public abstract class NSISScriptAction extends NSISAction implements IMakeNSISRunListener
{
    protected IPath mInput = null;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action)
    {
        super.init(action);
        MakeNSISRunner.addListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    public void dispose()
    {
        super.dispose();
        MakeNSISRunner.removeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        super.setActiveEditor(action, targetEditor);
        mInput = null;
        if(mEditor != null) {
            IEditorInput editorInput = mEditor.getEditorInput();
            if(editorInput !=null) {
                if(editorInput instanceof IFileEditorInput) {
                    mInput = ((IFileEditorInput)editorInput).getFile().getFullPath();
                }
                else if(editorInput instanceof IPathEditorInput) {
                    mInput = ((IPathEditorInput)editorInput).getPath();
                }
            }
        }
        updateActionState();
    }

    public void updateActionState()
    {
        if(mAction != null) {
            try {
                mAction.setEnabled(isEnabled());
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {  
        if(selection instanceof IStructuredSelection) {
            //This is for the popup context menu handling
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if(!selection.isEmpty()) {
                mInput = ((IFile)structuredSelection.getFirstElement()).getFullPath();
            }
            else {
                mInput = null;
            }
        }
        updateActionState();
    }
    
    public boolean isEnabled()
    {
        return (mPlugin != null && mPlugin.isConfigured() && mInput != null && 
                mInput.getFileExtension().equalsIgnoreCase(NSI_EXTENSION));
    }

    public void started()
    {
    }

    public void stopped()
    {
    }
}