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

import java.beans.*;
import java.io.File;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.osgi.framework.Version;

public class JARSignerOptionsDialog extends AbstractJAROptionsDialog
{
    /**
     *
     */
    private static final char PATH_SEPARATOR = System.getProperty("path.separator").charAt(0); //$NON-NLS-1$

    private static final Version JAVA_5_VERSION = new Version(1,5,0);

    private static final String STORE_PASS = "store.pass"; //$NON-NLS-1$
    private static final String ALIAS = "alias"; //$NON-NLS-1$
    private static final String STORE_TYPE = "store.type"; //$NON-NLS-1$
    private static final String KEY_PASS = "key.pass"; //$NON-NLS-1$
    private static final String SIG_FILE = "sig.file"; //$NON-NLS-1$
    private static final String SIGNED_JAR = "signed.jar"; //$NON-NLS-1$
    private static final String INTERNAL_SF = "internal.sf"; //$NON-NLS-1$
    private static final String SECTIONS_ONLY = "sections.only"; //$NON-NLS-1$

    private static final String SUPPORTS_TIMESTAMPING = "supports.timestamping"; //$NON-NLS-1$
    private static final String USE_TIMESTAMPING = "use.timestamping"; //$NON-NLS-1$
    private static final String TSA_CERT_OPTION = "tsa.cert.option"; //$NON-NLS-1$
    private static final String TSA = "tsa"; //$NON-NLS-1$
    private static final String TSA_CERT = "tsa.cert"; //$NON-NLS-1$

    private static final String SUPPORTS_ALT_SIGNING = "supports.alt.signing"; //$NON-NLS-1$
    private static final String USE_ALT_SIGNING = "use.alt.signing"; //$NON-NLS-1$
    private static final String ALT_SIGNER = "alt.signer"; //$NON-NLS-1$
    private static final String ALT_SIGNER_PATH = "alt.signer.path"; //$NON-NLS-1$

    private List<String> mAliases;
    private ComboViewer mAliasesComboViewer;
    private ComboViewer mTSACertAliasesComboViewer;

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

        setValue(USE_TIMESTAMPING,getDialogSettings().getBoolean(USE_TIMESTAMPING)?Boolean.TRUE:Boolean.FALSE);
        setValue(TSA_CERT_OPTION,getDialogSettings().getBoolean(TSA_CERT_OPTION)?Boolean.TRUE:Boolean.FALSE);
        setValue(TSA,getStringDialogSetting(TSA));
        String alias = getStringDialogSetting(TSA_CERT);
        if(mAliases.contains(alias)) {
            setValue(TSA_CERT,alias);
        }
        else {
            if(mAliases.size() > 0) {
                setValue(TSA_CERT,mAliases.get(0));
            }
            else {
                setValue(TSA_CERT,""); //$NON-NLS-1$
            }
        }

        setValue(USE_ALT_SIGNING,getDialogSettings().getBoolean(USE_ALT_SIGNING)?Boolean.TRUE:Boolean.FALSE);
        setValue(ALT_SIGNER,getStringDialogSetting(ALT_SIGNER));
        setValue(ALT_SIGNER_PATH,getStringDialogSetting(ALT_SIGNER_PATH));
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
                        mAliasesComboViewer.refresh(true);
                        if(!mAliases.contains(alias)) {
                            if(mAliases.size() > 0) {
                                alias = mAliases.get(0);
                            }
                            else {
                                alias = ""; //$NON-NLS-1$
                            }
                        }
                        setValue(ALIAS,alias);
                        mAliasesComboViewer.setSelection(new StructuredSelection(alias));

