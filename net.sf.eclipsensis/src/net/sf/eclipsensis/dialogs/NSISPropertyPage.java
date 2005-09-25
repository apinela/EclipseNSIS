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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISProperties;
import net.sf.eclipsensis.settings.NSISSettings;

import org.eclipse.core.resources.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class NSISPropertyPage extends NSISSettingsPage
{
    private Button mUseParent = null;
    
    /**
     * @return
     */
    protected String getContextId()
    {
        return PLUGIN_CONTEXT_PREFIX + "nsis_properties_context"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#canEnableControls()
     */
    protected boolean canEnableControls()
    {
        return !mUseParent.getSelection();
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#createEnablerControl(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createMasterControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        String label = null;
        String tooltip = null;
        IResource resource = (IResource)getElement();
        if(resource instanceof IFile) {
            label = "folder.options.label"; //$NON-NLS-1$
            tooltip = "folder.options.tooltip"; //$NON-NLS-1$
        }
        else if(resource instanceof IFolder) {
            if(((IFolder)resource).getParent() instanceof IProject) {
                label = "project.options.label"; //$NON-NLS-1$
                tooltip = "project.options.tooltip"; //$NON-NLS-1$
            }
            else {
                label = "folder.options.label"; //$NON-NLS-1$
                tooltip = "folder.options.tooltip"; //$NON-NLS-1$
            }
        }
        else {
            label = "global.options.label"; //$NON-NLS-1$
            tooltip = "global.options.tooltip"; //$NON-NLS-1$
        }
        mUseParent = createCheckBox(composite, 
                                    EclipseNSISPlugin.getResourceString(label),
                                    EclipseNSISPlugin.getResourceString(tooltip),
                                    ((NSISProperties)getSettings()).getUseParent());
        mUseParent.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                enableControls(canEnableControls());
            }
        });
        
        return composite;
    }

    protected void enableControls(boolean state)
    {
        Button button = getDefaultsButton();
        if(button != null && !button.isDisposed()) {
            button.setEnabled(state);
        }
        super.enableControls(state);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#getPageDescription()
     */
    protected String getPageDescription()
    {
        String label = null;
        IResource resource = (IResource)getElement();
        if(resource instanceof IFile) {
            label = "file.properties.header.text"; //$NON-NLS-1$
        }
        else if(resource instanceof IFolder) {
            label = "folder.properties.header.text"; //$NON-NLS-1$
        }
        else {
            label = "project.properties.header.text"; //$NON-NLS-1$
        }
        return EclipseNSISPlugin.getResourceString(label); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#loadSettings()
     */
    protected NSISSettings loadSettings()
    {
        return NSISProperties.getProperties((IResource)getElement());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        if(!mUseParent.getSelection()) {
            NSISSettings properties = (NSISProperties)getSettings();
            mHdrInfo.setSelection(properties.getDefaultHdrInfo());
            mLicense.setSelection(properties.getDefaultLicense());
            mNoConfig.setSelection(properties.getDefaultNoConfig());
            mNoCD.setSelection(properties.getDefaultNoCD());
            mVerbosity.select(properties.getDefaultVerbosity());
            mCompressor.select(properties.getDefaultCompressor());
            mSolidCompression.setSelection(properties.getDefaultSolidCompression());
            mInstructions.setInput(properties.getDefaultInstructions());
            mSymbols.setInput(properties.getDefaultSymbols());
            super.performDefaults();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        boolean useDefaults = mUseParent.getSelection();
        NSISProperties properties = (NSISProperties)getSettings();
        properties.setUseParent(useDefaults);
        if(useDefaults) {
            properties.store();
            return true;
        }
        else {
            return super.performOk();
        }
    }

    protected boolean isSolidCompressionSupported()
    {
        return NSISKeywords.INSTANCE.isValidKeyword("/SOLID"); //$NON-NLS-1$
    }
}