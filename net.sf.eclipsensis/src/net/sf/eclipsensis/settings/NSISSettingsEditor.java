/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

public abstract class NSISSettingsEditor implements INSISSettingsEditorPageListener
{
    private NSISSettings mSettings = null;
    private NSISSettingsEditorPage[] mPages = new NSISSettingsEditorPage[2];
    private TabFolder mFolder = null;
    private boolean mEnabledState;

    public void settingsChanged()
    {
        boolean enabledState = mPages[0].canEnableControls() && mPages[1].canEnableControls();
        if(enabledState != mEnabledState) {
            mEnabledState = enabledState;
            enableControls();
        }
    }

    public NSISSettingsEditorPage[] getPages()
    {
        return mPages;
    }

    protected void enableControls()
    {
        mPages[0].enableControls(mEnabledState);
        mPages[1].enableControls(mEnabledState);
        TabItem[] tabItems = mFolder.getItems();
        for(int i=1; i<tabItems.length; i++) {
            tabItems[i].getControl().setEnabled(mEnabledState);
        }
    }

    public Control createControl(Composite parent)
    {
        final TabFolder folder = new TabFolder(parent, SWT.NONE);
        
        mPages[0] = createGeneralPage(); 
        mPages[0].addListener(this);
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("general.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("general.tab.tooltip")); //$NON-NLS-1$
        item.setControl(mPages[0].create(folder));

        mPages[1] = createSymbolsPage();
        mPages[1].addListener(this);
        item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("symbols.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("symbols.tab.tooltip")); //$NON-NLS-1$
        item.setControl(mPages[1].create(folder));

        mEnabledState = mPages[0].canEnableControls() && mPages[1].canEnableControls();
        
        folder.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        try {
                            TabItem item = folder.getSelection()[0];
                            if(!item.getControl().isEnabled()) {
                                folder.setSelection(0);
                            }
                        }
                        catch(Exception ex) {
                            EclipseNSISPlugin.getDefault().log(ex);
                        }
                    }
                });

        Dialog.applyDialogFont(folder);
        mFolder = folder;
        enableControls();
        return mFolder;
    }

    protected NSISSettingsEditorSymbolsPage createSymbolsPage()
    {
        return new NSISSettingsEditorSymbolsPage(getSettings());
    }

    protected abstract NSISSettingsEditorGeneralPage createGeneralPage();

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public final boolean performApply()
    {
        for(int i=0; i<mPages.length; i++) {
            if(!mPages[i].performApply()) {
                return false;
            }
        }
        getSettings().store();
        return true;
    }

    public boolean isValid()
    {
        return true;
    }
    
    /**
     * @return Returns the settings.
     */
    public NSISSettings getSettings()
    {
        if(mSettings == null) {
            mSettings = loadSettings();
        }
        return mSettings;
    }

    protected abstract NSISSettings loadSettings();
}