                        mTSACertAliasesComboViewer.refresh(true);
                        if(!mAliases.contains(alias)) {
                            if(mAliases.size() > 0) {
                                alias = mAliases.get(0);
                            }
                            else {
                                alias = ""; //$NON-NLS-1$
                            }
                        }
                        setValue(ALIAS,alias);
                        mAliasesComboViewer.setSelection(new StructuredSelection(alias));
                    }
                }
            }
        };
        Text t = makeBrowser(composite,JARSignerPlugin.getResourceString("key.store.location"), KEY_STORE, sa, true); //$NON-NLS-1$
        t.setEditable(false);
        gd = (GridData)t.getLayoutData();
        gd.widthHint = convertWidthInCharsToPixels(50);

        makeLabel(composite, JARSignerPlugin.getResourceString(ALIAS), true);

        mAliasesComboViewer = makeAliasesComboViewer(composite,ALIAS);
        ((GridData)mAliasesComboViewer.getCombo().getLayoutData()).horizontalSpan = 2;

        makeText(composite,JARSignerPlugin.getResourceString(STORE_TYPE),STORE_TYPE,false);
        makeText(composite,JARSignerPlugin.getResourceString(KEY_PASS),KEY_PASS,false);
        makeText(composite,JARSignerPlugin.getResourceString(SIG_FILE),SIG_FILE,false);

        if(getSelection().size() <= 1) {
            makeBrowser(composite,JARSignerPlugin.getResourceString("signed.jar.location"), SIGNED_JAR,  //$NON-NLS-1$
                    new FileSelectionAdapter("signed.jar.location.message","",false), //$NON-NLS-1$ //$NON-NLS-2$
                    false);
        }

    }

    private ComboViewer makeAliasesComboViewer(Composite composite, final String property)
    {
        final Combo combo = new Combo(composite,SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = ((GridLayout)composite.getLayout()).numColumns;
        combo.setLayoutData(gd);
        ComboViewer comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        comboViewer.setLabelProvider(new LabelProvider());
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                setValue(property,combo.getText());
            }
        });
        comboViewer.setInput(mAliases);
        comboViewer.setSelection(new StructuredSelection(getValues().get(property)));
        return comboViewer;
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

        final Group tsGroup = new Group(composite,SWT.NONE);
        tsGroup.setText(JARSignerPlugin.getResourceString("time.stamping.label")); //$NON-NLS-1$
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tsGroup.setLayoutData(gd);
        layout = new GridLayout(2,false);
        tsGroup.setLayout(layout);

        final Button useTimeStamping = makeCheckBox(tsGroup, JARSignerPlugin.getResourceString("time.stamp.signature.label"), USE_TIMESTAMPING, false); //$NON-NLS-1$

        final Button tsaOption = makeRadio(tsGroup, JARSignerPlugin.getResourceString("tsa.url.label"), TSA_CERT_OPTION, true, Boolean.FALSE); //$NON-NLS-1$
        ((GridData)tsaOption.getLayoutData()).horizontalSpan = 1;

        final Text tsa = makeText(tsGroup, "", TSA, false); //$NON-NLS-1$
        ((GridData)tsa.getLayoutData()).horizontalSpan = 1;

        final Button tsaCertOption = makeRadio(tsGroup, JARSignerPlugin.getResourceString("tsa.cert.label"), TSA_CERT_OPTION, true, Boolean.TRUE); //$NON-NLS-1$
        ((GridData)tsaCertOption.getLayoutData()).horizontalSpan = 1;

        mTSACertAliasesComboViewer = makeAliasesComboViewer(tsGroup, TSA_CERT);
        ((GridData)mTSACertAliasesComboViewer.getCombo().getLayoutData()).horizontalSpan = 1;

        tsaOption.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean enabled = tsaOption.getSelection();
                setEnabled(tsa, enabled);
                setEnabled(mTSACertAliasesComboViewer.getCombo(), !enabled);
            }
        });

        tsaCertOption.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean enabled = tsaCertOption.getSelection();
                setEnabled(tsa, !enabled);
                setEnabled(mTSACertAliasesComboViewer.getCombo(), enabled);
            }
        });


        useTimeStamping.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean enabled = useTimeStamping.getSelection();
                setEnabled(tsaOption, enabled);
                setEnabled(tsa, enabled && tsaOption.getSelection());
                setEnabled(tsaCertOption, enabled);
                setEnabled(mTSACertAliasesComboViewer.getCombo(), enabled && tsaCertOption.getSelection());
            }
        });


        final Group asGroup = new Group(composite,SWT.NONE);
        asGroup.setText(JARSignerPlugin.getResourceString("alt.signing.method.label")); //$NON-NLS-1$
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        asGroup.setLayoutData(gd);
        layout = new GridLayout(3,false);
        asGroup.setLayout(layout);

        final Button useAltSigning = makeCheckBox(asGroup, JARSignerPlugin.getResourceString("use.alt.signing.label"), USE_ALT_SIGNING, false); //$NON-NLS-1$

        final Text altSigner = makeText(asGroup, JARSignerPlugin.getResourceString("alt.signing.class.label"), ALT_SIGNER, true); //$NON-NLS-1$
        final Text altSignerPath = makeBrowser(asGroup, JARSignerPlugin.getResourceString("alt.signing.classpath.label"), ALT_SIGNER_PATH, new SelectionAdapter() { //$NON-NLS-1$
            public void widgetSelected(SelectionEvent e)
            {
                Button b = (Button)e.widget;
                if(b == null) {
                    b = (Button)e.item;
                }
                Text text = (Text)b.getData(ATTR_TEXT);
                if(text != null) {
                    List<String> classpath = Common.tokenize(getAltSignerPath(), PATH_SEPARATOR);
                    ClasspathDialog dialog = new ClasspathDialog(getShell(), classpath);
                    if(dialog.open() == Window.OK)
                    {
                        text.setText(Common.flatten(dialog.getClasspath(), PATH_SEPARATOR));
                    }
                }
            }
        }, false, true);

        useAltSigning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean enabled = useAltSigning.getSelection();
                setEnabled(altSigner, enabled);
                setEnabled(altSignerPath, enabled);
            }
        });

        final Runnable runnable = new Runnable() {

            public void run()
            {
                Version version = getToolsJarVersion();
                boolean java5 = false;
                if(version != null)
                {
                    java5 = version.compareTo(JAVA_5_VERSION) >= 0;
                }
                setValue(SUPPORTS_TIMESTAMPING, java5);
                setValue(SUPPORTS_ALT_SIGNING, java5);
                if(java5)
                {
                    tsGroup.setEnabled(true);
                    setEnabled(useTimeStamping, true);
                    setEnabled(tsaOption, useTimeStamping.getSelection());
                    setEnabled(tsa, useTimeStamping.getSelection() && tsaOption.getSelection());
                    setEnabled(tsaCertOption, useTimeStamping.getSelection());
                    setEnabled(mTSACertAliasesComboViewer.getCombo(), useTimeStamping.getSelection() && tsaCertOption.getSelection());

                    asGroup.setEnabled(true);
                    setEnabled(useAltSigning, true);
                    setEnabled(altSigner, useAltSigning.getSelection());
                    setEnabled(altSignerPath, useAltSigning.getSelection());
                }
                else
                {
                    setEnabled(useTimeStamping, false);
                    setEnabled(tsaOption, false);
                    setEnabled(tsa, false);
                    setEnabled(tsaCertOption, false);
                    setEnabled(mTSACertAliasesComboViewer.getCombo(), false);
                    tsGroup.setEnabled(false);

                    setEnabled(useAltSigning, false);
                    setEnabled(altSigner, false);
                    setEnabled(altSignerPath, false);
                    asGroup.setEnabled(false);
                }
            }
        };
        runnable.run();

        mPropertyChangeSupport.addPropertyChangeListener(TOOLS_JAR_VERSION, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt)
            {
                runnable.run();
            }
        });

        applyDialogFont(tsGroup);
    }

    @Override
    protected IStatus validate()
    {
        IStatus status = super.validate();
        if(status.isOK()) {
            if(Common.isEmpty(getAlias())) {
                status = createStatus(IStatus.ERROR, JARSignerPlugin.getResourceString("missing.jar.signing.cert.alias")); //$NON-NLS-1$
            }
            if(status.isOK()) {
                String signedJar = getSignedJar();
                if(!Common.isEmpty(signedJar)) {
                    File file = new File(signedJar);
                    if(Common.isValidDirectory(file))
                    {
                        status = createStatus(IStatus.ERROR, String.format(JARSignerPlugin.getResourceString("signed.jar.name.error"),file.getAbsolutePath())); //$NON-NLS-1$
                    }
                }
            }

            if (status.isOK())
            {
                if (isSupportsTimestamping())
                {
                    if (isUseTimestamping())
                    {
                        if (isTSACertOption())
                        {
                            if(Common.isEmpty(getTSACert())) {
                                status = createStatus(IStatus.ERROR, JARSignerPlugin.getResourceString("missing.tsa.cert.alias")); //$NON-NLS-1$
                            }
                        }
                        else
                        {
                            String tsa = getTSA();
                            if (!Common.isEmpty(tsa))
                            {
                                try
                                {
                                    new URI(tsa);
                                }
                                catch (URISyntaxException e)
                                {
                                    status = createStatus(IStatus.ERROR, JARSignerPlugin.getResourceString("invalid.tsa.url")); //$NON-NLS-1$
                                }

                            }
                            else {
                                status = createStatus(IStatus.ERROR, JARSignerPlugin.getResourceString("missing.tsa.url")); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }

            if (status.isOK())
            {
                if (isSupportsAltSigning())
                {
                    if (isUseAltSigning())
                    {
                        String altSigner = getAltSigner();
                        if (!Common.isEmpty(altSigner))
                        {
                            List<String> parts = Common.tokenize(altSigner, '.');
                            for (String part : parts)
                            {
                                if (!isJavaIdentifier(part))
                                {
                                    status = createStatus(IStatus.ERROR, String.format(JARSignerPlugin.getResourceString("alt.signer.class.name.invalid"),altSigner)); //$NON-NLS-1$
                                    break;
                                }
                            }
                        }
                        else
                        {
                            status = createStatus(IStatus.ERROR, String.format(JARSignerPlugin.getResourceString("missing.alt.signer.class.name"),altSigner)); //$NON-NLS-1$
                        }

                        if (status.isOK())
                        {
                            String altSignerPath = getAltSignerPath();
                            if (!Common.isEmpty(altSignerPath))
                            {
                                List<String> parts = Common.tokenize(altSignerPath, PATH_SEPARATOR);
                                for (String string : parts)
                                {
                                    if (!new File(string).exists())
                                    {
                                        status = createStatus(IStatus.ERROR, String.format(JARSignerPlugin.getResourceString("invalid.alt.signer.classpath.element"),string)); //$NON-NLS-1$
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return status;
    }

    private boolean isJavaIdentifier(String name)
    {
        char[] chars = name.toCharArray();
        if(chars.length > 0) {
            if(Character.isJavaIdentifierStart(chars[0]))
            {
                for (int i = 1; i < chars.length; i++)
                {
                    if(!Character.isJavaIdentifierPart(chars[i]))
                    {
                        return false;
                    }
                }
                return true;
            }
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

        getDialogSettings().put(USE_TIMESTAMPING, isUseTimestamping());
        getDialogSettings().put(TSA_CERT_OPTION, isTSACertOption());
        getDialogSettings().put(TSA, getTSA());
        getDialogSettings().put(TSA_CERT, getTSACert());

        getDialogSettings().put(USE_ALT_SIGNING, isUseAltSigning());
        getDialogSettings().put(ALT_SIGNER, getAltSigner());
        getDialogSettings().put(ALT_SIGNER_PATH, getAltSignerPath());

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

    public boolean isUseTimestamping()
    {
        return (Boolean)getValues().get(USE_TIMESTAMPING);
    }

    public boolean isTSACertOption()
    {
        return (Boolean)getValues().get(TSA_CERT_OPTION);
    }

    public String getTSA()
    {
        return (String)getValues().get(TSA);
    }

    public String getTSACert()
    {
        return (String)getValues().get(TSA_CERT);
    }

    public boolean isUseAltSigning()
    {
        return (Boolean)getValues().get(USE_ALT_SIGNING);
    }

    public String getAltSigner()
    {
        return (String)getValues().get(ALT_SIGNER);
    }

    public String getAltSignerPath()
    {
        return (String)getValues().get(ALT_SIGNER_PATH);
    }

    public boolean isSupportsTimestamping()
    {
        return (Boolean)getValues().get(SUPPORTS_TIMESTAMPING);
    }

    public boolean isSupportsAltSigning()
    {
        return (Boolean)getValues().get(SUPPORTS_ALT_SIGNING);
    }
}
