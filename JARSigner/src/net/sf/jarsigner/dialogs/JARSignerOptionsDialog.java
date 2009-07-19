/*******************************************************************************
 * Copyright (c) 2005-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.io.File;
import java.security.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
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

    private List<String> mAliases;
    private ComboViewer mComboViewer;

    /**
     * @param parentShell
     */
    public JARSignerOptionsDialog(Shell parentShell, List<?> selection)
    {
        super(parentShell, selection);
    }

    @Override
	protected void init()
    {
        super.init();
        String storePass = getStringDialogSetting(STORE_PASS);
        String keyStore = getKeyStore();
        KeyStore ks = JARSignerPlugin.loadKeyStore(keyStore,storePass);
        mAliases = new ArrayList<String>();
        if(ks != null) {
            setValue(STORE_PASS,storePass);
            try {
                mAliases.addAll(Collections.list(ks.aliases()));
                String alias = getStringDialogSetting(ALIAS);
                if(mAliases.contains(alias)) {
                    setValue(ALIAS,alias);
                }
                else {
                    if(mAliases.size() > 0) {
                        setValue(ALIAS,mAliases.get(0));
                    }
                    else {
                        setValue(ALIAS,""); //$NON-NLS-1$
                    }
                }
            }
            catch (KeyStoreException e) {
                e.printStackTrace();
                setValue(ALIAS,""); //$NON-NLS-1$
            }
        }
        else {
            setValue(KEY_STORE,""); //$NON-NLS-1$
            setValue(STORE_PASS,""); //$NON-NLS-1$
            setValue(ALIAS,""); //$NON-NLS-1$
        }

        setValue(STORE_TYPE,getStringDialogSetting(STORE_TYPE));
        setValue(KEY_PASS,getStringDialogSetting(KEY_PASS));
        setValue(SIG_FILE,getStringDialogSetting(SIG_FILE));
        if(getSelection().size() <= 1) {
            setValue(SIGNED_JAR,getStringDialogSetting(SIGNED_JAR));
        }
        setValue(INTERNAL_SF,getDialogSettings().getBoolean(INTERNAL_SF)?Boolean.TRUE:Boolean.FALSE);
        setValue(SECTIONS_ONLY,getDialogSettings().getBoolean(SECTIONS_ONLY)?Boolean.TRUE:Boolean.FALSE);
    }

    @Override
	protected String getDialogTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.dialog.title"); //$NON-NLS-1$
    }

    @Override
	protected void createValuesDialogArea(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e)
            {
                Button b = (Button)e.widget;
                Text text = (Text)b.getData(ATTR_TEXT);
                if(text != null) {
                    String store = getKeyStore();
                    if(Common.isEmpty(store)) {
                        store = getDefaultKeyStore();
                    }
                    KeyStoreDialog dialog = new KeyStoreDialog(getShell(),store,getStorePass());
                    if(dialog.open() == Window.OK) {
                        setValue(KEY_STORE, dialog.getKeyStoreName());
                        text.setText(getKeyStore());
                        setValue(STORE_PASS,dialog.getStorePassword());
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
                                alias = mAliases.get(0);
                            }
                            else {
                                alias = ""; //$NON-NLS-1$
                            }
                        }
                        setValue(ALIAS,alias);
                        mComboViewer.setSelection(new StructuredSelection(alias));
                    }
                }
            }
        };
        Text t = makeFileBrowser(composite,JARSignerPlugin.getResourceString("key.store.location"), KEY_STORE, sa, true); //$NON-NLS-1$
        t.setEditable(false);
        gd = (GridData)t.getLayoutData();
        gd.widthHint = convertWidthInCharsToPixels(50);

        makeLabel(composite, JARSignerPlugin.getResourceString(ALIAS), true);
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
                setValue(ALIAS,combo.getText());
            }
        });
        mComboViewer.setInput(mAliases);
        mComboViewer.setSelection(new StructuredSelection(getAlias()));

        makeText(composite,JARSignerPlugin.getResourceString(STORE_TYPE),STORE_TYPE,false);
        makeText(composite,JARSignerPlugin.getResourceString(KEY_PASS),KEY_PASS,false);
        makeText(composite,JARSignerPlugin.getResourceString(SIG_FILE),SIG_FILE,false);

        if(getSelection().size() <= 1) {
            makeFileBrowser(composite,JARSignerPlugin.getResourceString("signed.jar.location"), SIGNED_JAR,  //$NON-NLS-1$
                    new FileSelectionAdapter("signed.jar.location.message","",false), //$NON-NLS-1$ //$NON-NLS-2$
                    false);
        }

    }

    @Override
	protected void createFlagsDialogArea(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);
        makeCheckBox(composite,JARSignerPlugin.getResourceString(INTERNAL_SF),INTERNAL_SF,false);
        makeCheckBox(composite,JARSignerPlugin.getResourceString(SECTIONS_ONLY),SECTIONS_ONLY,false);
    }

    @Override
	protected boolean isValid()
    {
        if(super.isValid()) {
            boolean state = !Common.isEmpty(getAlias());
            if(state) {
                String signedJar = getSignedJar();
                if(!Common.isEmpty(signedJar)) {
                    File file = new File(signedJar);
                    state = !Common.isValidDirectory(file);
                }
            }
            return state;
        }
        return false;
    }

    @Override
	protected void okPressed()
    {
        getDialogSettings().put(STORE_PASS,getStorePass());
        getDialogSettings().put(ALIAS,getAlias());
        getDialogSettings().put(STORE_TYPE,getStoreType());
        getDialogSettings().put(KEY_PASS,getKeyPass());
        getDialogSettings().put(SIG_FILE,getSigFile());
        if(getSelection().size() <= 1) {
            getDialogSettings().put(SIGNED_JAR,getSignedJar());
        }
        getDialogSettings().put(INTERNAL_SF,isInternalSF());
        getDialogSettings().put(SECTIONS_ONLY,isSectionsOnly());
        super.okPressed();
    }

    public String getAlias()
    {
        return (String)getValues().get(ALIAS);
    }

    public boolean isInternalSF()
    {
        return ((Boolean)getValues().get(INTERNAL_SF)).booleanValue();
    }

    public String getKeyPass()
    {
        return (String)getValues().get(KEY_PASS);
    }

    public boolean isSectionsOnly()
    {
        return ((Boolean)getValues().get(SECTIONS_ONLY)).booleanValue();
    }

    public String getSigFile()
    {
        return (String)getValues().get(SIG_FILE);
    }

    public String getSignedJar()
    {
        return getSelection().size()>1?"":(String)getValues().get(SIGNED_JAR); //$NON-NLS-1$
    }

    public String getStorePass()
    {
        return (String)getValues().get(STORE_PASS);
    }

    public String getStoreType()
    {
        return (String)getValues().get(STORE_TYPE);
    }
}
