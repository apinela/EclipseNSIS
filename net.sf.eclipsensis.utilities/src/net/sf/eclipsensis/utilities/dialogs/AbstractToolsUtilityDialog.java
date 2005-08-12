/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities.dialogs;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.util.*;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import net.sf.eclipsensis.utilities.UtilitiesPlugin;
import net.sf.eclipsensis.utilities.util.Common;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Version;

public abstract class AbstractToolsUtilityDialog extends Dialog
{
    protected static final String ATTR_BUTTON = "button";
    protected static final String ATTR_LABEL = "label";
    protected static final String ATTR_TEXT = "text";
    
    protected static final String TOOLS_JAR = "tools.jar"; //$NON-NLS-1$
    protected static final String VERBOSE = "verbose"; //$NON-NLS-1$
    protected static final String IGNORE_ERRORS = "ignore.errors"; //$NON-NLS-1$

    private static Pattern cCreatedByPattern = Pattern.compile("([1-9](\\.[0-9])*[_\\-0-9a-zA-Z]*)( \\(.*\\))*");

    private Map mValues;
    private IDialogSettings mDialogSettings;
    private IVMInstall mVMInstall = null;
    private String mToolsMainClassName = null;
    private List mSelection = Collections.EMPTY_LIST;
    
    /**
     * @param parentShell
     * @throws KeyStoreException 
     */
    public AbstractToolsUtilityDialog(Shell parentShell, List selection)
    {
        super(parentShell);
        mSelection = (selection == null?Collections.EMPTY_LIST:selection);
        IDialogSettings dialogSettings = getPlugin().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        mValues = new HashMap();
        init();
    }
    
    protected void init()
    {
        String toolsJar = getStringDialogSetting(TOOLS_JAR);
        if(Common.isEmpty(toolsJar)) {
            IVMInstall vm = Common.getVMInstall(getMinJDKVersion());
            if(vm == null) {
                throw new RuntimeException(UtilitiesPlugin.getFormattedString("vm.not.found", new Object[]{getMinJDKVersion()})); //$NON-NLS-1$
            }
            else {
                File jdkHome = vm.getInstallLocation();
                File toolPath = new File(jdkHome,"lib/tools.jar"); //$NON-NLS-1$
                if(toolPath.exists() && toolPath.isFile()) {
                    toolsJar = toolPath.getAbsolutePath();
                }
            }
        }
        setValue(TOOLS_JAR, toolsJar);
        if(mSelection.size() > 1) {
            setValue(IGNORE_ERRORS,mDialogSettings.getBoolean(IGNORE_ERRORS)?Boolean.TRUE:Boolean.FALSE);
        }
        setValue(VERBOSE,mDialogSettings.getBoolean(VERBOSE)?Boolean.TRUE:Boolean.FALSE);
    }

    protected final IDialogSettings getDialogSettings()
    {
        return mDialogSettings;
    }

    protected final List getSelection()
    {
        return mSelection;
    }

    protected final Map getValues()
    {
        return mValues;
    }

    protected final String getStringDialogSetting(String name)
    {
        String str = mDialogSettings.get(name);
        return (str==null?"":str); //$NON-NLS-1$
    }
    
    protected final Integer getIntDialogSetting(String name)
    {
        try {
            return new Integer(mDialogSettings.getInt(name));
        }
        catch(NumberFormatException nfe) {
            return Common.ZERO;
        }
    }

    protected final void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(getDialogTitle()); //$NON-NLS-1$
        newShell.setImage(UtilitiesPlugin.getDefault().getShellImage());
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
    
