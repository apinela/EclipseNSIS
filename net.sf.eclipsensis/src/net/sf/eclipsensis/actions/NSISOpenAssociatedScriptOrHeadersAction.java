/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.NSISHeaderAssociationManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISOpenAssociatedScriptOrHeadersAction extends NSISScriptAction
{
    private IFile mFile = null;

    protected void started(IPath script)
    {
    }

    protected void stopped(IPath script, MakeNSISResults results)
    {
    }

    public void run(IAction action)
    {
        if(mPart != null && mFile != null) {
            NSISEditorUtilities.openAssociatedFiles(mPart.getSite().getPage(),mFile);
        }
    }

    protected void setInput(IPath input)
    {
        super.setInput(input);
        mFile = null;
        if(input != null && input.getDevice() == null) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(input);
            if(file != null) {
                String ext = file.getFileExtension();
                if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true)) {
                    mFile = file;
                }
                else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true)) {
                    mFile = file;
                }
            }
        }
    }

    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            if(mFile != null) {
                String ext = mFile.getFileExtension();
                if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true)) {
                    return !Common.isEmptyCollection(NSISHeaderAssociationManager.getInstance().getAssociatedHeaders(mFile));
                }
                else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true)) {
                    return (NSISHeaderAssociationManager.getInstance().getAssociatedScript(mFile) != null);
                }
            }
        }
        return false;
    }
}
