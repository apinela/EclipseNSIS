/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.*;

public class SwitchEditorAction extends Action
{
    public static final String ID = "net.sf.eclipsensis.installoptions.switch_editor"; //$NON-NLS-1$
    
    private IInstallOptionsEditor mEditor;
    private String mSwitchToEditorId;
    
    public SwitchEditorAction(IInstallOptionsEditor editor, String switchToEditorId)
    {
        super();
        mEditor = editor;
        mSwitchToEditorId = switchToEditorId;
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
    }
    
    public String getId()
    {
        return ID;
    }
    
    public void run() {
        IWorkbenchPage page = mEditor.getSite().getPage();
        IEditorInput input = mEditor.getEditorInput();
        //TODO REMOVE BELOW
        IDocument document = ((InstallOptionsEditorInput)input).getDocumentProvider().getDocument(input);
        if(document != null) {
            char[] buf = new char[1024];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (char)(32 + (int)Math.rint(Math.random()*94));
            }
            document.set(new String(buf));
        }
        //REMOVE ABOVE
        mEditor.setSwitching(true);
        page.closeEditor(mEditor,false);
        try {
            page.openEditor(input,mSwitchToEditorId);
        }
        catch (PartInitException e) {
            e.printStackTrace();
        }
    }
}