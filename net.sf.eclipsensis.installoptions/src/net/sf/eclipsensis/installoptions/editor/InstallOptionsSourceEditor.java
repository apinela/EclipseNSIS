/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import net.sf.eclipsensis.installoptions.actions.SwitchEditorAction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class InstallOptionsSourceEditor extends TextEditor implements IInstallOptionsEditor
{
    private boolean mSwitching = false;
    
    public boolean isSwitching()
    {
        return mSwitching;
    }
    public void setSwitching(boolean switching)
    {
        mSwitching = switching;
    }

    protected void createActions()
    {
        super.createActions();
        IAction action = new SwitchEditorAction(this, INSTALLOPTIONS_DESIGN_EDITOR_ID);
        setAction(action.getId(),action);
    }

    public void dispose()
    {
        if(isSwitching()) {
            InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
            if(input != null) {
                IDocumentProvider documentProvider = input.getDocumentProvider();
                if(documentProvider != null) {
                    documentProvider.disconnect(input);
                    setDocumentProvider(new FileDocumentProvider());
                }
            }
        }
        super.dispose();
    }
    
    protected void doSetInput(IEditorInput input) throws CoreException
    {
        if(input != null) {
            if(!(input instanceof InstallOptionsEditorInput)) {
                input = new InstallOptionsEditorInput((IFileEditorInput)input);
            }
            setDocumentProvider(((InstallOptionsEditorInput)input).getDocumentProvider());
            //TODO REMOVE BELOW
            IDocument document = getDocumentProvider().getDocument(input);
            System.out.println(document.get());
            //REMOVE ABOVE
        }
        super.doSetInput(input);
    }
}
