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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISProperties;
import net.sf.eclipsensis.settings.NSISSettings;

public class NSISPropertyPage extends NSISSettingsPage
{
    private Button mUseDefaults = null;
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#canEnableControls()
     */
    protected boolean canEnableControls()
    {
        return !mUseDefaults.getSelection();
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#createEnablerControl(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createEnablerControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NULL);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        mUseDefaults = createCheckBox(composite, EclipseNSISPlugin.getResourceString("use.defaults.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("use.defaults.tooltip"), //$NON-NLS-1$
                                      ((NSISProperties)getSettings()).getUseDefaults());
        mUseDefaults.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                enableControls(canEnableControls());
            }
        });
        
        return composite;
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#getPageDescription()
     */
    protected String getPageDescription()
    {
        return EclipseNSISPlugin.getResourceString("properties.header.text"); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#loadSettings()
     */
    protected NSISSettings loadSettings()
    {
        return NSISProperties.getProperties((IFile)getElement());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        super.performDefaults();
        NSISPreferences preferences = NSISPreferences.getPreferences();
        mHdrInfo.setSelection(preferences.getHdrInfo());
        mLicense.setSelection(preferences.getLicense());
        mNoConfig.setSelection(preferences.getNoConfig());
        mNoCD.setSelection(preferences.getNoCD());
        mVerbosity.select(preferences.getVerbosity());
        mCompressor.select(preferences.getCompressor());
        mInstructions.setInput(preferences.getInstructions());
        mSymbols.setInput(preferences.getSymbols());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        boolean useDefaults = mUseDefaults.getSelection();
        NSISProperties properties = (NSISProperties)getSettings();
        properties.setUseDefaults(useDefaults);
        if(useDefaults) {
            
        }
        return super.performOk();
    }
}