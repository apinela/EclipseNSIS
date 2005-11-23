/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.FileSelectionDialog;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class NSISWorkspaceLaunchConfigTabGroup extends NSISLaunchConfigTabGroup
{
    protected NSISTab createNSISTab()
    {
        return new NSISWorkspaceTab();
    }

    public class NSISWorkspaceTab extends NSISTab
    {
        protected LaunchSettingsEditor createLaunchSettingsEditor()
        {
            return new WorkspaceLaunchSettingsEditor();
        }
    
        public class WorkspaceLaunchSettingsEditor extends LaunchSettingsEditor
        {
            private IFilter mFilter = new IFilter() {
                public boolean select(Object toTest)
                {
                    if(toTest instanceof IFile) {
                        return ((IFile)toTest).getFileExtension().equalsIgnoreCase(INSISConstants.NSI_EXTENSION);
                    }
                    return false;
                }
            };
    
            protected String browseForScript(Shell shell, String script)
            {
                FileSelectionDialog dialog = new FileSelectionDialog(shell,checkFile(script),
                                                                     mFilter);
                if(dialog.open() == Window.OK) {
                    return dialog.getFile().getFullPath().toString();
                }
                return null;
            }
            
            private IFile checkFile(String script)
            {
                IFile file = null;
                if(!Common.isEmpty(script)) {
                    IPath path = new Path(script);
                    if(path.getFileExtension().equalsIgnoreCase(INSISConstants.NSI_EXTENSION)) {
                        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                        if(resource != null && resource instanceof IFile) {
                            file = (IFile)resource;
                        }
                    }
                }
                return file;
            }
    
            protected boolean validateScript(String script)
            {
                return checkFile(script) != null;
            }
    
        }
    }
}
