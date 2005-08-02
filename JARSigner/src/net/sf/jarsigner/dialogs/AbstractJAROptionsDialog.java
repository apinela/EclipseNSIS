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
import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public abstract class AbstractJAROptionsDialog extends Dialog
{
    protected static final String ATTR_BUTTON = "button";
    protected static final String ATTR_LABEL = "label";
    protected static final String ATTR_TEXT = "text";
    
    protected static final String TOOLS_JAR = "tools.jar"; //$NON-NLS-1$
    protected static final String KEY_STORE = "key.store"; //$NON-NLS-1$
    protected static final String VERBOSE = "verbose"; //$NON-NLS-1$
    protected static final String IGNORE_ERRORS = "ignore.errors"; //$NON-NLS-1$

    protected Map mValues;
    protected IDialogSettings mDialogSettings;
    protected boolean mMulti;
    
    /**
     * @param parentShell
     * @throws KeyStoreException 
     */
    public AbstractJAROptionsDialog(Shell parentShell, boolean multi) throws KeyStoreException
    {
        super(parentShell);
        mMulti = multi;
        init();
    }

    protected void init()
    {
        mValues = new HashMap();
        IDialogSettings dialogSettings = JARSignerPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        String toolsJar = getStringDialogSetting(TOOLS_JAR);
        if(JARSignerPlugin.isEmpty(toolsJar)) {
            IVMInstall vm = JARSignerPlugin.getDefault().getVMInstall();
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
            mValues.put(KEY_STORE,getDefaultKeyStore());
        }
        else {
            mValues.put(KEY_STORE,keyStore);
        }
        if(mMulti) {
            mValues.put(IGNORE_ERRORS,mDialogSettings.getBoolean(IGNORE_ERRORS)?Boolean.TRUE:Boolean.FALSE);
        }
        mValues.put(VERBOSE,mDialogSettings.getBoolean(VERBOSE)?Boolean.TRUE:Boolean.FALSE);
    }

    protected String getDefaultKeyStore()
    {
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        File storePath = new File(userHome,".keystore"); //$NON-NLS-1$
        if(storePath.exists() && storePath.isFile()) {
            return storePath.getAbsolutePath();
        }
        return "";
    }

    protected final String getStringDialogSetting(String name)
    {
        String str = mDialogSettings.get(name);
        return (str==null?"":str); //$NON-NLS-1$
    }

    protected final void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(getDialogTitle()); //$NON-NLS-1$
    }
    
    protected final void makeBold(Control c)
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
    
    protected final Button makeCheckBox(Composite composite, String label, final String property, boolean isRequired)
    {
        final Button b = new Button(composite, SWT.CHECK);
        if(isRequired) {
            makeBold(b);
        }
        b.setText(JARSignerPlugin.getResourceString(label)); //$NON-NLS-1$
        b.setSelection(((Boolean)mValues.get(property)).booleanValue());
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 3;
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mValues.put(property,b.getSelection()?Boolean.TRUE:Boolean.FALSE);
                updateButtons();
            }
        });
        return b;
    }
    
    protected final Text makeText(Composite composite, String label, final String property, boolean isRequired)
    {
        Label l = makeLabel(composite, label, isRequired);
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mValues.put(property,text.getText());
                updateButtons();
            }
        });
        
        text.setData(ATTR_LABEL,l); //$NON-NLS-1$
        return text;
    }

    protected final Text makeFileBrowser(Composite composite, String label, final String property,
                                 SelectionListener listener, boolean isRequired)
    {
        Label l = makeLabel(composite, label, isRequired);
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mValues.put(property,text.getText());
                updateButtons();
            }
        });
        Button b = new Button(composite,SWT.PUSH);
        b.setLayoutData(new GridData());
        b.setText(JARSignerPlugin.getResourceString("browse.label")); //$NON-NLS-1$
        if(listener != null) {
            b.addSelectionListener(listener);
        }
        text.setData(ATTR_LABEL,l); //$NON-NLS-1$
        text.setData(ATTR_BUTTON,b); //$NON-NLS-1$
        b.setData(ATTR_TEXT,text); //$NON-NLS-1$
        return text;
    }

    protected final Label makeLabel(Composite composite, String label, boolean isRequired)
    {
        Label l = new Label(composite, SWT.NONE);
        if(isRequired) {
            makeBold(l);
        }
        l.setText(JARSignerPlugin.getResourceString(label)); //$NON-NLS-1$
        l.setLayoutData(new GridData());
        return l;
    }
    
    public final void create()
    {
        super.create();
        updateButtons();
    }
    
    protected void okPressed()
    {
        mDialogSettings.put(TOOLS_JAR,getToolsJar());
        mDialogSettings.put(KEY_STORE,getKeyStore());
        mDialogSettings.put(VERBOSE,isVerbose());
        if(mMulti) {
            mDialogSettings.put(IGNORE_ERRORS,isIgnoreErrors());
        }
        super.okPressed();
    }

    public final String getToolsJar()
    {
        return (String)mValues.get(TOOLS_JAR);
    }

    public final boolean isIgnoreErrors()
    {
        return (mMulti?((Boolean)mValues.get(IGNORE_ERRORS)).booleanValue():true);
    }

    public final String getKeyStore()
    {
        return (String)mValues.get(KEY_STORE);
    }

    public final boolean isVerbose()
    {
        return ((Boolean)mValues.get(VERBOSE)).booleanValue();
    }

    protected final void updateButtons()
    {
        Button button = getButton(IDialogConstants.OK_ID);
        if(button != null) {
            button.setEnabled(isValid());
        }
    }

    protected abstract String getDialogTitle();

    protected abstract boolean isValid();
    
    protected class FileSelectionAdapter extends SelectionAdapter
    {
        private boolean mOpen;
        private String mDialogText;
        private String mDefaultFile;
        
        
        public FileSelectionAdapter(String text, String file, boolean open)
        {
            super();
            mDefaultFile = file;
            mDialogText = JARSignerPlugin.getResourceString(text);
            mOpen = open;
        }

        public void widgetSelected(SelectionEvent e) 
        {
            Button b = (Button)e.widget;
            if(b == null) {
                b = (Button)e.item;
            }
            Text text = (Text)b.getData(ATTR_TEXT); //$NON-NLS-1$
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
