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

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISProperties;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class NSISLaunchShortcut implements ILaunchShortcut
{
    private ILaunchManager mLaunchManager;
    private ILaunchConfigurationType mConfigType;
    private IStringVariableManager mStringVariableManager;

    public NSISLaunchShortcut()
    {
        mLaunchManager = DebugPlugin.getDefault().getLaunchManager();
        mConfigType = mLaunchManager.getLaunchConfigurationType("net.sf.eclipsensis.launch.nsisLaunchConfigType");
        mStringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
    }

    public void launch(final ISelection selection, String mode)
    {
        if(mode.equals(ILaunchManager.RUN_MODE) && selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object obj = ((IStructuredSelection)selection).getFirstElement();
            if(obj instanceof IFile) {
                launch(((IFile)obj).getFullPath(), mode);
            }
        }
    }

    public void launch(final IEditorPart editor, String mode)
    {
        if(mode.equals(ILaunchManager.RUN_MODE) && editor != null) {
            IEditorInput input = editor.getEditorInput();
            if(input instanceof IFileEditorInput) {
                launch(((IFileEditorInput)input).getFile().getFullPath(), mode);
            }
            else if(input instanceof IPathEditorInput) {
                launch(((IPathEditorInput)input).getPath(), mode);
            }
        }
    }
    
    private void launch(IPath path, String mode)
    {
        if (INSISConstants.NSI_EXTENSION.equalsIgnoreCase(path.getFileExtension())) {
            ILaunchConfiguration config = findConfiguration(path);
            if(config != null) {
                DebugUITools.launch(config, mode);
            }
        }
    }

    private ILaunchConfiguration findConfiguration(IPath path)
    {
        String fullname = getLocation(path);
        List candidateConfigs = new ArrayList();
        try {
            if (mConfigType != null) {
                ILaunchConfiguration[] configs = mLaunchManager.getLaunchConfigurations(mConfigType);
                if (!Common.isEmptyArray(configs)) {
                    for (int i = 0; i < configs.length; i++) {
                        ILaunchConfiguration config = configs[i];
                        try {
                            String script = config.getAttribute(NSISLaunchSettings.SCRIPT, "");
                            if (!Common.isEmpty(script)) {
                                String fullname2 = getLocation(new Path(mStringVariableManager.performStringSubstitution(script)));
                                if (Common.stringsAreEqual(fullname, fullname2, true)) {
                                    candidateConfigs.add(config);
                                }
                            }
                        }
                        catch (CoreException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        
        if(candidateConfigs.size() > 0) {
            if(candidateConfigs.size() > 1) {
                return chooseConfiguration(candidateConfigs);
            }
            else {
                return (ILaunchConfiguration)candidateConfigs.get(0);
            }
        }
        else {
            return createConfiguration(path);
        }
    }

    private ILaunchConfiguration createConfiguration(IPath path) 
    {
        NSISLaunchSettings settings;
        String script;
        if(path.getDevice() == null) {
            script = mStringVariableManager.generateVariableExpression("workspace_loc",path.toString());
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            settings = new NSISLaunchSettings(NSISProperties.getProperties(file));
        }
        else {
            script = path.toOSString();
            settings = new NSISLaunchSettings(NSISPreferences.INSTANCE);
        }
        settings.setScript(script);
        settings.setRunInstaller(false);
        
        ILaunchConfiguration config = null;
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            wc = mConfigType.newInstance(null, mLaunchManager.generateUniqueLaunchConfigurationNameFrom(path.lastSegment()));
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);
            return null;        
        }
        settings.setLaunchConfig(wc);
        settings.store();
        try {
            config = wc.doSave();       
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);         
            return null;        
        }
        return config;
    }
    
    protected void reportCreatingConfiguration(final CoreException exception) 
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                int mask = IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR;
                ErrorDialog dialog = new ErrorDialog(getShell(), "Error Launching", "Exception", exception.getStatus(), mask);
                dialog.create();
                dialog.getShell().setImage(EclipseNSISPlugin.getShellImage());
                dialog.open();
            }
        });
    }
    
    private ILaunchConfiguration chooseConfiguration(List configList) 
    {
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setElements(configList.toArray());
        dialog.setTitle("Launch Configuration Selection");
        dialog.setMessage("Choose a launch configuration to run");
        dialog.setMultipleSelection(false);
        int result = dialog.open();
        labelProvider.dispose();
        if (result == Window.OK) {
            return (ILaunchConfiguration) dialog.getFirstResult();
        }
        return null;        
    }

    private Shell getShell()
    {
        Shell shell;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            shell = window.getShell();
        }
        else {
            shell = null;
        }
        return shell;
    }
    
    private String getLocation(IPath path)
    {
        String fullname = null;
        if(path.getDevice() == null) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            if(file != null) {
                fullname = file.getLocation().toOSString();
            }
        }
        else {
            fullname = path.toOSString();
        }
        return fullname;
    }
}