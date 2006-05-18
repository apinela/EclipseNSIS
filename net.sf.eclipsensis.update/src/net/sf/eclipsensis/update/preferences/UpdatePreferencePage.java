/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.util.NumberVerifyListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class UpdatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IUpdatePreferenceConstants
{
    private Button mUseHttpProxy;
    private Text mHttpProxyHost;
    private Text mHttpProxyPort;
    private Text mNSISUpdateSite;
    private Text mSourceforgeMirror;
    private Button mIgnorePreview;
    private ModifyListener mModifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e)
        {
            updateState();
        }
    };

    protected IPreferenceStore doGetPreferenceStore()
    {
        return EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
    }

    protected Control createContents(Composite parent)
    {
        parent = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        Label l= new Label(parent, SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("preference.page.header")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        createProxyGroup(parent);
        createSitesGroup(parent);
        createOptionsGroup(parent);

        loadPreferences();
        updateState();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,EclipseNSISUpdatePlugin.PLUGIN_CONTEXT_PREFIX+"nsis_update_prefs_context"); //$NON-NLS-1$
        return parent;
    }

    private void loadPreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        
        mUseHttpProxy.setSelection(prefs.getBoolean(USE_HTTP_PROXY));
        mHttpProxyHost.setText(prefs.getString(HTTP_PROXY_HOST));
        int proxyPort = prefs.getInt(HTTP_PROXY_PORT);
        if(proxyPort > 0 && proxyPort <= 0xFFFF) {
            mHttpProxyPort.setText(Integer.toString(proxyPort));
        }
        else {
            mHttpProxyPort.setText(""); //$NON-NLS-1$
        }
        
        mNSISUpdateSite.setText(prefs.getString(NSIS_UPDATE_SITE));
        mSourceforgeMirror.setText(prefs.getString(SOURCEFORGE_MIRROR));
        
        mIgnorePreview.setSelection(prefs.getBoolean(IGNORE_PREVIEW));
        
        updateProxyGroup();
    }

    private void updateProxyGroup()
    {
        Enabler enabler = Enabler.get(mUseHttpProxy);
        if(enabler != null) {
            enabler.run();
        }
    }

    private void loadDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();

        mUseHttpProxy.setSelection(prefs.getDefaultBoolean(USE_HTTP_PROXY));
        mHttpProxyHost.setText(prefs.getDefaultString(HTTP_PROXY_HOST));
        int proxyPort = prefs.getDefaultInt(HTTP_PROXY_PORT);
        if(proxyPort > 0 && proxyPort <= 0xFFFF) {
            mHttpProxyPort.setText(Integer.toString(proxyPort));
        }
        else {
            mHttpProxyPort.setText(""); //$NON-NLS-1$
        }
        
        mNSISUpdateSite.setText(prefs.getDefaultString(NSIS_UPDATE_SITE));
        mSourceforgeMirror.setText(prefs.getDefaultString(SOURCEFORGE_MIRROR));
        
        mIgnorePreview.setSelection(prefs.getDefaultBoolean(IGNORE_PREVIEW));
        
        updateProxyGroup();
    }

    private void savePreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        prefs.setValue(USE_HTTP_PROXY,mUseHttpProxy.getSelection());
        prefs.setValue(HTTP_PROXY_HOST,mHttpProxyHost.getText());
        prefs.setValue(HTTP_PROXY_PORT,mHttpProxyPort.getText());
        
        prefs.setValue(NSIS_UPDATE_SITE,mNSISUpdateSite.getText());
        prefs.setValue(SOURCEFORGE_MIRROR,mSourceforgeMirror.getText());
        
        prefs.setValue(IGNORE_PREVIEW,mIgnorePreview.getSelection());
    }

    private void createProxyGroup(Composite parent)
    {
        GridLayout layout;
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("proxy.group.label")); //$NON-NLS-1$
        layout = new GridLayout(2,false);
        group.setLayout(layout);
        
        mUseHttpProxy = new Button(group, SWT.CHECK);
        mUseHttpProxy.setText(EclipseNSISUpdatePlugin.getResourceString("use.http.proxy.label")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,false);
        gridData.horizontalSpan = 2;
        mUseHttpProxy.setLayoutData(gridData);
        mUseHttpProxy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateState();
            }
        });
        
        Label l1 = new Label(group,SWT.NONE);
        l1.setText(EclipseNSISUpdatePlugin.getResourceString("http.proxy.host.label")); //$NON-NLS-1$
        l1.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mHttpProxyHost = new Text(group,SWT.BORDER|SWT.SINGLE);
        mHttpProxyHost.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        mHttpProxyHost.addModifyListener(mModifyListener);
        
        Label l2 = new Label(group,SWT.NONE);
        l2.setText(EclipseNSISUpdatePlugin.getResourceString("http.proxy.port.label")); //$NON-NLS-1$
        l2.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mHttpProxyPort = new Text(group,SWT.BORDER|SWT.SINGLE);
        mHttpProxyPort.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        mHttpProxyPort.addVerifyListener(new NumberVerifyListener());
        mHttpProxyPort.addModifyListener(mModifyListener);
        
        new Enabler(mUseHttpProxy, new Control[] {l1, mHttpProxyHost, l2, mHttpProxyPort});
    }

    private void createSitesGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("sites.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2,false));

        Label l = new Label(group,SWT.NONE);
        Font f = l.getFont();
        FontData[] fd = f.getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
        }
        final Font f2 = new Font(getShell().getDisplay(),fd);
        l.setFont(f2);
        l.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                f2.dispose();
            }
        });
        l.setText(EclipseNSISUpdatePlugin.getResourceString("sites.group.message")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gridData.horizontalSpan = 2;
        l.setLayoutData(gridData);
        
        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("nsis.update.site.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mNSISUpdateSite = new Text(group,SWT.BORDER|SWT.SINGLE);
        mNSISUpdateSite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        mNSISUpdateSite.addModifyListener(mModifyListener);
        
        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("sourceforge.mirror.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mSourceforgeMirror = new Text(group,SWT.BORDER|SWT.SINGLE);
        mSourceforgeMirror.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        mSourceforgeMirror.addModifyListener(mModifyListener);
    }

    private void createOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.options.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));
        
        mIgnorePreview = new Button(group, SWT.CHECK);
        mIgnorePreview.setText(EclipseNSISUpdatePlugin.getResourceString("ignore.preview.label")); //$NON-NLS-1$
        mIgnorePreview.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
    }
    
    private void updateState()
    {
        setValid(validate());
    }

    private boolean validate()
    {
        String text = mNSISUpdateSite.getText();
        if(text == null || text.trim().length() == 0) {
            setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("missing.nsis.update.site.error")); //$NON-NLS-1$
            return false;
        }
        text = mSourceforgeMirror.getText();
        if(text == null || text.trim().length() == 0) {
            setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("missing.sourceforge.mirror.error")); //$NON-NLS-1$
            return false;
        }
        if(mUseHttpProxy.getSelection()) {
            text = mHttpProxyHost.getText();
            if(text == null || text.trim().length() == 0) {
                setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("missing.proxy.host.error")); //$NON-NLS-1$
                return false;
            }
            text = mHttpProxyPort.getText();
            if(text != null && text.length() > 0) {
                try {
                    int port = Integer.parseInt(text);
                    if(port < 1 || port > 0xFFFF) {
                        setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("invalid.proxy.port.error")); //$NON-NLS-1$
                        return false;
                    }
                }
                catch(NumberFormatException nfe) {
                    setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("invalid.proxy.port.error")); //$NON-NLS-1$
                    return false;
                }
            }
        }
        setErrorMessage(null);
        return true;
    }

    public boolean performOk()
    {
        boolean ok = super.performOk();
        if(ok) {
            ok = validate();
            if(ok) {
                savePreferences();
            }
        }
        return ok;
    }

    protected void performDefaults()
    {
        loadDefaults();
        super.performDefaults();
    }

    public void init(IWorkbench workbench)
    {
    }
}
