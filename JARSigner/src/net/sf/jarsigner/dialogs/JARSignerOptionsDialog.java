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

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class JARSignerOptionsDialog extends Dialog
{
    private static final String TOOLS_JAR = "tools.jar"; //$NON-NLS-1$
    private static final String KEY_STORE = "key.store"; //$NON-NLS-1$
    private static final String STORE_PASS = "store.pass"; //$NON-NLS-1$
    private static final String ALIAS = "alias"; //$NON-NLS-1$
    private static final String STORE_TYPE = "store.type"; //$NON-NLS-1$
    private static final String KEY_PASS = "key.pass"; //$NON-NLS-1$
    private static final String SIG_FILE = "sig.file"; //$NON-NLS-1$
    private static final String SIGNED_JAR = "signed.jar"; //$NON-NLS-1$
    private static final String VERBOSE = "verbose"; //$NON-NLS-1$
    private static final String INTERNAL_SF = "internal.sf"; //$NON-NLS-1$
    private static final String SECTIONS_ONLY = "sections.only"; //$NON-NLS-1$
    private static final String IGNORE_ERRORS = "ignore.errors"; //$NON-NLS-1$

    private Map mValues = new HashMap();
    private IDialogSettings mDialogSettings;
    private boolean mMulti = false;
    private List mAliases = new ArrayList();
    private ComboViewer mComboViewer;
    
    /**
     * @param parentShell
     * @throws KeyStoreException 
     */
    public JARSignerOptionsDialog(Shell parentShell, boolean multi) throws KeyStoreException
    {
        super(parentShell);
        mMulti = multi;
        init();
    }

    private void init()
    {
        IDialogSettings dialogSettings = JARSignerPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        String toolsJar = getStringDialogSetting(TOOLS_JAR);
        if(JARSignerPlugin.isEmpty(toolsJar)) {
            IVMInstall vm = JavaRuntime.getDefaultVMInstall();
            if(vm == null) {
                throw new RuntimeException(JARSignerPlugin.getResourceString("vm.not.found")); //$NON-NLS-1$
            }
            else {
                File jdkHome = vm.getInstallLocation();
                File toolPath = new File(jdkHome,"lib/tools.jar"); //$NON-NLS-1$
                if(toolPath.exists() && toolPath.isFile()) {
                    toolsJar = toolPath.getAbsolutePath();
                }
            }
        }
        mValues.put(TOOLS_JAR,toolsJar);

        String keyStore = getStringDialogSetting(KEY_STORE);
        if(JARSignerPlugin.isEmpty(keyStore)) {
            String userHome = System.getProperty("user.home"); //$NON-NLS-1$
            File storePath = new File(userHome,".keystore"); //$NON-NLS-1$
            if(storePath.exists() && storePath.isFile()) {
                keyStore = storePath.getAbsolutePath();
            }
        }
        String storePass = getStringDialogSetting(STORE_PASS);

        KeyStore ks = JARSignerPlugin.loadKeyStore(keyStore,storePass);
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
        else {
            mValues.put(IGNORE_ERRORS,mDialogSettings.getBoolean(IGNORE_ERRORS)?Boolean.TRUE:Boolean.FALSE);
        }
        mValues.put(VERBOSE,mDialogSettings.getBoolean(VERBOSE)?Boolean.TRUE:Boolean.FALSE);
        mValues.put(INTERNAL_SF,mDialogSettings.getBoolean(INTERNAL_SF)?Boolean.TRUE:Boolean.FALSE);
        mValues.put(SECTIONS_ONLY,mDialogSettings.getBoolean(SECTIONS_ONLY)?Boolean.TRUE:Boolean.FALSE);
    }

    private String getStringDialogSetting(String name)
    {
        String str = mDialogSettings.get(name);
        return (str==null?"":str); //$NON-NLS-1$
    }

    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(JARSignerPlugin.getResourceString("jarsigner.dialog.title")); //$NON-NLS-1$
    }
    
    private void makeBold(Control c)
    {
        FontData[] fd = c.getFont().getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].setStyle(SWT.BOLD);
        }
        final Font f = new Font(c.getDisplay(),fd);
        c.setFont(f);
        c.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                f.dispose();
            }
        });
    }
    
    private Button makeCheckBox(Composite composite, String label, final String property, boolean isRequired)
    {
        final Button b = new Button(composite, SWT.CHECK);
        if(isRequired) {
            makeBold(b);
        }
        b.setText(JARSignerPlugin.getResourceString(label)); //$NON-NLS-1$
        b.setSelection(((Boolean)mValues.get(property)).booleanValue());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mValues.put(property,b.getSelection()?Boolean.TRUE:Boolean.FALSE);
                validate();
            }
        });
        return b;
    }
    
    private Text makeText(Composite composite, String label, final String property, boolean isRequired)
    {
        makeLabel(composite, label, isRequired);
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mValues.put(property,text.getText());
                validate();
            }
        });
        
        return text;
    }

    private Text makeFileBrowser(Composite composite, String label, final String property,
                                 SelectionListener listener, boolean isRequired)
    {
        makeLabel(composite, label, isRequired);
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mValues.put(property,text.getText());
                validate();
            }
        });
        Button b = new Button(composite,SWT.PUSH);
        b.setLayoutData(new GridData());
        b.setText(JARSignerPlugin.getResourceString("browse.label")); //$NON-NLS-1$
        if(listener != null) {
            b.addSelectionListener(listener);
        }
        text.setData("button",b); //$NON-NLS-1$
        b.setData("text",text); //$NON-NLS-1$
        return text;
    }

    private void makeLabel(Composite composite, String label, boolean isRequired)
    {
        Label l = new Label(composite, SWT.NONE);
        if(isRequired) {
            makeBold(l);
        }
        l.setText(JARSignerPlugin.getResourceString(label)); //$NON-NLS-1$
        l.setLayoutData(new GridData());
    }

    protected Control createDialogArea(Composite parent)
    {
        parent = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent.getLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Button b = (Button)e.widget;
                Text text = (Text)b.getData("text"); //$NON-NLS-1$
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

        makeLabel(composite, "alias", true); //$NON-NLS-1$
        final Combo combo = new Combo(composite,SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        combo.setLayoutData(gd);
        mComboViewer = new ComboViewer(combo);
        mComboViewer.setContentProvider(new ArrayContentProvider());
        mComboViewer.setLabelProvider(new LabelProvider());
        mComboViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                mValues.put(ALIAS,combo.getText());
                validate();
            }
        });
        mComboViewer.setInput(mAliases);
        mComboViewer.setSelection(new StructuredSelection(getAlias()));
        
        makeText(composite,"store.type",STORE_TYPE,false); //$NON-NLS-1$
        makeText(composite,"key.pass",KEY_PASS,false); //$NON-NLS-1$
        makeText(composite,"sig.file",SIG_FILE,false); //$NON-NLS-1$

        if(!mMulti) {
            makeFileBrowser(composite,"signed.jar.location", SIGNED_JAR,  //$NON-NLS-1$
                    new FileSelectionAdapter("signed.jar.location.message","",false), //$NON-NLS-1$ //$NON-NLS-2$
                    false); //$NON-NLS-1$
        }
        
        makeCheckBox(composite,"verbose",VERBOSE,false); //$NON-NLS-1$
        makeCheckBox(composite,"internal.sf",INTERNAL_SF,false); //$NON-NLS-1$
        makeCheckBox(composite,"sections.only",SECTIONS_ONLY,false); //$NON-NLS-1$
        
        makeFileBrowser(composite,"tools.jar.location", TOOLS_JAR,  //$NON-NLS-1$
                new FileSelectionAdapter("tools.jar.location.message",JARSignerPlugin.getResourceString("tools.jar.name"),false), //$NON-NLS-1$ //$NON-NLS-2$
                true); //$NON-NLS-1$
        
        if(mMulti) {
            makeCheckBox(composite,"ignore",IGNORE_ERRORS,false); //$NON-NLS-1$
        }
        return parent;
    }
    
    public void create()
    {
        super.create();
        validate();
    }

    private void validate()
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
        Button button = getButton(IDialogConstants.OK_ID);
        if(button != null) {
            button.setEnabled(state);
        }
    }

    protected void okPressed()
    {
        mDialogSettings.put(TOOLS_JAR,getToolsJar());
        mDialogSettings.put(KEY_STORE,getKeyStore());
        mDialogSettings.put(STORE_PASS,getStorePass());
        mDialogSettings.put(ALIAS,getAlias());
        mDialogSettings.put(STORE_TYPE,getStoreType());
        mDialogSettings.put(KEY_PASS,getKeyPass());
        mDialogSettings.put(SIG_FILE,getSigFile());
        if(!mMulti) {
            mDialogSettings.put(SIGNED_JAR,getSignedJar());
        }
        mDialogSettings.put(VERBOSE,isVerbose());
        mDialogSettings.put(INTERNAL_SF,isInternalSF());
        mDialogSettings.put(SECTIONS_ONLY,isSectionsOnly());
        if(mMulti) {
            mDialogSettings.put(IGNORE_ERRORS,isIgnoreErrors());
        }
        super.okPressed();
    }

    public String getToolsJar()
    {
        return (String)mValues.get(TOOLS_JAR);
    }

    public String getAlias()
    {
        return (String)mValues.get(ALIAS);
    }

    public boolean isInternalSF()
    {
        return ((Boolean)mValues.get(INTERNAL_SF)).booleanValue();
    }

    public boolean isIgnoreErrors()
    {
        return (mMulti?((Boolean)mValues.get(IGNORE_ERRORS)).booleanValue():true);
    }

    public String getKeyPass()
    {
        return (String)mValues.get(KEY_PASS);
    }

    public String getKeyStore()
    {
        return (String)mValues.get(KEY_STORE);
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

    public boolean isVerbose()
    {
        return ((Boolean)mValues.get(VERBOSE)).booleanValue();
    }
    
    private class FileSelectionAdapter extends SelectionAdapter
    {
        private boolean mOpen;
        private String mDialogText;
        private String mDefaultFile;
        
        
        public FileSelectionAdapter(String file, String text, boolean open)
        {
            super();
            mDefaultFile = file;
            mDialogText = JARSignerPlugin.getResourceString(text);
            mOpen = open;
        }

        public void widgetSelected(SelectionEvent e) 
        {
            Button b = (Button)e.item;
            Text text = (Text)b.getData("text"); //$NON-NLS-1$
            if(text != null) {
                FileDialog dialog = new FileDialog(getShell(),(mOpen?SWT.OPEN:SWT.SAVE));
                dialog.setText(mDialogText); //$NON-NLS-1$
                String file = text.getText();
                dialog.setFileName(JARSignerPlugin.isEmpty(file)?mDefaultFile:file); //$NON-NLS-1$
                file = dialog.open();
                if(file != null) {
                    text.setText(file);
                }
            }
        }
    }
}
