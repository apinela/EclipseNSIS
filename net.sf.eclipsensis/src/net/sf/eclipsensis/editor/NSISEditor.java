/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.HashSet;
import java.util.Iterator;

import net.sf.eclipsensis.actions.NSISAction;

import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;

public class NSISEditor extends TextEditor
{
    private HashSet mActions = new HashSet();
    
    public static NSISEditor getActiveEditor()
    {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if(activeWorkbenchWindow != null) {
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            if(activePage != null) {
                IEditorPart editor = activePage.getActiveEditor();
                if(editor != null && editor instanceof NSISEditor) {
                    return (NSISEditor)editor;
                }
            }
        }
        return null;
    }

    public static void updateEditorActionsState()
    {
        NSISEditor editor = getActiveEditor();
        if(editor != null) {
            editor.updateActionsState();
        }
    }
    
    public NSISEditor() {
		super();
	}
	public void dispose() {
		super.dispose();
	}

    public void addAction(NSISAction action)
    {
        mActions.add(action);
    }
    
    public void updateActionsState()
    {
        for(Iterator iter=mActions.iterator(); iter.hasNext(); ) {
            ((NSISAction)iter.next()).updateActionState();
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
     */
    protected void editorSaved()
    {
        super.editorSaved();
        updateActionsState();
    }
}
