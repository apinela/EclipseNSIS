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
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
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
    private IStringVariableManager mStringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
    
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
        try {
            return checkExternalFile(mStringVariableManager.performStringSubstitution(script)) != null;
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return false;
        }
    }
    
    private IFile checkWorkspaceFile(String script)
    {
        if(!Common.isEmpty(script)) {
            IPath path = new Path(script);
            if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(path.getFileExtension())) {
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
                if(file != null && file.exists()) {
                    return file;
                }
            }
        }
        return null;
    }

    private File checkExternalFile(String script)
    {
        if(!Common.isEmpty(script)) {
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
        private Text mScript = null;
        private Button mRunInstaller = null;
        private boolean mHandlingScriptChange = false;
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
            gridData.horizontalSpan = 3;
            buttons.setLayoutData(gridData);
            layout = new GridLayout(3,true);
            layout.marginHeight=0;
            layout.marginWidth=0;
            buttons.setLayout(layout);
            final Button workspaceButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.workspace.label"), ""); //$NON-NLS-1$  //$NON-NLS-2$
            workspaceButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseWorkspace(workspaceButton.getShell());
                }
            });
            workspaceButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            final Button filesystemButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.filesystem.label"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            filesystemButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseFilesystem(filesystemButton.getShell());
                }
            });
            filesystemButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            final Button variablesButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.variables.label"), ""); //$NON-NLS-1$  //$NON-NLS-2$
            variablesButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseVariables(variablesButton.getShell());
                }
            });
            variablesButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            
            mRunInstaller = createCheckBox(composite, EclipseNSISPlugin.getResourceString("launchconfig.run.installer.label"), "", ((NSISLaunchSettings)getSettings()).getRunInstaller()); //$NON-NLS-1$ //$NON-NLS-2$
            gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gridData.horizontalSpan = 2;
            mRunInstaller.setLayoutData(gridData);
            return composite;
        }

        private void handleBrowseWorkspace(Shell shell)
        {
            IFile ifile;
            try {
                ifile = checkWorkspaceFile(mStringVariableManager.performStringSubstitution(mScript.getText()));
            }
            catch (CoreException e) {
                ifile = null;
                EclipseNSISPlugin.getDefault().log(e);
            }
            FileSelectionDialog dialog = new FileSelectionDialog(shell,ifile,mFilter);
            if(dialog.open() == Window.OK) {
                mScript.setText(mStringVariableManager.generateVariableExpression("workspace_loc",dialog.getFile().getFullPath().toString()));
            }
        }

        private void handleBrowseFilesystem(Shell shell)
        {
            FileDialog dialog = new FileDialog(shell,SWT.OPEN);
            dialog.setFilterExtensions(FILTER_EXTENSIONS);
            dialog.setFilterNames(FILTER_NAMES);

            File file;
            try {
                file = checkExternalFile(mStringVariableManager.performStringSubstitution(mScript.getText()));
            }
            catch (Exception e) {
                file = null;
                EclipseNSISPlugin.getDefault().log(e);
            }
            if(file != null) {
                dialog.setFileName(file.getName());
                dialog.setFilterPath(file.getParentFile().getAbsolutePath());
            }
            
            String filename = dialog.open();
            if(filename != null) {
                mScript.setText(filename);
            }
        }

        private void handleBrowseVariables(Shell shell)
        {
            StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(shell);
            dialog.open();
            String variable = dialog.getVariableExpression();
            if (variable != null) {
                mScript.insert(variable);
            }
        }
        
        protected NSISSettings loadSettings()
        {
            return new NSISLaunchSettings(NSISPreferences.INSTANCE);
        }
    }
}