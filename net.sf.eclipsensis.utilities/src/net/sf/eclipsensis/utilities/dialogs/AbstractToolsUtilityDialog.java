/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities.dialogs;

import java.beans.*;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.security.KeyStoreException;
import java.util.*;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import net.sf.eclipsensis.utilities.UtilitiesPlugin;
import net.sf.eclipsensis.utilities.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.launching.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Version;

public abstract class AbstractToolsUtilityDialog extends StatusDialog
{
    protected static final String ATTR_BUTTON = "button"; //$NON-NLS-1$
    protected static final String ATTR_LABEL = "label"; //$NON-NLS-1$
    protected static final String ATTR_TEXT = "text"; //$NON-NLS-1$

    protected static final String TOOLS_JAR_VERSION = "toolsjarversion"; //$NON-NLS-1$
    protected static final String TOOLS_JAR = "tools.jar"; //$NON-NLS-1$
    protected static final String VERBOSE = "verbose"; //$NON-NLS-1$
    protected static final String IGNORE_ERRORS = "ignore.errors"; //$NON-NLS-1$

    private static Pattern cCreatedByPattern = Pattern.compile("([1-9](\\.[0-9])*[_\\-0-9a-zA-Z]*)( \\(.*\\))*"); //$NON-NLS-1$

    private Map<String, Object> mValues;
    private IDialogSettings mDialogSettings;
    private IVMInstall mVMInstall = null;
    private String mToolsMainClassName = null;
    private List<?> mSelection = Collections.emptyList();

