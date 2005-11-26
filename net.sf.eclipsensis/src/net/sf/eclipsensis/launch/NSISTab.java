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
import net.sf.eclipsensis.dialogs.FileSelectionDialog;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

class NSISTab extends AbstractLaunchConfigurationTab implements INSISSettingsEditorListener
{
    private static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    private static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.filtername")}; //$NON-NLS-1$
    private LaunchSettingsEditor mSettingsEditor;
    
    public NSISTab()
    {
        mSettingsEditor = new LaunchSettingsEditor();
    }
    
    public Image getImage() 
    {
        return EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("nsis.icon")); //$NON-NLS-1$
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("launchconfig.nsis.tab.header")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        Control control = mSettingsEditor.createControl(composite);
        control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        
        setControl(composite);
        mSettingsEditor.addListener(this);
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
    {
        NSISLaunchSettings settings = (NSISLaunchSettings)mSettingsEditor.getSettings();
        ILaunchConfiguration config = settings.getLaunchConfig();
        try {
            settings.setLaunchConfig(null);
            settings.load();
            settings.setLaunchConfig(configuration);
            mSettingsEditor.performApply();
        }
        finally {
            settings.setLaunchConfig(config);
            settings.load();
        }
    }

    public void initializeFrom(ILaunchConfiguration configuration)
    {
        NSISLaunchSettings settings = (NSISLaunchSettings)mSettingsEditor.getSettings();
        settings.setLaunchConfig(configuration);
        settings.load();
        mSettingsEditor.reset();
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration)
    {
        NSISLaunchSettings settings = (NSISLaunchSettings)mSettingsEditor.getSettings();
        ILaunchConfiguration config = settings.getLaunchConfig();
        try {
            settings.setLaunchConfig(configuration);
            mSettingsEditor.performApply();
        }
        finally {
            settings.setLaunchConfig(config);
        }
    }

    public String getName()
    {
        return EclipseNSISPlugin.getResourceString("launchconfig.nsis.tab.name"); //$NON-NLS-1$
    }

    public boolean canSave()
    {
        return mSettingsEditor.isValid(); 
    }

    public boolean isValid(ILaunchConfiguration launchConfig)
    {
        if(super.isValid(launchConfig)) {
            return mSettingsEditor.isValid(); 
        }
        return false;
    }

