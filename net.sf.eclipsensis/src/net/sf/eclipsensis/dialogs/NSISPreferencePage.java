/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;


import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.NSISValidator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class NSISPreferencePage	extends NSISSettingsPage
{
    private Text mNSISHome = null;
    
    public static void show()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        PreferenceManager manager = workbench.getPreferenceManager();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        PreferenceDialog pd = new PreferenceDialog(shell, manager);
        pd.setSelectedNode(NSISPreferencePage.class.getName());
        pd.open();
    }
    
    protected String getPageDescription()
    {
        return EclipseNSISPlugin.getResourceString("preferences.header.text"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#loadSettings()
     */
    protected NSISSettings loadSettings()
    {
        return NSISPreferences.getPreferences();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#canEnableControls()
     */
    protected boolean canEnableControls()
    {
        return !Common.isEmpty(mNSISHome.getText());
    }
    
    /**
     * @param composite
     * @return
     */
    protected Composite createEnablerControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NULL);
        GridLayout layout = new GridLayout(3,false);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("nsis.home.text")); //$NON-NLS-1$
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        
        mNSISHome = new Text(composite, SWT.SINGLE | SWT.BORDER);
        mNSISHome.setToolTipText(EclipseNSISPlugin.getResourceString("nsis.home.tooltip")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        mNSISHome.setLayoutData(data);
        mNSISHome.setText(((NSISPreferences)getSettings()).getNSISHome());
        mNSISHome.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) 
            {
                boolean state = false;
                String nsisHome = mNSISHome.getText();
                if(!Common.isEmpty(nsisHome)) { 
                    if(!NSISValidator.validateNSISHome(nsisHome)) {
                        MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),EclipseNSISPlugin.getResourceString("invalid.nsis.home.message")); //$NON-NLS-1$ //$NON-NLS-2$
                        mNSISHome.setText(""); //$NON-NLS-1$
                        mNSISHome.forceFocus();
                    }
                    else {
                        state = true;
                    }
                }
                enableControls(state);
            }
        });
        Button button = new Button(composite, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Shell shell = getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell);
        
                String nsisHome = dialog.open();
                if (!Common.isEmpty(nsisHome)) { 
                    if(NSISValidator.validateNSISHome(nsisHome)) {
                        mNSISHome.setText(nsisHome);
                        enableControls(true);
                    }
                    else {
                        MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),EclipseNSISPlugin.getResourceString("invalid.nsis.home.message")); //$NON-NLS-1$ //$NON-NLS-2$
                        mNSISHome.setText(""); //$NON-NLS-1$
                        mNSISHome.setFocus();
                        enableControls(false);
                    }
                }
            }
        });
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        button.setLayoutData(data);
        
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        super.performDefaults();
        mHdrInfo.setSelection(getSettings().getDefaultHdrInfo());
        mLicense.setSelection(getSettings().getDefaultLicense());
        mNoConfig.setSelection(getSettings().getDefaultNoConfig());
        mNoCD.setSelection(getSettings().getDefaultNoCD());
        mVerbosity.select(getSettings().getDefaultVerbosity());
        mCompressor.select(getSettings().getDefaultCompressor());
        mInstructions.setInput(getSettings().getDefaultInstructions());
        mSymbols.setInput(getSettings().getDefaultSymbols());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        ((NSISPreferences)getSettings()).setNSISHome(mNSISHome.getText());
        return super.performOk();
    }
}