    protected final PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);
    private ComboViewer mVmInstalls;
    private Version mToolsJarVersion;

    /**
     * @param parentShell
     * @throws KeyStoreException
     */
    public AbstractToolsUtilityDialog(Shell parentShell, List<?> selection)
    {
        super(parentShell);
        mSelection = selection == null?Collections.emptyList():selection;
        IDialogSettings dialogSettings = getPlugin().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        mValues = new HashMap<String, Object>();
        mPropertyChangeSupport.addPropertyChangeListener(TOOLS_JAR, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                handleToolsJarChanged((String)evt.getOldValue(), (String)evt.getNewValue());
            }
        });
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
                if(Common.isValidFile(toolPath)) {
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

    protected final List<?> getSelection()
    {
        return mSelection;
    }

    protected final Map<String, Object> getValues()
    {
        return mValues;
    }

    protected final String getStringDialogSetting(String name)
    {
        String str = mDialogSettings.get(name);
        return str==null?"":str; //$NON-NLS-1$
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

    @Override
    protected final void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(getDialogTitle());
        newShell.setImage(UtilitiesPlugin.getDefault().getShellImage());
    }

    protected final void makeBold(Control c)
    {
        FontData[] fd = c.getFont().getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
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
            @Override
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
        b.setText(label);
        b.setSelection(((Boolean)mValues.get(property)).booleanValue());
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = layout.numColumns;
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            @Override
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
        gd.horizontalSpan = layout.numColumns - ( l == null?0:1);
        text.setLayoutData(gd);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                setValue(property, text.getText());
            }
        });

        text.setData(ATTR_LABEL, l);
        return text;
    }

    protected final Text makeBrowser(Composite composite, String label, final String property,
            SelectionListener listener, boolean isRequired)
    {
        return makeBrowser(composite, label, property, listener, isRequired, false);
    }

    protected final Text makeBrowser(Composite composite, String label, final String property,
            SelectionListener listener, boolean isRequired, boolean isReadOnly)
    {
        Label l = null;
        if(label != null) {
            l = makeLabel(composite, label, isRequired);
        }
        final Text text = new Text(composite,SWT.BORDER | (isReadOnly?SWT.READ_ONLY:SWT.NONE));
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
        text.setData(ATTR_LABEL,l);
        text.setData(ATTR_BUTTON,b);
        b.setData(ATTR_TEXT,text);
        return text;
    }

    protected void setEnabled(Control control, boolean enabled)
    {
        if(control != null && !control.isDisposed() && !control.isEnabled() == enabled)
        {
            control.setEnabled(enabled);
            Label l = (Label) control.getData(ATTR_LABEL);
            setEnabled(l, enabled);
            Button b = (Button) control.getData(ATTR_BUTTON);
            if(b != null)
            {
                setEnabled(b, enabled);
            }
            else
            {
                Text t = (Text) control.getData(ATTR_TEXT);
                setEnabled(t, enabled);
            }
        }
    }

    protected final Label makeLabel(Composite composite, String label, boolean isRequired)
    {
        if(label == null || label.length() == 0)
        {
            return null;
        }
        Label l = new Label(composite, SWT.NONE);
        if(isRequired) {
            makeBold(l);
        }
        l.setText(label);
        l.setLayoutData(new GridData());
        return l;
    }

    @Override
    protected final Control createDialogArea(Composite parent)
    {
        Composite parent2 = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent2.getLayout();

        Composite composite = new Composite(parent2,SWT.NONE);
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

        Group group = new Group(composite,SWT.NONE);
        group.setText("JRE Settings");
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = layout.numColumns;
        group.setLayoutData(gridData);
        layout = new GridLayout(3, false);
        //        layout.marginHeight = 0;
        //        layout.marginWidth = 0;
        group.setLayout(layout);
        applyDialogFont(group);
        makeBrowser(group,UtilitiesPlugin.getResourceString("tools.jar.location"), TOOLS_JAR,  //$NON-NLS-1$
                new FileSelectionAdapter(UtilitiesPlugin.getResourceString("tools.jar.location.message"), //$NON-NLS-1$
                        UtilitiesPlugin.getResourceString("tools.jar.name"),false), //$NON-NLS-1$
                        true);

        makeLabel(group, "JRE:", true);
        final Combo combo = new Combo(group,SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        combo.setLayoutData(gd);
        mVmInstalls = new ComboViewer(combo);
        mVmInstalls.setContentProvider(new ArrayContentProvider());
        mVmInstalls.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element)
            {
                if(element instanceof IVMInstall)
                {
                    return ((IVMInstall)element).getName();
                }
                return super.getText(element);
            }

        });
        mVmInstalls.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                mVMInstall = (IVMInstall) (event.getSelection().isEmpty()?null:((IStructuredSelection)event.getSelection()).getFirstElement());
            }
        });
        setVMInstall(mVMInstall);
        return parent2;
    }

    @Override
    public void create()
    {
        super.create();
        updateStatus();
    }

    private void setToolsJarVersion(Version version)
    {
        Version oldToolsJarVersion = mToolsJarVersion;
        mToolsJarVersion = version;
        mPropertyChangeSupport.firePropertyChange(TOOLS_JAR_VERSION, oldToolsJarVersion, mToolsJarVersion);
    }

    private void handleToolsJarChanged(String oldToolsJar, String newToolsJar)
    {
        if(!Common.stringsAreEqual(oldToolsJar, newToolsJar)) {
            JarFile jarfile = null;
            try
            {
                String toolsMainClassName = null;
                IVMInstall vmInstall = null;
                Version toolsJarVersion = null;
                if (!Common.isEmpty(newToolsJar))
                {
                    File f = new File(newToolsJar);
                    if (Common.isValidFile(f))
                    {
                        try
                        {
                            jarfile = new JarFile(f);
                            String createdBy = jarfile.getManifest().getMainAttributes().getValue("Created-By"); //$NON-NLS-1$
                            Matcher matcher = cCreatedByPattern.matcher(createdBy);
                            if (matcher.matches())
                            {
                                toolsJarVersion = Common.parseVersion(matcher.group(1));
                            }
                        }
                        catch (Exception ex)
                        {
                            toolsJarVersion = new Version(0, 0, 0);
                        }

                        if (toolsJarVersion != null)
                        {
                            vmInstall = Common.getVMInstall(getMinJDKVersion(), toolsJarVersion);
                            if (vmInstall == null)
                            {
                                MessageDialog.openError(getShell(), UtilitiesPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                        UtilitiesPlugin.getResourceString("mismatched.tools.jar.vm.version"));
                            }
                        }
                    }

                    if (toolsJarVersion != null && jarfile != null)
                    {
                        toolsMainClassName = getToolsMainClassName(toolsJarVersion);
                        ZipEntry entry = jarfile.getEntry(toolsMainClassName.replace('.', '/') + ".class"); //$NON-NLS-1$
                        if (entry == null)
                        {
                            vmInstall = null;
                            toolsMainClassName = null;
                        }
                    }
                }
                setVMInstall(vmInstall);
                setToolsMainClassName(toolsMainClassName);
                setToolsJarVersion(toolsJarVersion);
            }
            finally
            {
                if(jarfile != null) {
                    try {
                        jarfile.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                jarfile = null;

            }
        }
    }

    @Override
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
        return mSelection.size() > 1?((Boolean)mValues.get(IGNORE_ERRORS)).booleanValue():true;
    }

    public final boolean isVerbose()
    {
        return ((Boolean)mValues.get(VERBOSE)).booleanValue();
    }

    private final void updateStatus()
    {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed())
        {
            updateStatus(validate());
        }
    }

    protected final void setValue(String name, Object value)
    {
        Object oldValue = mValues.put(name,value);
        mPropertyChangeSupport.firePropertyChange(name, oldValue, value);
        updateStatus();
    }

    private void setVMInstall(IVMInstall vmInstall)
    {
        if (mVmInstalls != null && !mVmInstalls.getCombo().isDisposed())
        {
            mVmInstalls.setInput(Common.getVMInstalls(vmInstall==null?getMinJDKVersion():
                Common.parseVersion(((IVMInstall2)vmInstall).getJavaVersion())));
            StructuredSelection selection = vmInstall != null?new StructuredSelection(vmInstall):StructuredSelection.EMPTY;
            mVmInstalls.setSelection(selection);
        }
        else
        {
            mVMInstall = vmInstall;
        }
    }

    public final IVMInstall getVMInstall()
    {
        return mVMInstall;
    }

    public final void setToolsMainClassName(String className)
    {
        mToolsMainClassName = className;
    }

    public final String getToolsMainClassName()
    {
        return mToolsMainClassName;
    }

    public Version getToolsJarVersion()
    {
        return mToolsJarVersion;
    }

    protected IStatus validate()
    {
        if( mVMInstall != null) {
            String toolsJar = getToolsJar();
            File f= new File(toolsJar);
            if(Common.isValidFile(f)) {
                return Status.OK_STATUS;
            }
        }
        return createStatus(IStatus.ERROR, "No JRE has been selected.");
    }

    protected final IStatus createStatus(int severity, String message)
    {
        return new Status(severity, getPlugin().getBundle().getSymbolicName(), message);
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

        @Override
        public void widgetSelected(SelectionEvent e)
        {
            Button b = (Button)e.widget;
            if(b == null) {
                b = (Button)e.item;
            }
            Text text = (Text)b.getData(ATTR_TEXT);
            if(text != null) {
                FileDialog dialog = new FileDialog(getShell(),(mOpen?SWT.OPEN:SWT.SAVE));
                dialog.setText(mDialogText);
                String file = text.getText();
                dialog.setFileName(Common.isEmpty(file)?mDefaultFile:file);
                file = dialog.open();
                if(file != null) {
                    text.setText(file);
                }
            }
        }
    }
}
