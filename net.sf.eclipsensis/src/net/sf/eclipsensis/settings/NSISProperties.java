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

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class NSISProperties extends NSISSettings implements INSISConstants
{
    private static NSISPreferences cPreferences;
    private static HashMap cPropertiesCache = new HashMap();
    private static HashMap cQualifiedNames = new HashMap();

    private IFile mFile = null;
    private boolean mUseGlobals = true;
    
    private static Random cRandom = new Random();
    
    static {
        cPreferences = NSISPreferences.getPreferences();
        cQualifiedNames.put(USE_GLOBALS, new QualifiedName(PLUGIN_NAME,USE_GLOBALS));
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
        if(!cPropertiesCache.containsKey(fileName)) {
            synchronized(NSISProperties.class) {
                if(!cPropertiesCache.containsKey(fileName)) {
                    NSISProperties props = new NSISProperties(file);
                    props.load();
                    cPropertiesCache.put(fileName,props);
                }
            }
        }
        return (NSISProperties)cPropertiesCache.get(fileName);
    }
    
    public int getCompressor()
    {
        if(getUseGlobals()) {
            return cPreferences.getCompressor();
        }
        else {
            return super.getCompressor();
        }
    }
    
    public boolean getHdrInfo()
    {
        if(getUseGlobals()) {
            return cPreferences.getHdrInfo();
        }
        else {
            return super.getHdrInfo();
        }
    }
    
    public ArrayList getInstructions()
    {
        if(getUseGlobals()) {
            return cPreferences.getInstructions();
        }
        else {
            return super.getInstructions();
        }
    }
    
    public boolean getLicense()
    {
        if(getUseGlobals()) {
            return cPreferences.getLicense();
        }
        else {
            return super.getLicense();
        }
    }
    
    public boolean getNoCD()
    {
        if(getUseGlobals()) {
            return cPreferences.getNoCD();
        }
        else {
            return super.getNoCD();
        }
    }
    
    public boolean getNoConfig()
    {
        if(getUseGlobals()) {
            return cPreferences.getNoConfig();
        }
        else {
            return super.getNoConfig();
        }
    }
    
    public LinkedHashMap getSymbols()
    {
        if(getUseGlobals()) {
            return cPreferences.getSymbols();
        }
        else {
            return super.getSymbols();
        }
    }
    
    public int getVerbosity()
    {
        if(getUseGlobals()) {
            return cPreferences.getVerbosity();
        }
        else {
            return super.getVerbosity();
        }
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
        String temp = getPersistentProperty(getQualifiedName(USE_GLOBALS));
        setUseGlobals((temp == null || Boolean.valueOf(temp).booleanValue()));
        if(!getUseGlobals()) {
            super.load();
        }
    }
    
    public void store()
    {
        setValue(USE_GLOBALS,getUseGlobals());
        if(getUseGlobals()) {
            setHdrInfo(getDefaultHdrInfo());
            setLicense(getDefaultLicense());
            setNoConfig(getDefaultNoConfig());
            setNoCD(getDefaultNoCD());
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
    public boolean getUseGlobals()
    {
        return mUseGlobals;
    }
    
    /**
     * @param useGlobals The useGlobals to set.
     */
    public void setUseGlobals(boolean useGlobals)
    {
        mUseGlobals = useGlobals;
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
    protected Object loadObject(String name)
    {
        QualifiedName qname = getQualifiedName(name);
        ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
        synchronizer.add(qname);
        InputStream is = null;
        try {
            byte[] bytes = synchronizer.getSyncInfo(qname,mFile);
            if(!Common.isEmptyArray(bytes)) {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                is = new BufferedInputStream(bais);
                Object object = Common.readObject(is);
                return object;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            Common.closeIO(is);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#storeObject(java.lang.String, java.lang.Object)
     */
    protected void storeObject(String name, Object object)
    {
        try {
            QualifiedName qname = getQualifiedName(name);
            ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
            synchronizer.add(qname);
            if(object != null) {
                OutputStream os = null;
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    os = new BufferedOutputStream(baos);
                    Common.writeObject(os,object);
                    os.close();
                    os = null;
                    synchronizer.setSyncInfo(qname,mFile,baos.toByteArray());
                    return;
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                finally {
                    Common.closeIO(os);
                }
            }
            synchronizer.setSyncInfo(qname,mFile,null);
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }
}