    public void settingsChanged()
    {
        updateLaunchConfigurationDialog();
        if(!mSettingsEditor.isValid()) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.prompt")); //$NON-NLS-1$
        }
        else {
            setErrorMessage(null);
        }
    }
    
    private boolean validateScript(String script)
    {
        return checkWorkspaceFile(script) != null || checkExternalFile(script) != null;
    }
    
    private IFile checkWorkspaceFile(String script)
    {
        IFile file = null;
        if(!Common.isEmpty(script) && script.indexOf('\\') == -1) {
            IPath path = new Path(script);
            if(path.getDevice() == null) {
                if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(path.getFileExtension())) {
                    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                    if(resource != null && resource instanceof IFile) {
                        file = (IFile)resource;
                    }
                }
            }
        }
        return file;
    }

    private File checkExternalFile(String script)
    {
        if(!Common.isEmpty(script) && script.indexOf('/') == -1) {
            File file = new File(script);
            if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(Common.getFileExtension(file)) &&
               file.exists() && file.isAbsolute() && file.isFile()) {
                return file;
            }
        }
        return null;
    }

    private class LaunchSettingsEditor extends NSISSettingsEditor
    {
        Text mScript = null;
        Button mRunInstaller = null;
        boolean mHandlingScriptChange = false;
        private IFilter mFilter = new IFilter() {
            public boolean select(Object toTest)
            {
                if(toTest instanceof IFile) {
                    return ((IFile)toTest).getFileExtension().equalsIgnoreCase(INSISConstants.NSI_EXTENSION);
                }
                return false;
            }
        };

        protected ControlAdapter createTableControlListener()
        {
            return new ControlAdapter() {
                boolean init=false;
                boolean ok=false;
                public void controlResized(ControlEvent e)
                {
                    final Table table = (Table)e.widget;
                    if(table.getShell().isVisible()) {
                        //Really dumb hack because LaunchConfigurationDialog
                        //keeps resizing the dialog with normal use of TableResizer
                        if(init && !ok) {
                            table.removeControlListener(this);
                            ControlAdapter controlAdapter = LaunchSettingsEditor.super.createTableControlListener();
                            table.addControlListener(controlAdapter);
                            final Point p = table.getSize();
                            p.x -= 19;
                            table.setSize(p);
                            p.x += 19;
                            table.getShell().getDisplay().asyncExec(new Runnable() {
                                public void run() 
                                {
                                    table.setSize(p);
                                }
                            });
                            ok = true;
                        }
                        else {
                            init = true;
                        }
                    }
                }  
            };
        }

        public boolean isValid()
        {
            return validateScript(mScript.getText());
        }
        
        public void reset()
        {
            NSISLaunchSettings settings = (NSISLaunchSettings)getSettings();
            mScript.setText(settings.getScript());
            mRunInstaller.setSelection(settings.getRunInstaller());
            super.reset();
        }

        private boolean handleScriptChange()
        {
            if(!mHandlingScriptChange) {
                try {
                    mHandlingScriptChange = true;
                    boolean state = false;
                    String script = mScript.getText();
                    if(!Common.isEmpty(script)) {
                        if(validateScript(script)) {
                            state = true;
                            setErrorMessage(null);
                        }
                        else {
                            setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.prompt")); //$NON-NLS-1$
                        }
                    }
                    else if(getErrorMessage() == null) {
                        setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.prompt")); //$NON-NLS-1$
                    }
                    enableControls(state);
                    return state;
                }
                finally {
                    fireChanged();
                    mHandlingScriptChange = false;
                }
            }
            return true;
        }
        
        protected void enableControls(boolean state)
        {
            mRunInstaller.setEnabled(state);
            super.enableControls(state);
        }

        public void setDefaults()
        {
            super.setDefaults();
            mScript.setText(""); //$NON-NLS-1$
        }

        protected boolean performApply(NSISSettings settings)
        {
            if(super.performApply(settings)) {
                NSISLaunchSettings settings2 = (NSISLaunchSettings)settings;
                settings2.setScript(mScript.getText());
                settings2.setRunInstaller(mRunInstaller.getSelection());
                return true;
            }
            return false;
        }

        protected boolean canEnableControls()
        {
            return !Common.isEmpty(mScript.getText());
        }

        protected Composite createMasterControl(Composite parent)
        {
            Composite composite = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginWidth = 0;
            composite.setLayout(layout);

            Label label = new Label(composite, SWT.LEFT);
            label.setText(EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.label")); //$NON-NLS-1$
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            label.setLayoutData(data);

            mScript = new Text(composite, SWT.BORDER);
            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            mScript.setLayoutData(data);

            mScript.setText(((NSISLaunchSettings)getSettings()).getScript());
            mScript.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    handleScriptChange();
                }
            });

            Composite buttons = new Composite(composite,SWT.NONE);
            GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
            gridData.horizontalSpan = 2;
            buttons.setLayoutData(gridData);
            layout = new GridLayout(2,true);
            layout.marginHeight=0;
            layout.marginWidth=0;
            buttons.setLayout(layout);
            final Button workspaceButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.workspace.label"), ""); //$NON-NLS-1$  //$NON-NLS-2$
            workspaceButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseSelected(workspaceButton.getShell(), true);
                }
            });
            workspaceButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
            final Button filesystemButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.filesystem.label"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            filesystemButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseSelected(workspaceButton.getShell(), false);
                }
            });
            filesystemButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
            
            mRunInstaller = createCheckBox(composite, EclipseNSISPlugin.getResourceString("launchconfig.run.installer.label"), "", ((NSISLaunchSettings)getSettings()).getRunInstaller()); //$NON-NLS-1$ //$NON-NLS-2$
            gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gridData.horizontalSpan = 2;
            mRunInstaller.setLayoutData(gridData);
            return composite;
        }

        private void handleBrowseSelected(Shell shell, boolean isWorkspace)
        {
            String script = mScript.getText(); 
            script = browseForScript(shell, script, isWorkspace);
            if (!Common.isEmpty(script)) {
                if(validateScript(script)) {
                    mScript.setText(script);
                    enableControls(true);
                }
                else {
                    Common.openError(shell,
                            EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.prompt"),  //$NON-NLS-1$
                            EclipseNSISPlugin.getShellImage());
                }
            }
        }

        private String browseForScript(Shell shell, String script, boolean isWorkspace)
        {
            if(isWorkspace) {
                FileSelectionDialog dialog = new FileSelectionDialog(shell,checkWorkspaceFile(script),
                                                                     mFilter);
                if(dialog.open() == Window.OK) {
                    return dialog.getFile().getFullPath().toString();
                }
            }
            else {
                FileDialog dialog = new FileDialog(shell,SWT.OPEN);
                dialog.setFilterExtensions(FILTER_EXTENSIONS);
                dialog.setFilterNames(FILTER_NAMES);

                File file = checkExternalFile(script);
                if(file != null) {
                    dialog.setFileName(file.getName());
                    dialog.setFilterPath(file.getParentFile().getAbsolutePath());
                }
                
                return dialog.open();
            }
            return null;
        }

        protected NSISSettings loadSettings()
        {
            return new NSISLaunchSettings(NSISPreferences.INSTANCE);
        }
    }
}