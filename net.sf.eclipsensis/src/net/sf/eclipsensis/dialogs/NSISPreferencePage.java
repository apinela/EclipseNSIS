/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;


import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Collection;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class NSISPreferencePage	extends NSISSettingsPage
{
    private static File cNSISHomesListFile = new File(EclipseNSISPlugin.getPluginStateLocation(),
                                                NSISPreferencePage.class.getName()+".NSISHomesList.ser"); //$NON-NLS-1$
    private ComboViewer mNSISHome = null;
    private Button mUseEclipseHelp = null;
    private Button mAutoShowConsole = null;
    
    public static void show()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        PreferenceManager manager = workbench.getPreferenceManager();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        PreferenceDialog pd = new PreferenceDialog(shell, manager);
        pd.setSelectedNode(NSISPreferencePage.class.getName());
        pd.open();
    }
    
    /**
     * @return
     */
    protected String getContextId()
    {
        return PLUGIN_CONTEXT_PREFIX + "nsis_prefs_context"; //$NON-NLS-1$
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
        return !Common.isEmpty(mNSISHome.getCombo().getText());
    }
    
    /**
     * @param composite
     * @return
     */
    protected Composite createMasterControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(3,false);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("nsis.home.text")); //$NON-NLS-1$
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        label.setLayoutData(data);
        
        Combo c = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
        c.setToolTipText(EclipseNSISPlugin.getResourceString("nsis.home.tooltip")); //$NON-NLS-1$
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
        c.setLayoutData(data);
        
        final Collection nsisHomes;
        if(cNSISHomesListFile.exists()) {
            Collection temp;
            try {
                temp = (Collection)Common.readObject(cNSISHomesListFile);
            }
            catch (Exception e1) {
                temp = new CaseInsensitiveSet();
            }
            nsisHomes = temp;
        }
        else {
            nsisHomes = new CaseInsensitiveSet();
        }
        String home = ((NSISPreferences)getSettings()).getNSISHome();
        if(!nsisHomes.contains(home)) {
            nsisHomes.add(home);
        }

        mNSISHome = new ComboViewer(c);
        mNSISHome.setContentProvider(new CollectionContentProvider());
        mNSISHome.setLabelProvider(new CollectionLabelProvider());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        mNSISHome.setSorter(new ViewerSorter(collator));
        mNSISHome.setInput(nsisHomes);
        
        c.setText(home);
        c.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) 
            {
                boolean state = false;
                String nsisHome = mNSISHome.getCombo().getText();
                if(!Common.isEmpty(nsisHome)) { 
                    if(nsisHome.endsWith("\\") && !nsisHome.endsWith(":\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                        nsisHome = nsisHome.substring(0,nsisHome.length()-1);
                        mNSISHome.getCombo().setText(nsisHome);
                    }
                    if(!NSISValidator.validateNSISHome(nsisHome)) {
                        Common.openError(getShell(),EclipseNSISPlugin.getResourceString("invalid.nsis.home.message")); //$NON-NLS-1$ //$NON-NLS-2$
                        mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                        mNSISHome.getCombo().forceFocus();
                    }
                    else {
                        state = true;
                    }
                }
                enableControls(state);
            }
        });

        Button button = createButton(composite, EclipseNSISPlugin.getResourceString("browse.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Shell shell = getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setMessage(EclipseNSISPlugin.getResourceString("nsis.home.message")); //$NON-NLS-1$
                String nsisHome = dialog.open();
                if (!Common.isEmpty(nsisHome)) { 
                    if(NSISValidator.validateNSISHome(nsisHome)) {
                        mNSISHome.getCombo().setText(nsisHome);
                        enableControls(true);
                    }
                    else {
                        Common.openError(getShell(), EclipseNSISPlugin.getResourceString("invalid.nsis.home.message")); //$NON-NLS-1$ //$NON-NLS-2$
                        mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                        mNSISHome.getCombo().setFocus();
                        enableControls(false);
                    }
                }
            }
        });
        
        mAutoShowConsole = createCheckBox(composite, EclipseNSISPlugin.getResourceString("auto.show.console.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("auto.show.console.tooltip"), //$NON-NLS-1$
                                      ((NSISPreferences)getSettings()).isAutoShowConsole());
        ((GridData)mAutoShowConsole.getLayoutData()).horizontalSpan = 2;
        
        mUseEclipseHelp = createCheckBox(composite, EclipseNSISPlugin.getResourceString("use.eclipse.help.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("use.eclipse.help.tooltip"), //$NON-NLS-1$
                                      ((NSISPreferences)getSettings()).isUseEclipseHelp());
        ((GridData)mUseEclipseHelp.getLayoutData()).horizontalSpan = 2;
        return composite;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#enableControls(boolean)
     */
    protected void enableControls(boolean state)
    {
        mAutoShowConsole.setEnabled(state);
        mUseEclipseHelp.setEnabled(state);
        super.enableControls(state);
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
        mUseEclipseHelp.setSelection(true);
        mAutoShowConsole.setSelection(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        Combo combo = mNSISHome.getCombo();
        String home = combo.getText();
        Collection nsisHomes = (Collection)mNSISHome.getInput();
        if(!nsisHomes.contains(home)) {
            nsisHomes.add(home);
            try {
                Common.writeObject(cNSISHomesListFile,nsisHomes);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mNSISHome.refresh();
            combo.setText(home);
        }
        NSISPreferences preferences = (NSISPreferences)getSettings();
        preferences.setNSISHome(home);
        preferences.setAutoShowConsole(mAutoShowConsole.getSelection());
        preferences.setUseEclipseHelp(mUseEclipseHelp.getSelection());
        return super.performOk();
    }
}