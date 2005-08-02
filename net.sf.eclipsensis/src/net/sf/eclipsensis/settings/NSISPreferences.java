/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.MinimalProgressMonitorDialog;
import net.sf.eclipsensis.editor.NSISTaskTag;
import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class NSISPreferences extends NSISSettings
{
    public static final NSISPreferences INSTANCE;
    
    private IPreferenceStore mPreferenceStore = null;
    private File mNSISExe = null;
    private String mNSISHome = null;
    private Version mNSISVersion = null;
    private boolean mUseEclipseHelp = true;
    private boolean mAutoShowConsole = true;
    private Properties mNSISOptions = null;
    private Collection mTaskTags = null;
    private Collection mDefaultTaskTags = null;
    private boolean mCaseSensitiveTaskTags = true;
    private List mListeners = new ArrayList();

    static
    {
        IPreferenceStore preferenceStore = EclipseNSISPlugin.getDefault().getPreferenceStore();
        NSISPreferences instance = new NSISPreferences(preferenceStore);
        instance.load();
        INSTANCE = instance;
    }
    
    protected NSISPreferences(IPreferenceStore preferenceStore)
    {
        mPreferenceStore = preferenceStore;
    }

    public void addListener(INSISHomeListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(INSISHomeListener listener)
    {
        mListeners.remove(listener);
    }

    private void notifyListeners(final String oldHome, final String newHome)
    {
        if(mListeners.size() > 0) {
            final IRunnableWithProgress op = new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask(EclipseNSISPlugin.getResourceString("propagating.home.message"),10*mListeners.size()); //$NON-NLS-1$
                    for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
                        try {
                            ((INSISHomeListener)iter.next()).nsisHomeChanged(monitor, oldHome, newHome);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        monitor.worked(10);
                    }
                }
            };
            ProgressMonitorDialog dialog = new MinimalProgressMonitorDialog(Display.getDefault().getActiveShell());
            try {
                dialog.run(false,false,op);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * @return Returns the preferenceStore.
     */
    public IPreferenceStore getPreferenceStore()
    {
        return mPreferenceStore;
    }
    
    private void initializePreference(String name, Object defaultValue)
    {
        if(defaultValue != null) {
            Class type = defaultValue.getClass();
            if(type.equals(Integer.class)) {
                mPreferenceStore.setDefault(name,((Integer)defaultValue).intValue());
            }
            else if(type.equals(Long.class)) {
                mPreferenceStore.setDefault(name,((Long)defaultValue).longValue());
            }
            else if(type.equals(Float.class)) {
                mPreferenceStore.setDefault(name,((Float)defaultValue).floatValue());
            }
            else if(type.equals(Double.class)) {
                mPreferenceStore.setDefault(name,((Double)defaultValue).doubleValue());
            }
            else if(type.equals(Boolean.class)) {
                mPreferenceStore.setDefault(name,((Boolean)defaultValue).booleanValue());
            }
            else {
                mPreferenceStore.setDefault(name, defaultValue.toString());
            }
            if(!mPreferenceStore.contains(name)) {
                mPreferenceStore.setToDefault(name);
            }
        }
    }

    private void initializeNSISPreferences()
    {
        initializePreference(NSIS_HOME,""); //$NON-NLS-1$
        initializePreference(USE_ECLIPSE_HELP,Boolean.FALSE); //$NON-NLS-1$
        initializePreference(HDRINFO,(getDefaultHdrInfo()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(LICENSE,(getDefaultLicense()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCONFIG,(getDefaultNoConfig()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCD,(getDefaultNoCD()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(VERBOSITY,new Integer(getDefaultVerbosity()));
        initializePreference(COMPRESSOR,new Integer(getDefaultCompressor()));
        initializePreference(SOLID_COMPRESSION,(getDefaultSolidCompression()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(AUTO_SHOW_CONSOLE,Boolean.TRUE);
        initializePreference(INSTRUCTIONS,""); //$NON-NLS-1$
        initializePreference(SYMBOLS,""); //$NON-NLS-1$
        
        setNSISHome(mPreferenceStore.getString(NSIS_HOME));
        setUseEclipseHelp(mPreferenceStore.getBoolean(USE_ECLIPSE_HELP));
    }

    private void initializeEditorPreferences()
    {
        initializePreference(MATCHING_DELIMITERS,Boolean.TRUE);
        initializePreference(MATCHING_DELIMITERS_COLOR,StringConverter.asString(new RGB(128,128,128)));

        initializePreference(USE_SPACES_FOR_TABS,Boolean.TRUE);
    }
    
    private void initializeSyntaxPreference(String name, RGB foreground, RGB background, boolean bold, 
                                            boolean italic, boolean underline, boolean strikethrough)
    {
        NSISSyntaxStyle style = new NSISSyntaxStyle(foreground, background, bold, italic,
                                                    underline, strikethrough);
        mPreferenceStore.setDefault(name,style.toString());
        if(!mPreferenceStore.contains(name)) {
            mPreferenceStore.setToDefault(name);
        }
    }

    private void initializeSyntaxPreferences()
    {
        initializeSyntaxPreference(CALLBACKS_STYLE,ColorManager.NAVY_BLUE, null, false, false, false, false);
        initializeSyntaxPreference(SYMBOLS_STYLE,ColorManager.PURPLE, null, false, false, false, false);
        initializeSyntaxPreference(LANGSTRINGS_STYLE,ColorManager.CHOCOLATE, null, false, false, false, false);
        initializeSyntaxPreference(PREDEFINED_VARIABLES_STYLE,ColorManager.DARK_OLIVE_GREEN, null, false, false, false, false);
        initializeSyntaxPreference(USERDEFINED_VARIABLES_STYLE,ColorManager.PURPLE, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTIONS_STYLE,ColorManager.PINK, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTION_PARAMETERS_STYLE,ColorManager.PURPLE, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTION_OPTIONS_STYLE,ColorManager.ORCHID, null, false, false, false, false);
        initializeSyntaxPreference(COMMANDS_STYLE,ColorManager.ORANGE, null, true, false, false, false);
        initializeSyntaxPreference(INSTALLER_ATTRIBUTES_STYLE,ColorManager.MAGENTA, null, false, false, false, false);
        initializeSyntaxPreference(COMPILETIME_COMMANDS_STYLE,ColorManager.RED, null, false, false, false, false);
        initializeSyntaxPreference(NUMBERS_STYLE,ColorManager.RED, null, false, false, false, false);
        initializeSyntaxPreference(STRINGS_STYLE,ColorManager.TURQUOISE, null, false, false, false, false);
        initializeSyntaxPreference(COMMENTS_STYLE,ColorManager.GREY, null, false, true, false, false);
        initializeSyntaxPreference(TASK_TAGS_STYLE,ColorManager.TEAL, null, true, false, false, false);
        initializeSyntaxPreference(PLUGINS_STYLE,ColorManager.BEIGE, null, false, false, false, false);
    }

    protected void load()
    {
        initializeNSISPreferences();
        initializeEditorPreferences();
        initializeSyntaxPreferences();
        initializeDefaultTaskTags();
        initializePreference(TASK_TAGS,""); //$NON-NLS-1$
        mTaskTags = (Collection)loadObject(TASK_TAGS);
        initializePreference(CASE_SENSITIVE_TASK_TAGS,Boolean.TRUE); //$NON-NLS-1$
        mCaseSensitiveTaskTags = getBoolean(CASE_SENSITIVE_TASK_TAGS);
        super.load();
    }
    
    /**
     * 
     */
    private void initializeDefaultTaskTags()
    {
        String defaultTaskTag = EclipseNSISPlugin.getResourceString("default.task.tag",""); //$NON-NLS-1$ //$NON-NLS-2$
        String[] taskTags = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"task.tags"); //$NON-NLS-1$
        String[] taskTagPriorities = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"task.priorities"); //$NON-NLS-1$
        mDefaultTaskTags = new ArrayList();
        if(!Common.isEmptyArray(taskTags)) {
            for (int i = 0; i < taskTags.length; i++) {
                NSISTaskTag tag;
                if(!Common.isEmptyArray(taskTagPriorities) && taskTagPriorities.length > i) {
                    try {
                        tag = new NSISTaskTag(taskTags[i],Integer.parseInt(taskTagPriorities[i]));
                    }
                    catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                        tag = new NSISTaskTag(taskTags[i],IMarker.PRIORITY_NORMAL);
                    }
                }
                else {
                    tag = new NSISTaskTag(taskTags[i],IMarker.PRIORITY_NORMAL);
                }
                tag.setDefault(taskTags[i].equals(defaultTaskTag));
                mDefaultTaskTags.add(tag);
            }
        }
    }

    public void store()
    {
        setValue(NSIS_HOME,mNSISHome);
        setValue(USE_ECLIPSE_HELP,mUseEclipseHelp);
        setValue(AUTO_SHOW_CONSOLE,mAutoShowConsole);
        setValue(CASE_SENSITIVE_TASK_TAGS,mCaseSensitiveTaskTags);
        storeObject(TASK_TAGS,mTaskTags);
        super.store();
    }

    private Collection createCopy(Collection tags)
    {
        Collection copy = new ArrayList();
        for (Iterator iter=tags.iterator(); iter.hasNext(); ) {
            copy.add(new NSISTaskTag((NSISTaskTag)iter.next()));
        }
        return copy;
    }

    /**
     * @return Returns the taskTags.
     */
    public Collection getTaskTags()
    {
        return (mTaskTags == null?getDefaultTaskTags():createCopy(mTaskTags));
    }
    
    /**
     * @param taskTags The taskTags to set.
     */
    public void setTaskTags(Collection taskTags)
    {
        if(taskTags != null && taskTags.size() == mDefaultTaskTags.size()) {
            boolean different = false;
            for (Iterator iter = taskTags.iterator(); iter.hasNext();) {
                if(!mDefaultTaskTags.contains(iter.next())) {
                    different = true;
                    break;
                }
            }
            if(!different) {
                taskTags = null;
            }
        }
        mTaskTags = taskTags;
    }
    
    
    /**
     * @return Returns the defaultTaskTags.
     */
    public Collection getDefaultTaskTags()
    {
        return createCopy(mDefaultTaskTags);
    }
    
    /**
     * @return Returns the caseSensitiveTaskTags.
     */
    public boolean isCaseSensitiveTaskTags()
    {
        return mCaseSensitiveTaskTags;
    }
    
    /**
     * @param caseSensitiveTaskTags The caseSensitiveTaskTags to set.
     */
    public void setCaseSensitiveTaskTags(boolean caseSensitiveTaskTags)
    {
        mCaseSensitiveTaskTags = caseSensitiveTaskTags;
    }
    
    /**
     * @return Returns the NSISHome.
     */
    public String getNSISHome()
    {
        return mNSISHome;
    }
    
    /**
     * @param nsisHome The NSISHome to set.
     */
    public void setNSISHome(String nsisHome)
    {
        String oldHome = mNSISHome;
        mNSISExe = (nsisHome==null?null:NSISValidator.findNSISExe(new File(nsisHome)));
        if(mNSISExe != null) {
            mNSISHome = nsisHome;
            mNSISVersion = NSISValidator.getNSISVersion(mNSISExe);
            mNSISOptions = NSISValidator.loadNSISOptions(mNSISExe);
        }
        else {
            mNSISHome = ""; //$NON-NLS-1$
            mNSISVersion = Version.EMPTY_VERSION;
            mNSISOptions = null;
        }
        if(!Common.stringsAreEqual(oldHome, mNSISHome)) {
            notifyListeners(oldHome, mNSISHome);
        }
    }

    public String getNSISExe()
    {
        return (mNSISExe !=null?mNSISExe.getAbsolutePath():null);
    }

    public File getNSISExeFile()
    {
        return mNSISExe;
    }
    
    /**
     * @return Returns the NSIS Options.
     */
    public Properties getNSISOptions()
    {
        return mNSISOptions;
    }
    
    /**
     * @return Returns the NSIS Version.
     */
    public Version getNSISVersion()
    {
        return mNSISVersion;
    }
    
    /**
     * @return Returns the useIntegratedHelp.
     */
    public boolean isUseEclipseHelp()
    {
        return mUseEclipseHelp;
    }

    /**
     * @param useEclipseHelp The useEclipseHelp to set.
     */
    public void setUseEclipseHelp(boolean useEclipseHelp)
    {
        mUseEclipseHelp = useEclipseHelp;
    }
    
    /**
     * @return Returns the autoShowConsole.
     */
    public boolean isAutoShowConsole()
    {
        return mAutoShowConsole;
    }
    
    /**
     * @param autoShowConsole The autoShowConsole to set.
     */
    public void setAutoShowConsole(boolean autoShowConsole)
    {
        mAutoShowConsole = autoShowConsole;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    protected boolean getBoolean(String name)
    {
        return mPreferenceStore.getBoolean(name);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    protected int getInt(String name)
    {
        return mPreferenceStore.getInt(name);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    protected String getString(String name)
    {
        return mPreferenceStore.getString(name);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    protected void setValue(String name, boolean value)
    {
        mPreferenceStore.setValue(name, value);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    protected void setValue(String name, int value)
    {
        mPreferenceStore.setValue(name, value);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    protected void setValue(String name, String value)
    {
        mPreferenceStore.setValue(name, value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    protected void removeBoolean(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    protected void removeInt(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.INT_DEFAULT_DEFAULT);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    protected void removeString(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.STRING_DEFAULT_DEFAULT);
    }

    public String getNSISOption(String option)
    {
        return mNSISOptions.getProperty(option);
    }
    
    protected void storeObject(String name, Object object)
    {
        String fileName = getString(name);
        if(Common.isEmpty(fileName)) {
            fileName = makeSettingFileName(name);
        }
        File objectFile = new File(cPluginStateLocation,fileName);
        if(object == null) {
            if(objectFile.exists()) {
                objectFile.delete();
            }
            setValue(name,""); //$NON-NLS-1$
        }
        else {
            try {
                Common.writeObject(objectFile, object);
                setValue(name,fileName);
            }
            catch(IOException ioe) {
                setValue(name,""); //$NON-NLS-1$
                ioe.printStackTrace();
            }
        }
    }

    protected Object loadObject(String name)
    {
        String fileName = getString(name);
        File objectFile = new File(cPluginStateLocation,fileName);
        Object object = null;
        if(objectFile.exists()) {
            try {
                object = Common.readObject(objectFile);
            }
            catch (Exception e) {
                object = null;
            }
        }
        
        return object;
    }
    
    private String makeSettingFileName(String name)
    {
        return new StringBuffer(getClass().getName()).append(".").append(name).append(".ser").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
