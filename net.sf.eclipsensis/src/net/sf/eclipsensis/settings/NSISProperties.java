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

import java.util.HashMap;
import java.util.Random;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class NSISProperties extends NSISSettings implements INSISConstants
{
    private static HashMap cPropertiesCache = new HashMap();
    private static HashMap cQualifiedNames = new HashMap();

    private IFile mFile = null;
    private boolean mUseDefaults = true;
    
    private static Random cRandom = new Random();
    
    static {
        cQualifiedNames.put(USE_DEFAULTS, new QualifiedName(PLUGIN_NAME,USE_DEFAULTS));
        cQualifiedNames.put(HDRINFO, new QualifiedName(PLUGIN_NAME,HDRINFO));
        cQualifiedNames.put(VERBOSITY, new QualifiedName(PLUGIN_NAME,VERBOSITY));
        cQualifiedNames.put(LICENSE, new QualifiedName(PLUGIN_NAME,LICENSE));
        cQualifiedNames.put(NOCONFIG, new QualifiedName(PLUGIN_NAME,NOCONFIG));
        cQualifiedNames.put(NOCD, new QualifiedName(PLUGIN_NAME,NOCD));
        cQualifiedNames.put(COMPRESSOR, new QualifiedName(PLUGIN_NAME,COMPRESSOR));
        cQualifiedNames.put(INSTRUCTIONS, new QualifiedName(PLUGIN_NAME,INSTRUCTIONS));
        cQualifiedNames.put(SYMBOLS, new QualifiedName(PLUGIN_NAME,SYMBOLS));
    }
    
    public static NSISProperties getProperties(IFile file)
    {
        String fileName = file.getLocation().toString();
        NSISProperties props = null;
        if(!cPropertiesCache.containsKey(fileName)) {
            synchronized(NSISProperties.class) {
                if(!cPropertiesCache.containsKey(fileName)) {
                    props = new NSISProperties(file);
                    props.load();
                    cPropertiesCache.put(fileName,props);
                }
            }
        }
        return (NSISProperties)cPropertiesCache.get(fileName);
    }
    
    private static QualifiedName getQualifiedName(String name)
    {
        QualifiedName qname = (QualifiedName)cQualifiedNames.get(name);
        if(qname == null) {
            synchronized(NSISProperties.class) {
                qname = (QualifiedName)cQualifiedNames.get(name);
                if(qname == null) {
                    qname = new QualifiedName(PLUGIN_NAME,name);
                    cQualifiedNames.put(name,qname);
                }
            }
        }
        return qname;
    }
    
    protected NSISProperties(IFile file)
    {
        mFile = file;
    }

    protected void load()
    {
        String temp = getPersistentProperty(getQualifiedName(USE_DEFAULTS));
        setUseDefaults((temp == null || Boolean.valueOf(temp).booleanValue()));
        if(!getUseDefaults()) {
            super.load();
        }
    }
    
    public void store()
    {
        setValue(USE_DEFAULTS,getUseDefaults());
        if(getUseDefaults()) {
            setHdrInfo(getDefaultHdrInfo());
            setLicense(getDefaultLicense());
            setNoConfig(getNoConfig());
            setVerbosity(getDefaultVerbosity());
            setCompressor(getDefaultCompressor());
            setSymbols(getDefaultSymbols());
            setInstructions(getDefaultInstructions());
        }
        super.store();
    }

    /**
     * @return Returns the useDefaults.
     */
    public boolean getUseDefaults()
    {
        return mUseDefaults;
    }
    
    /**
     * @param useDefaults The useDefaults to set.
     */
    public void setUseDefaults(boolean useDefaults)
    {
        mUseDefaults = useDefaults;
    }
    
    private String getPersistentProperty(QualifiedName qname)
    {
        String value = null;
        try {
            value = mFile.getPersistentProperty(qname);
        }
        catch(CoreException ex){
            value = null;
        }
        return value;
    }
    
    private void setPersistentProperty(QualifiedName qname, String value)
    {
        try {
            mFile.setPersistentProperty(qname, value);
        }
        catch(CoreException ex){
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    protected boolean getBoolean(String name)
    {
        String value = getPersistentProperty(getQualifiedName(name));
        return (value !=null && Boolean.valueOf(value).booleanValue());
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    protected int getInt(String name)
    {
        String value = getPersistentProperty(getQualifiedName(name));
        if(value != null) {
            try {
                return Integer.parseInt(value);
            }
            catch(NumberFormatException nfe) {
                return 0;
            }
        }
        else {
            return 0;
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    protected String getString(String name)
    {
        return getPersistentProperty(getQualifiedName(name));
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    protected void setValue(String name, boolean value)
    {
        setPersistentProperty(getQualifiedName(name), Boolean.toString(value));
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    protected void setValue(String name, int value)
    {
        setPersistentProperty(getQualifiedName(name), Integer.toString(value));
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    protected void setValue(String name, String value)
    {
        setPersistentProperty(getQualifiedName(name), value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    protected void removeBoolean(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    protected void removeInt(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    protected void removeString(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#loadObject(java.lang.String)
     */
    protected Object loadObject(String settingName)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#storeObject(java.lang.String, java.lang.Object)
     */
    protected void storeObject(String settingName, Object object)
    {
        // TODO Auto-generated method stub

    }
}
