/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.*;
import java.util.List;

import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class JARSignerOptionsDialog extends AbstractJAROptionsDialog
{
    private static final String STORE_PASS = "store.pass"; //$NON-NLS-1$
    private static final String ALIAS = "alias"; //$NON-NLS-1$
    private static final String STORE_TYPE = "store.type"; //$NON-NLS-1$
    private static final String KEY_PASS = "key.pass"; //$NON-NLS-1$
    private static final String SIG_FILE = "sig.file"; //$NON-NLS-1$
    private static final String SIGNED_JAR = "signed.jar"; //$NON-NLS-1$
    private static final String INTERNAL_SF = "internal.sf"; //$NON-NLS-1$
    private static final String SECTIONS_ONLY = "sections.only"; //$NON-NLS-1$

    private List mAliases;
    private ComboViewer mComboViewer;
    
    /**
     * @param parentShell
     * @throws KeyStoreException 
     */
    public JARSignerOptionsDialog(Shell parentShell, boolean multi) throws KeyStoreException
    {
        super(parentShell, multi);
    }

    protected void init()
    {
        super.init();
        String storePass = getStringDialogSetting(STORE_PASS);
        String keyStore = getStringDialogSetting(KEY_STORE);
        KeyStore ks = JARSignerPlugin.loadKeyStore(keyStore,storePass);
        mAliases = new ArrayList();
        if(ks != null) {
            mValues.put(KEY_STORE,keyStore);
            mValues.put(STORE_PASS,storePass);
            try {
                mAliases.addAll(Collections.list(ks.aliases()));
                String alias = getStringDialogSetting(ALIAS);
                if(mAliases.contains(alias)) {
                    mValues.put(ALIAS,alias);
                }
                else {
                    if(mAliases.size() > 0) {
                        mValues.put(ALIAS,mAliases.get(0));
                    }
                    else {
                        mValues.put(ALIAS,""); //$NON-NLS-1$
                    }
                }
            }
            catch (KeyStoreException e) {
                e.printStackTrace();
                mValues.put(ALIAS,""); //$NON-NLS-1$
            }
        }
        else {
            mValues.put(KEY_STORE,""); //$NON-NLS-1$
            mValues.put(STORE_PASS,""); //$NON-NLS-1$
            mValues.put(ALIAS,""); //$NON-NLS-1$
        }

        mValues.put(STORE_TYPE,getStringDialogSetting(STORE_TYPE));
        mValues.put(KEY_PASS,getStringDialogSetting(KEY_PASS));
        mValues.put(SIG_FILE,getStringDialogSetting(SIG_FILE));
        if(!mMulti) {
            mValues.put(SIGNED_JAR,getStringDialogSetting(SIGNED_JAR));
        }
        mValues.put(INTERNAL_SF,mDialogSettings.getBoolean(INTERNAL_SF)?Boolean.TRUE:Boolean.FALSE);
        mValues.put(SECTIONS_ONLY,mDialogSettings.getBoolean(SECTIONS_ONLY)?Boolean.TRUE:Boolean.FALSE);
    }

    protected String getDialogTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.dialog.title"); //$NON-NLS-1$
    }

    protected Control createDialogArea(Composite parent)
    {
        parent = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent.getLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Button b = (Button)e.widget;
                Text text = (Text)b.getData(ATTR_TEXT); //$NON-NLS-1$
                if(text != null) {
                    KeyStoreDialog dialog = new KeyStoreDialog(getShell(),getKeyStore(),getStorePass());
                    if(dialog.open() == Window.OK) {
                        mValues.put(KEY_STORE, dialog.getKeyStoreName());
                        text.setText(getKeyStore());
                        mValues.put(STORE_PASS,dialog.getStorePassword());
                        KeyStore keyStore = dialog.getKeyStore();
                        String alias = getAlias();
                        mAliases.clear();
                        try {
                            mAliases.addAll(Collections.list(keyStore.aliases()));
                        }
                        catch (KeyStoreException e1) {
                            e1.printStackTrace();
                        }
                        mComboViewer.refresh(true);
                        if(!mAliases.contains(alias)) {
                            if(mAliases.size() > 0) {
                                alias = (String)mAliases.get(0);
                            }
                            else {
                                alias = ""; //$NON-NLS-1$
                            }
                        }
                        mValues.put(ALIAS,alias);
                        mComboViewer.setSelection(new StructuredSelection(alias));
                    }
                }
            }
        };
        Text t = makeFileBrowser(composite,"key.store.location", KEY_STORE, sa, true); //$NON-NLS-1$
        t.setEditable(false);
        GridData gd = (GridData)t.getLayoutData();
        gd.widthHint = convertWidthInCharsToPixels(50);

        makeLabel(composite, ALIAS, true); //$NON-NLS-1$
        final Combo combo = new Combo(composite,SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        combo.setLayoutData(gd);
        mComboViewer = new ComboViewer(combo);
        mComboViewer.setContentProvider(new ArrayContentProvider());
        mComboViewer.setLabelProvider(new LabelProvider());
        mComboViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                mValues.put(ALIAS,combo.getText());
                updateButtons();
            }
        });
        mComboViewer.setInput(mAliases);
        mComboViewer.setSelection(new StructuredSelection(getAlias()));
        
        makeText(composite,STORE_TYPE,STORE_TYPE,false); //$NON-NLS-1$
        makeText(composite,KEY_PASS,KEY_PASS,false); //$NON-NLS-1$
        makeText(composite,SIG_FILE,SIG_FILE,false); //$NON-NLS-1$

        if(!mMulti) {
            makeFileBrowser(composite,"signed.jar.location", SIGNED_JAR,  //$NON-NLS-1$
                    new FileSelectionAdapter("signed.jar.location.message","",false), //$NON-NLS-1$ //$NON-NLS-2$
                    false); //$NON-NLS-1$
        }
        
        makeCheckBox(composite,VERBOSE,VERBOSE,false); //$NON-NLS-1$
        makeCheckBox(composite,INTERNAL_SF,INTERNAL_SF,false); //$NON-NLS-1$
        makeCheckBox(composite,SECTIONS_ONLY,SECTIONS_ONLY,false); //$NON-NLS-1$
        
        makeFileBrowser(composite,"tools.jar.location", TOOLS_JAR,  //$NON-NLS-1$
                new FileSelectionAdapter("tools.jar.location.message",JARSignerPlugin.getResourceString("tools.jar.name"),false), //$NON-NLS-1$ //$NON-NLS-2$
                true); //$NON-NLS-1$
        
        if(mMulti) {
            makeCheckBox(composite,IGNORE_ERRORS,IGNORE_ERRORS,false); //$NON-NLS-1$
        }
        return parent;
    }
    
    protected boolean isValid()
    {
        boolean state = !JARSignerPlugin.isEmpty(getKeyStore());
        
        if(state) {
            state = !JARSignerPlugin.isEmpty(getAlias());
            if(state) {
                File f = new File(getKeyStore());
                state = (f.exists() && f.isFile());
                
                String signedJar = getSignedJar();
                if(state) {
                    if(!JARSignerPlugin.isEmpty(signedJar)) {
                        File file = new File(signedJar);
                        state = !file.exists() || !file.isDirectory();
                    }
                    
                    if(state) {
                        f = new File(getToolsJar());
                        state = (f.exists() && f.isFile());
                    }
                }
            }
        }
        return state;
    }

    protected void okPressed()
    {
        mDialogSettings.put(STORE_PASS,getStorePass());
        mDialogSettings.put(ALIAS,getAlias());
        mDialogSettings.put(STORE_TYPE,getStoreType());
        mDialogSettings.put(KEY_PASS,getKeyPass());
        mDialogSettings.put(SIG_FILE,getSigFile());
        if(!mMulti) {
            mDialogSettings.put(SIGNED_JAR,getSignedJar());
        }
        mDialogSettings.put(INTERNAL_SF,isInternalSF());
        mDialogSettings.put(SECTIONS_ONLY,isSectionsOnly());
        super.okPressed();
    }

    public String getAlias()
    {
        return (String)mValues.get(ALIAS);
    }

    public boolean isInternalSF()
    {
        return ((Boolean)mValues.get(INTERNAL_SF)).booleanValue();
    }

    public String getKeyPass()
    {
        return (String)mValues.get(KEY_PASS);
    }

    public boolean isSectionsOnly()
    {
        return ((Boolean)mValues.get(SECTIONS_ONLY)).booleanValue();
    }

    public String getSigFile()
    {
        return (String)mValues.get(SIG_FILE);
    }

    public String getSignedJar()
    {
        return (mMulti?"":(String)mValues.get(SIGNED_JAR)); //$NON-NLS-1$
    }

    public String getStorePass()
    {
        return (String)mValues.get(STORE_PASS);
    }

    public String getStoreType()
    {
        return (String)mValues.get(STORE_TYPE);
    }
}
