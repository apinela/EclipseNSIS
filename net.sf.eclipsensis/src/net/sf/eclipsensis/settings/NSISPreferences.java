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
import java.util.Properties;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;

public class NSISPreferences extends NSISSettings implements IPropertyChangeListener, IEclipseNSISPluginListener
{
    private IPreferenceStore mPreferenceStore = null;
    private File mNSISExe = null;
    private String mNSISHome = null;
    private Version mNSISVersion = null;
    private boolean mUseIntegratedHelp = true;
    private boolean mAutoShowConsole = true;
    private Properties mNSISOptions = null;
    
    private static NSISPreferences cInstance = null;
    
    public static NSISPreferences getPreferences()
    {
        if(cInstance == null) {
            synchronized(NSISPreferences.class) {
                if(cInstance == null) {
                    IPreferenceStore preferenceStore = EclipseNSISPlugin.getDefault().getPreferenceStore();
                    cInstance = new NSISPreferences(preferenceStore);
                    cInstance.load();
                }
            }
        }
        
        return cInstance;
    }
    
    protected NSISPreferences(IPreferenceStore preferenceStore)
    {
        mPreferenceStore = preferenceStore;
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
        if(!mPreferenceStore.contains(name)) {
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
                mPreferenceStore.setToDefault(name);
            }
        }
    }

    private void initializeNSISPreferences()
    {
        initializePreference(NSIS_HOME,""); //$NON-NLS-1$
        initializePreference(USE_INTEGRATED_HELP,Boolean.TRUE); //$NON-NLS-1$
        initializePreference(HDRINFO,(getDefaultHdrInfo()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(LICENSE,(getDefaultLicense()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCONFIG,(getDefaultNoConfig()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCD,(getDefaultNoCD()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(VERBOSITY,new Integer(getDefaultVerbosity()));
        initializePreference(COMPRESSOR,new Integer(getDefaultCompressor()));
        initializePreference(AUTO_SHOW_CONSOLE,Boolean.TRUE);
        initializePreference(INSTRUCTIONS,""); //$NON-NLS-1$
        initializePreference(SYMBOLS,""); //$NON-NLS-1$
        
        setNSISHome(mPreferenceStore.getString(NSIS_HOME));
        setUseIntegratedHelp(mPreferenceStore.getBoolean(USE_INTEGRATED_HELP));
    }

    private void initializeEditorPreference(String name, IPreferenceStore defaultStore, Class type)
    {
        if(!mPreferenceStore.contains(name)) {
            if(type.equals(Integer.class)) {
                mPreferenceStore.setDefault(name,defaultStore.getInt(name));
            }
            else if(type.equals(Long.class)) {
                mPreferenceStore.setDefault(name,defaultStore.getLong(name));
            }
            else if(type.equals(Float.class)) {
                mPreferenceStore.setDefault(name,defaultStore.getFloat(name));
            }
            else if(type.equals(Double.class)) {
                mPreferenceStore.setDefault(name,defaultStore.getDouble(name));
            }
            else if(type.equals(Boolean.class)) {
                mPreferenceStore.setDefault(name,defaultStore.getBoolean(name));
            }
            else {
                mPreferenceStore.setDefault(name,defaultStore.getString(name));
            }
            mPreferenceStore.setToDefault(name);
        }
    }

    private void initializeEditorPreferences()
    {
        IPreferenceStore defaultStore = EditorsUI.getPreferenceStore();
        initializeEditorPreference(CURRENT_LINE_COLOR,defaultStore,String.class);
        initializeEditorPreference(CURRENT_LINE,defaultStore,Boolean.class);

        initializeEditorPreference(TAB_WIDTH,defaultStore,Integer.class);

        initializeEditorPreference(PRINT_MARGIN_COLOR,defaultStore,String.class);
        initializeEditorPreference(PRINT_MARGIN_COLUMN,defaultStore,Integer.class);
        initializeEditorPreference(PRINT_MARGIN,defaultStore,Boolean.class);
        
        initializeEditorPreference(OVERVIEW_RULER,defaultStore,Boolean.class);
        
        initializeEditorPreference(LINE_NUMBER_RULER_COLOR,defaultStore,String.class);
        initializeEditorPreference(LINE_NUMBER_RULER,defaultStore,Boolean.class);
        initializeEditorPreference(USE_CUSTOM_CARETS,defaultStore,Boolean.class);
        initializeEditorPreference(WIDE_CARET,defaultStore,Boolean.class);
        
        initializeEditorPreference(SELECTION_FOREGROUND_COLOR,defaultStore,String.class);
        initializeEditorPreference(SELECTION_FOREGROUND_DEFAULT_COLOR,defaultStore,Boolean.class);
        initializeEditorPreference(SELECTION_BACKGROUND_COLOR,defaultStore,String.class);
        initializeEditorPreference(SELECTION_BACKGROUND_DEFAULT_COLOR,defaultStore,Boolean.class);

        initializePreference(MATCHING_DELIMITERS,Boolean.TRUE);
        initializePreference(MATCHING_DELIMITERS_COLOR,StringConverter.asString(new RGB(128,128,128)));

        initializePreference(USE_SPACES_FOR_TABS,Boolean.TRUE);
        
        String[] linkedModelPrefs = {"slave.","focus.","master.","exit."}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String[] linkedModelPrefSuffixes = { "text","text.style","color"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < linkedModelPrefs.length; i++) {
            String prefix = "linked."+linkedModelPrefs[i]; //$NON-NLS-1$
            for (int j = 0; j < linkedModelPrefSuffixes.length; j++) {
                String preference = prefix + linkedModelPrefSuffixes[j];
                initializePreference(preference, defaultStore.getString(preference));
            }
        }
        defaultStore.addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent event)
    {
        String name = event.getProperty();
        if(name.startsWith("linked.") && mPreferenceStore.contains(name)) { //$NON-NLS-1$
            mPreferenceStore.setValue(name, event.getNewValue().toString());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.IEclipseNSISPluginListener#stopped()
     */
    public void stopped()
    {
        try {
            EditorsUI.getPreferenceStore().removePropertyChangeListener(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeSyntaxPreference(String name, RGB foreground, RGB background, boolean bold, boolean italic)
    {
        if(!mPreferenceStore.contains(name)) {
            NSISSyntaxStyle style = new NSISSyntaxStyle(foreground, background, bold, italic);
            mPreferenceStore.setDefault(name,style.toString());
            mPreferenceStore.setToDefault(name);
        }
    }

    private void initializeSyntaxPreferences()
    {
        initializeSyntaxPreference(CALLBACKS_STYLE,ColorManager.NAVY_BLUE, null, false, false);
        initializeSyntaxPreference(SYMBOLS_STYLE,ColorManager.PURPLE, null, false, false);
        initializeSyntaxPreference(LANGSTRINGS_STYLE,ColorManager.CHOCOLATE, null, false, false);
        initializeSyntaxPreference(PREDEFINED_VARIABLES_STYLE,ColorManager.DARK_OLIVE_GREEN, null, false, false);
        initializeSyntaxPreference(USERDEFINED_VARIABLES_STYLE,ColorManager.PURPLE, null, false, false);
        initializeSyntaxPreference(INSTRUCTIONS_STYLE,ColorManager.PINK, null, false, false);
        initializeSyntaxPreference(INSTRUCTION_PARAMETERS_STYLE,ColorManager.PURPLE, null, false, false);
        initializeSyntaxPreference(INSTRUCTION_OPTIONS_STYLE,ColorManager.ORCHID, null, false, false);
        initializeSyntaxPreference(COMMANDS_STYLE,ColorManager.ORANGE, null, true, false);
        initializeSyntaxPreference(INSTALLER_ATTRIBUTES_STYLE,ColorManager.MAGENTA, null, false, false);
        initializeSyntaxPreference(COMPILETIME_COMMANDS_STYLE,ColorManager.RED, null, false, false);
        initializeSyntaxPreference(NUMBERS_STYLE,ColorManager.RED, null, false, false);
        initializeSyntaxPreference(STRINGS_STYLE,ColorManager.TURQUOISE, null, false, false);
        initializeSyntaxPreference(COMMENTS_STYLE,ColorManager.GREY, null, false, true);
    }

    protected void load()
    {
        initializeNSISPreferences();
        initializeEditorPreferences();
        initializeSyntaxPreferences();
        super.load();
    }
    
    public void store()
    {
        setValue(NSIS_HOME,mNSISHome);
        setValue(USE_INTEGRATED_HELP,mUseIntegratedHelp);
        super.store();
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
        mNSISExe = (nsisHome==null?null:NSISValidator.findNSISExe(new File(nsisHome)));
        if(mNSISExe != null) {
            mNSISHome = nsisHome;
            mNSISVersion = NSISValidator.getNSISVersion(mNSISExe);
            mNSISOptions = NSISValidator.loadNSISOptions(mNSISExe);
        }
        else {
            mNSISHome = ""; //$NON-NLS-1$
        }
    }

    public String getNSISExe()
    {
        return (mNSISExe !=null?mNSISExe.getAbsolutePath():null);
    }
    
    /**
     * @return Returns the nSISOptions.
     */
    public Properties getNSISOptions()
    {
        return mNSISOptions;
    }
    
    /**
     * @return Returns the nSISVersion.
     */
    public Version getNSISVersion()
    {
        return mNSISVersion;
    }
    
    /**
     * @return Returns the useIntegratedHelp.
     */
    public boolean isUseIntegratedHelp()
    {
        return mUseIntegratedHelp;
    }

    /**
     * @param useIntegratedHelp The useIntegratedHelp to set.
     */
    public void setUseIntegratedHelp(boolean useIntegratedHelp)
    {
        mUseIntegratedHelp = useIntegratedHelp;
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
