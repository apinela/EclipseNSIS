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

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class NSISExternalLaunchConfigTabGroup extends NSISLaunchConfigTabGroup
{
    private static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    private static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.filtername")}; //$NON-NLS-1$

    protected NSISTab createNSISTab()
    {
        return new NSISExternalTab();
    }

    public class NSISExternalTab extends NSISTab
    {
        protected LaunchSettingsEditor createLaunchSettingsEditor()
        {
            return new ExternalLaunchSettingsEditor();
        }
    
        public class ExternalLaunchSettingsEditor extends LaunchSettingsEditor
        {
            protected String browseForScript(Shell shell, String script)
            {
                FileDialog dialog = new FileDialog(shell,SWT.SAVE);
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                dialog.setFilterNames(FILTER_NAMES);

                File file = checkFile(script);
                if(file != null) {
                    dialog.setFileName(file.getName());
                    dialog.setFilterPath(file.getParentFile().getAbsolutePath());
                }
                
                return dialog.open();
            }

            private File checkFile(String script)
            {
                if(!Common.isEmpty(script)) {
                    File file = new File(script);
                    if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(Common.getFileExtension(file)) && file.exists() && file.isFile()) {
                        return file;
                    }
                }
                return null;
            }

            protected boolean validateScript(String script)
            {
                return checkFile(script) != null;
            }
    
        }
    }
}
