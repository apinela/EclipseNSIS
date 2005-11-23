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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

abstract class NSISTab extends AbstractLaunchConfigurationTab implements INSISSettingsEditorListener
{
    private LaunchSettingsEditor mSettingsEditor;
    
    public NSISTab()
    {
        mSettingsEditor = createLaunchSettingsEditor();
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

    protected abstract LaunchSettingsEditor createLaunchSettingsEditor();

    protected abstract class LaunchSettingsEditor extends NSISSettingsEditor
    {
        Text mScript = null;
        Button mRunInstaller = null;
        boolean mHandlingScriptChange = false;

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

        boolean handleScriptChange()
        {
            if(!mHandlingScriptChange) {
                try {
                    mHandlingScriptChange = true;
                    boolean state = false;
                    String script = mScript.getText();
                    if(!Common.isEmpty(script)) {
                        if(validateScript(script)) {
                            state = true;
                        }
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
            GridLayout layout = new GridLayout(3,false);
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

            final Button button = createButton(composite, EclipseNSISPlugin.getResourceString("browse.text"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    Shell shell = button.getShell();
                    String script = mScript.getText(); 
                    script = browseForScript(shell, script);
                    if (!Common.isEmpty(script)) {
                        if(validateScript(script)) {
                            mScript.setText(script);
                            enableControls(true);
                        }
                        else {
                            Common.openError(button.getShell(),
                                    EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.prompt"),  //$NON-NLS-1$
                                    EclipseNSISPlugin.getShellImage());
                        }
                    }
                }
            });
            
            mRunInstaller = createCheckBox(composite, EclipseNSISPlugin.getResourceString("launchconfig.run.installer.label"), "", ((NSISLaunchSettings)getSettings()).getRunInstaller()); //$NON-NLS-1$ //$NON-NLS-2$
            GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gridData.horizontalSpan = 3;
            mRunInstaller.setLayoutData(gridData);
            return composite;
        }

        protected NSISSettings loadSettings()
        {
            return new NSISLaunchSettings();
        }
        
        protected abstract String browseForScript(Shell shell, String script);
        protected abstract boolean validateScript(String script);
    }
}