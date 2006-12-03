/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

public abstract class NSISScriptAction extends NSISAction implements IMakeNSISRunListener
{
    protected IPath mInput = null;
    private boolean mValidExtension = false;
    private Pattern mExtensionPattern = null;

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

    public void setActiveEditor(IEditorPart targetEditor)
    {
        super.setActiveEditor(targetEditor);
        updateInput();
    }

    /**
     *
     */
    public void updateInput()
    {
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
        validateExtension();
        updateActionState();
    }

    private void validateExtension()
    {
        mValidExtension = (mInput != null && mInput.getFileExtension() != null && getExtensionPattern().matcher(mInput.getFileExtension()).matches());
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
        validateExtension();
        updateActionState();
    }

    public boolean isEnabled()
    {
        return (mPlugin != null && mPlugin.isConfigured() && mValidExtension);
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STARTED:
                started(event.getScript());
                break;
            case MakeNSISRunEvent.STOPPED:
                stopped(event.getScript(), (MakeNSISResults)event.getData());
                break;
        }
    }

    public Pattern getExtensionPattern()
    {
        if(mExtensionPattern == null) {
            mExtensionPattern = createExtensionPattern();
        }
        return mExtensionPattern;
    }

    protected Pattern createExtensionPattern()
    {
        return Pattern.compile(NSI_EXTENSION,Pattern.CASE_INSENSITIVE);
    }

    protected abstract void started(IPath script);
    protected abstract void stopped(IPath script, MakeNSISResults results);
}