    protected final Button makeRadio(Composite parent, String label, final String property, boolean isRequired,
                                     final Object data)
    {
        GridLayout layout = (GridLayout)parent.getLayout();
        final Button b = new Button(parent,SWT.RADIO|SWT.RIGHT);
        if(isRequired) {
            makeBold(b);
        }
        GridData gd = new GridData();
        gd.horizontalSpan = layout.numColumns;
        b.setLayoutData(gd);
        b.setText(label);
        b.setSelection(data.equals(getValues().get(property)));
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                setValue(property, data);
            }
        });
        
        return b;
    }

    protected final Button makeCheckBox(Composite composite, String label, final String property, boolean isRequired)
    {
        GridLayout layout = (GridLayout)composite.getLayout();
        final Button b = new Button(composite, SWT.CHECK);
        if(isRequired) {
            makeBold(b);
        }
        b.setText(label); //$NON-NLS-1$
        b.setSelection(((Boolean)mValues.get(property)).booleanValue());
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = layout.numColumns;
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                setValue(property,b.getSelection()?Boolean.TRUE:Boolean.FALSE);
            }
        });
        return b;
    }
    
    protected final Text makeText(Composite composite, String label, final String property, boolean isRequired)
    {
        GridLayout layout = (GridLayout)composite.getLayout();
        Label l = makeLabel(composite, label, isRequired);
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = layout.numColumns - 1;
        text.setLayoutData(gd);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                setValue(property, text.getText());
            }
        });
        
        text.setData(ATTR_LABEL,l); //$NON-NLS-1$
        return text;
    }

    protected final Text makeFileBrowser(Composite composite, String label, final String property,
                                 SelectionListener listener, boolean isRequired)
    {
        Label l = null;
        if(label != null) {
            l = makeLabel(composite, label, isRequired);
        }
        final Text text = new Text(composite,SWT.BORDER);
        text.setText((String)mValues.get(property));
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                setValue(property, text.getText());
            }
        });
        Button b = new Button(composite,SWT.PUSH);
        b.setLayoutData(new GridData());
        b.setText(UtilitiesPlugin.getResourceString("browse.label")); //$NON-NLS-1$
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
        l.setText(label); //$NON-NLS-1$
        l.setLayoutData(new GridData());
        return l;
    }

    protected final Control createDialogArea(Composite parent)
    {
        parent = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent.getLayout();
        
        Composite composite = new Composite(parent,SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = layout.numColumns;
        composite.setLayoutData(gridData);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);
        
        createValuesDialogArea(composite);

        if(mSelection.size() > 1) {
            makeCheckBox(composite,UtilitiesPlugin.getResourceString(IGNORE_ERRORS+".label"),IGNORE_ERRORS,false); //$NON-NLS-1$
        }
        makeCheckBox(composite,UtilitiesPlugin.getResourceString(VERBOSE+".label"),VERBOSE,false); //$NON-NLS-1$
        createFlagsDialogArea(composite);
        
        Composite composite2 = new Composite(composite,SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = layout.numColumns;
        composite2.setLayoutData(gridData);
        layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        applyDialogFont(composite2);
        makeFileBrowser(composite2,UtilitiesPlugin.getResourceString("tools.jar.location"), TOOLS_JAR,  //$NON-NLS-1$
                new FileSelectionAdapter(UtilitiesPlugin.getResourceString("tools.jar.location.message"), //$NON-NLS-1$
                        UtilitiesPlugin.getResourceString("tools.jar.name"),false), //$NON-NLS-1$ 
                true); //$NON-NLS-1$

        return parent;
    }
    
    public void create()
    {
        super.create();
        updateButtons();
    }
    
    protected void handleToolsJarChanged(String oldToolsJar, String newToolsJar)
    {
        if(!Common.stringsAreEqual(oldToolsJar, newToolsJar)) {
            String toolsMainClassName = null;
            IVMInstall vmInstall = null;
            if(!Common.isEmpty(newToolsJar)) {
                File f = new File(newToolsJar);
                JarFile jarfile = null;
                if(f.exists() && f.isFile()) {
                    Version toolsJarVersion = null;
                    try {
                        jarfile = new JarFile(f);
                        String createdBy = jarfile.getManifest().getMainAttributes().getValue("Created-By");
                        Matcher matcher = cCreatedByPattern.matcher(createdBy);
                        if(matcher.matches()) {
                            toolsJarVersion = Common.parseVersion(matcher.group(1));
                        }
                    }
                    catch(Exception ex) {
                        if(jarfile != null) {
                            try {
                                jarfile.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        jarfile = null;
                        toolsJarVersion = new Version(0,0,0);
                    }
                    
                    if(toolsJarVersion == null) {
                        return;
                    }
                    vmInstall = Common.getVMInstall(getMinJDKVersion(), toolsJarVersion);
                    if(vmInstall == null) {
                        MessageDialog.openError(getShell(),UtilitiesPlugin.getResourceString("error.title"),
                                UtilitiesPlugin.getResourceString("mismatched.tools.jar.vm.version"));
                    }
                }
                
                if(vmInstall != null) {
                    toolsMainClassName = getToolsMainClassName(Common.parseVersion(((IVMInstall2)vmInstall).getJavaVersion()));
                    ZipEntry entry = jarfile.getEntry(toolsMainClassName.replace('.','/')+".class");
                    if(entry == null) {
                        vmInstall = null;
                        toolsMainClassName = null;
                    }
                }
            }
            setVMInstall(vmInstall);
            setToolsMainClassName(toolsMainClassName);
        }
    }
    
    protected void okPressed()
    {
        mDialogSettings.put(TOOLS_JAR,getToolsJar());
        mDialogSettings.put(VERBOSE,isVerbose());
        if(mSelection.size() > 1) {
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
        return (mSelection.size() > 1?((Boolean)mValues.get(IGNORE_ERRORS)).booleanValue():true);
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
    
    protected void setValue(String name, Object value)
    {
        Object oldValue = mValues.put(name,value);
        if(!Common.objectsAreEqual(oldValue, value)) {
            valueChanged(name, oldValue, value);
        }
        updateButtons();
    }

    protected void valueChanged(String name, Object oldValue, Object newValue)
    {
        if(name.equals(TOOLS_JAR)) {
            handleToolsJarChanged((String)oldValue, (String)newValue);
        }
    }

    protected void setVMInstall(IVMInstall vmInstall)
    {
        mVMInstall = vmInstall;
    }

    public IVMInstall getVMInstall()
    {
        return mVMInstall;
    }
    
    protected void setToolsMainClassName(String className)
    {
        mToolsMainClassName = className;
    }

    public String getToolsMainClassName()
    {
        return mToolsMainClassName;
    }

    protected boolean isValid()
    {
        if( mVMInstall != null) {
            String toolsJar = getToolsJar();
            File f= new File(toolsJar);
            if(f.exists() && f.isFile()) {
                return true;
            }
        }
        return false;
    }
    
    protected abstract String getDialogTitle();
    protected abstract AbstractUIPlugin getPlugin();
    protected abstract Version getMinJDKVersion();
    protected abstract void createValuesDialogArea(Composite parent);
    protected abstract void createFlagsDialogArea(Composite parent);
    protected abstract String getToolsMainClassName(Version toolsJarVersion);
    
    protected class FileSelectionAdapter extends SelectionAdapter
    {
        private boolean mOpen;
        private String mDialogText;
        private String mDefaultFile;
        
        
        public FileSelectionAdapter(String text, String file, boolean open)
        {
            super();
            mDefaultFile = file;
            mDialogText = text;
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
                dialog.setFileName(Common.isEmpty(file)?mDefaultFile:file); //$NON-NLS-1$
                file = dialog.open();
                if(file != null) {
                    text.setText(file);
                }
            }
        }
    }
}
