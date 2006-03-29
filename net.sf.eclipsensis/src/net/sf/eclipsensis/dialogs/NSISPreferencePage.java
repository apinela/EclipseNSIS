/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NSISPreferencePage	extends NSISSettingsPage implements INSISPreferenceConstants
{
    public static final List NSIS_HOMES;

    private static final List cInternalNSISHomes;
    private static File cNSISHomesListFile = new File(EclipseNSISPlugin.getPluginStateLocation(),
                                                NSISPreferencePage.class.getName()+".NSISHomesList.ser"); //$NON-NLS-1$
    private static IJobStatusRunnable cSaveNSISHomesRunnable = new IJobStatusRunnable() {
        public IStatus run(IProgressMonitor monitor)
        {
            try {
                IOUtility.writeObject(cNSISHomesListFile,cInternalNSISHomes);
                return Status.OK_STATUS;
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
                return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,e.getMessage(),e);
            }
        }
    };
    private static Map cSolidCompressionMap = new HashMap();
    private static final String[] cAutoShowConsoleText;

    static {
        Collection nsisHomes;
        if(cNSISHomesListFile.exists()) {
            try {
                nsisHomes = (Collection)IOUtility.readObject(cNSISHomesListFile);
                if(!(nsisHomes instanceof List)) {
                    nsisHomes = new ArrayList(nsisHomes);
                }
            }
            catch (Exception e1) {
                nsisHomes = new ArrayList();
            }
        }
        else {
            nsisHomes = new ArrayList();
        }
        cInternalNSISHomes = (List)nsisHomes;
        NSIS_HOMES = Collections.unmodifiableList(cInternalNSISHomes);
        
        cAutoShowConsoleText = new String[AUTO_SHOW_CONSOLE_ARRAY.length];
        for (int i = 0; i < AUTO_SHOW_CONSOLE_ARRAY.length; i++) {
            cAutoShowConsoleText[i] = EclipseNSISPlugin.getResourceString("auto.show.console."+AUTO_SHOW_CONSOLE_ARRAY[i]); //$NON-NLS-1$
        }
    }

    public static boolean addNSISHome(String nsisHome)
    {
        return addNSISHome(cInternalNSISHomes,nsisHome);
    }

    private static boolean addNSISHome(List nsisHomesList, String nsisHome)
    {
        if(nsisHomesList.size() > 0) {
            if(((String)nsisHomesList.get(0)).equalsIgnoreCase(nsisHome)) {
                return false;
            }
            removeNSISHome(nsisHomesList, nsisHome);
        }
        nsisHomesList.add(0,nsisHome);
        return true;
    }

    public static boolean removeNSISHome(String nsisHome)
    {
        return removeNSISHome(cInternalNSISHomes, nsisHome);
    }

    private static boolean removeNSISHome(List nsisHomesList, String nsisHome)
    {
        for(Iterator iter=nsisHomesList.iterator(); iter.hasNext(); ) {
            if(((String)iter.next()).equalsIgnoreCase(nsisHome)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public static void saveNSISHomes()
    {
        JobScheduler scheduler = EclipseNSISPlugin.getDefault().getJobScheduler();
        scheduler.cancelJobs(NSISPreferencePage.class);
        scheduler.scheduleJob(NSISPreferencePage.class, EclipseNSISPlugin.getResourceString("preferences.save.nsis.homes.job.name"), cSaveNSISHomesRunnable); //$NON-NLS-1$
    }

    public static void show()
    {
        PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                PREFERENCE_PAGE_ID, null, null).open();
    }

    protected String getContextId()
    {
        return PLUGIN_CONTEXT_PREFIX + "nsis_prefs_context"; //$NON-NLS-1$
    }

    protected String getPageDescription()
    {
        return EclipseNSISPlugin.getResourceString("preferences.header.text"); //$NON-NLS-1$
    }

    protected NSISSettingsEditor createSettingsEditor()
    {
        return new PreferencesEditor();
    }


    private class PreferencesEditor extends NSISSettingsEditor
    {
        private File mNSISExe = null;
        private Version mNSISVersion = Version.EMPTY_VERSION;

        public boolean isValid()
        {
            return mNSISExe != null;
        }

        protected NSISSettingsEditorGeneralPage createGeneralPage()
        {
            return new PreferencesEditorGeneralPage(getSettings());
        }

        protected NSISSettings loadSettings()
        {
            mNSISVersion = NSISPreferences.INSTANCE.getNSISVersion();
            mNSISExe = NSISPreferences.INSTANCE.getNSISExeFile();
            return NSISPreferences.INSTANCE;
        }
        
        private class PreferencesEditorGeneralPage extends NSISSettingsEditorGeneralPage
        {
            private ComboViewer mNSISHome = null;
            private Button mUseEclipseHelp = null;
            private Combo mAutoShowConsole = null;
            private Button mNotifyMakeNSISChanged = null;
            private boolean mNSISHomeDirty = false;
            private boolean mHandlingNSISHomeChange = false;

            public PreferencesEditorGeneralPage(NSISSettings settings)
            {
                super(settings);
            }

            protected boolean isSolidCompressionSupported()
            {
                if(mNSISVersion.compareTo(NSISPreferences.VERSION_2_07) >= 0) {
                    if(IOUtility.isValidFile(mNSISExe)) {
                        long[] data = (long[])cSolidCompressionMap.get(mNSISExe);
                        if(data != null) {
                            if(data[0] == mNSISExe.lastModified() && data[1] == mNSISExe.length()) {
                                return (data[2] == 1);
                            }
                        }
                        else {
                            data = new long[3];
                        }
                        data[0] = mNSISExe.lastModified();
                        data[1] = mNSISExe.length();
                        data[2] = 0;
                        Properties options = NSISValidator.loadNSISDefaultSymbols(mNSISExe);
                        if(options.containsKey(NSISPreferences.NSIS_CONFIG_COMPRESSION_SUPPORT)) {
                            data[2] = 1;
                        }
                        cSolidCompressionMap.put(mNSISExe,data);
                        return (data[2] == 1);
                    }
                }
                return false;
            }

            private boolean handleNSISHomeChange(boolean eraseInvalid)
            {
                if(mNSISHomeDirty && !mHandlingNSISHomeChange) {
                    try {
                        mHandlingNSISHomeChange = true;
                        boolean state = false;
                        String nsisHome = mNSISHome.getCombo().getText();
                        if(!Common.isEmpty(nsisHome)) {
                            if(nsisHome.endsWith("\\") && !nsisHome.endsWith(":\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                                nsisHome = nsisHome.substring(0,nsisHome.length()-1);
                                    mNSISHome.getCombo().setText(nsisHome);
                            }
                            if (!NSISValidator.validateNSISHome(nsisHome)) {
                                if(eraseInvalid) {
                                    Common.openError(getShell(),
                                                     EclipseNSISPlugin.getResourceString("invalid.nsis.home.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                    mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                                    mNSISHome.getCombo().forceFocus();
                                    mNSISHomeDirty = false;
                                }
                                mNSISVersion = Version.EMPTY_VERSION;
                                mNSISExe = null;
                                mSolidCompression.setVisible(false);
                            }
                            else {
                                state = true;
                                mNSISExe = new File(nsisHome, MAKENSIS_EXE);
                                mNSISVersion = NSISValidator.getNSISVersion(mNSISExe);
                                mSolidCompression.setVisible(isSolidCompressionSupported());
                                mNSISHomeDirty = false;
                            }
                        }
                        else {
                            mNSISHomeDirty = false;
                        }
                        setValid(state);
                        enableControls(state);
                        return state;
                    }
                    finally {
                        mHandlingNSISHomeChange = false;
                        fireChanged();
                    }
                }
                return true;
            }
            
            public void setDefaults()
            {
                super.setDefaults();
                mUseEclipseHelp.setSelection(true);
                mAutoShowConsole.select(0);
                mNotifyMakeNSISChanged.setSelection(false);
            }
            
            private int getAutoShowConsoleIndex(int autoShowConsole)
            {
                for (int i = 0; i < AUTO_SHOW_CONSOLE_ARRAY.length; i++) {
                    if(AUTO_SHOW_CONSOLE_ARRAY[i]==autoShowConsole) {
                        return i;
                    }
                }
                return 0;
            }

            public void reset()
            {
                NSISPreferences prefs = (NSISPreferences)getSettings();
                mNSISHome.getCombo().setText(prefs.getNSISHome());
                mUseEclipseHelp.setSelection(prefs.isUseEclipseHelp());
                mAutoShowConsole.select(getAutoShowConsoleIndex(prefs.getAutoShowConsole()));
                mNotifyMakeNSISChanged.setSelection(prefs.getBoolean(NOTIFY_MAKENSIS_CHANGED));
                super.reset();
            }

            protected boolean performApply(NSISSettings settings)
            {
                if (getControl() != null) {
                    if(!handleNSISHomeChange(true)) {
                        return false;
                    }
                    if(super.performApply(settings)) {
                        Combo combo = mNSISHome.getCombo();
                        String home = combo.getText();

                        List nsisHomes = (List)mNSISHome.getInput();
                        if (addNSISHome(nsisHomes, home)) {
                            mNSISHome.refresh();
                            combo.setText(home);
                        }

                        boolean dirty = false;
                        if (cInternalNSISHomes.size() == nsisHomes.size()) {
                            ListIterator e1 = cInternalNSISHomes.listIterator();
                            ListIterator e2 = nsisHomes.listIterator();
                            while (e1.hasNext() && e2.hasNext()) {
                                String s1 = (String)e1.next();
                                String s2 = (String)e2.next();
                                if (!Common.stringsAreEqual(s1, s2, true)) {
                                    dirty = true;
                                    break;
                                }
                            }
                        }
                        else {
                            dirty = true;
                        }
                        if (dirty) {
                            cInternalNSISHomes.clear();
                            cInternalNSISHomes.addAll(nsisHomes);
                            saveNSISHomes();
                        }

                        NSISPreferences preferences = (NSISPreferences)settings;
                        preferences.setNSISHome(home);
                        preferences.setAutoShowConsole(AUTO_SHOW_CONSOLE_ARRAY[mAutoShowConsole.getSelectionIndex()]);
                        preferences.setUseEclipseHelp(mUseEclipseHelp.getSelection());
                        preferences.setValue(NOTIFY_MAKENSIS_CHANGED, mNotifyMakeNSISChanged.getSelection());
                        return true;
                    }
                }
                return false;
            }

            public void enableControls(boolean state)
            {
                mAutoShowConsole.setEnabled(state);
                mUseEclipseHelp.setEnabled(state);
                mNotifyMakeNSISChanged.setEnabled(state);
                super.enableControls(state);
            }

            public boolean canEnableControls()
            {
                return !Common.isEmpty(mNSISHome.getCombo().getText());
            }

            protected Composite createMasterControl(Composite parent)
            {
                Composite composite = new Composite(parent,SWT.NONE);
                GridLayout layout = new GridLayout(3,false);
                layout.marginWidth = 0;
                composite.setLayout(layout);

                Label label = new Label(composite, SWT.LEFT);
                label.setText(EclipseNSISPlugin.getResourceString("nsis.home.text")); //$NON-NLS-1$
                GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
                label.setLayoutData(data);

                Combo c = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
                c.setToolTipText(EclipseNSISPlugin.getResourceString("nsis.home.tooltip")); //$NON-NLS-1$
                data = new GridData(SWT.FILL, SWT.CENTER, true, false);
                c.setLayoutData(data);

                List nsisHomes = new ArrayList(cInternalNSISHomes);
                String home = ((NSISPreferences)getSettings()).getNSISHome();
                addNSISHome(nsisHomes, home);

                mNSISHome = new ComboViewer(c);
                mNSISHome.setContentProvider(new CollectionContentProvider());
                mNSISHome.setLabelProvider(new CollectionLabelProvider());
                mNSISHome.setInput(nsisHomes);

                c.setText(home);
                c.addModifyListener(new ModifyListener(){
                    public void modifyText(ModifyEvent e)
                    {
                        mNSISHomeDirty = true;
                    }
                });
                c.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected(SelectionEvent e)
                    {
                        mNSISHomeDirty = true;
                        handleNSISHomeChange(true);
                    }
                });
                c.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e)
                    {
                        handleNSISHomeChange(false);
                    }
                });

                Button button = createButton(composite, EclipseNSISPlugin.getResourceString("browse.text"), //$NON-NLS-1$
                                             EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
                button.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        Shell shell = getShell();
                        DirectoryDialog dialog = new DirectoryDialog(shell);
                        dialog.setMessage(EclipseNSISPlugin.getResourceString("nsis.home.message")); //$NON-NLS-1$
                        String text = mNSISHome.getCombo().getText();
                        dialog.setFilterPath(text);
                        String nsisHome = dialog.open();
                        if (!Common.isEmpty(nsisHome)) {
                            if(NSISValidator.validateNSISHome(nsisHome)) {
                                mNSISHome.getCombo().setText(nsisHome);
                                enableControls(true);
                            }
                            else {
                                Common.openError(getShell(), EclipseNSISPlugin.getResourceString("invalid.nsis.home.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                                mNSISHome.getCombo().setFocus();
                                enableControls(false);
                            }
                        }
                    }
                });

                Composite composite2 = new Composite(composite,SWT.None);
                data = new GridData(SWT.FILL,SWT.FILL,false,false);
                data.horizontalSpan = 3;
                composite2.setLayoutData(data);
                layout = new GridLayout(2,false);
                layout.marginWidth = 0;
                layout.marginHeight = 0;
                composite2.setLayout(layout);
                mAutoShowConsole = createCombo(composite2, EclipseNSISPlugin.getResourceString("auto.show.console.text"), //$NON-NLS-1$
                                              EclipseNSISPlugin.getResourceString("auto.show.console.tooltip"), //$NON-NLS-1$
                                              cAutoShowConsoleText,getAutoShowConsoleIndex(((NSISPreferences)getSettings()).getAutoShowConsole()));

                mUseEclipseHelp = createCheckBox(composite, EclipseNSISPlugin.getResourceString("use.eclipse.help.text"), //$NON-NLS-1$
                                              EclipseNSISPlugin.getResourceString("use.eclipse.help.tooltip"), //$NON-NLS-1$
                                              ((NSISPreferences)getSettings()).isUseEclipseHelp());
                ((GridData)mUseEclipseHelp.getLayoutData()).horizontalSpan = 3;

                mNotifyMakeNSISChanged = createCheckBox(composite, EclipseNSISPlugin.getResourceString("notify.makensis.changed.text"), //$NON-NLS-1$
                                              EclipseNSISPlugin.getResourceString("notify.makensis.changed.tooltip"), //$NON-NLS-1$
                                              NSISPreferences.INSTANCE.getPreferenceStore().getBoolean(NOTIFY_MAKENSIS_CHANGED));
                ((GridData)mNotifyMakeNSISChanged.getLayoutData()).horizontalSpan = 3;
                return composite;
            }
        }
    }
}