/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.util.NSISValidator;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Sunil.Kamath
 */
public class NSISPreferences extends NSISSettings
{
    private IPreferenceStore mPreferenceStore = null;
    private File mNSISExe = null;
    private String mNSISHome = null;
    
    private static NSISPreferences cInstance = null;
    
    public static NSISPreferences getPreferences()
    {
        if(cInstance == null) {
            synchronized(NSISPreferences.class) {
                if(cInstance == null) {
                    cInstance = new NSISPreferences(EclipseNSISPlugin.getDefault().getPreferenceStore());
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

    protected void load()
    {
        mPreferenceStore.setDefault(NSIS_HOME,""); //$NON-NLS-1$
        mPreferenceStore.setDefault(HDRINFO,getDefaultHdrInfo());
        mPreferenceStore.setDefault(LICENSE,getDefaultLicense());
        mPreferenceStore.setDefault(NOCONFIG,getDefaultNoConfig());
        mPreferenceStore.setDefault(NOCD,getDefaultNoCD());
        mPreferenceStore.setDefault(VERBOSITY,getDefaultVerbosity());
        mPreferenceStore.setDefault(COMPRESSOR,getDefaultCompressor());
        mPreferenceStore.setDefault(INSTRUCTIONS,0); //$NON-NLS-1$
        mPreferenceStore.setDefault(SYMBOLS,""); //$NON-NLS-1$
        
        setNSISHome(mPreferenceStore.getString(NSIS_HOME));
        super.load();
    }
    
    public void store()
    {
        setValue(NSIS_HOME,mNSISHome);
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
        mNSISExe = NSISValidator.findNSISExe(new File(nsisHome));
        if(mNSISExe != null) {
            mNSISHome = nsisHome;
        }
        else {
            mNSISHome = ""; //$NON-NLS-1$
        }
        NSISEditor.updateEditorActionsState();
    }

    public String getNSISExe()
    {
        return (mNSISExe !=null?mNSISExe.getAbsolutePath():null);
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
}
