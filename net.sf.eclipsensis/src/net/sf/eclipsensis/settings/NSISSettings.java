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

import java.util.*;

import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;

public abstract class NSISSettings implements INSISPreferenceConstants
{
    private boolean mHdrInfo = false;
    private boolean mLicense = false;
    private boolean mNoConfig = false;
    private boolean mNoCD = false;
    private int mVerbosity = INSISPreferenceConstants.VERBOSITY_ALL;
    private int mCompressor = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private int mInstructionsCount = 0;
    private ArrayList mInstructions = null;
    private Properties mSymbols = null;
    private Properties mOldSymbols = null;

    protected abstract String getString(String name);
    protected abstract boolean getBoolean(String name);
    protected abstract int getInt(String name);
    protected abstract void setValue(String name, String value);
    protected abstract void setValue(String name, boolean value);
    protected abstract void setValue(String name, int value);
    protected abstract void removeString(String name);
    protected abstract void removeBoolean(String name);
    protected abstract void removeInt(String name);
    
    protected void load()
    {
        setHdrInfo(getBoolean(HDRINFO));
        setLicense(getBoolean(LICENSE));
        setNoConfig(getBoolean(NOCONFIG));
        setNoCD(getBoolean(NOCD));
        setVerbosity(getInt(VERBOSITY));
        setCompressor(getInt(COMPRESSOR));
        setInstructions(loadArrayList(INSTRUCTIONS));
        mInstructionsCount = mInstructions.size();
        setSymbols(loadProperties(SYMBOLS));
    }
    
    public void store()
    {
        setValue(HDRINFO,mHdrInfo);
        setValue(LICENSE, mLicense);
        setValue(NOCONFIG, mNoConfig);
        setValue(NOCD, mNoCD);
        setValue(VERBOSITY,mVerbosity);
        setValue(COMPRESSOR, mCompressor);
        storeProperties(SYMBOLS, mOldSymbols, mSymbols);
        mInstructionsCount = storeArrayList(INSTRUCTIONS, mInstructionsCount, mInstructions);
    }

    private String makeSettingsPropertyName(String settingName, String propertyName)
    {
        return new StringBuffer(settingName).append(".").append(propertyName).toString(); //$NON-NLS-1$
    }
    
    protected Properties loadProperties(String settingName)
    {
        Properties properties = new Properties();
        String propertiesList = getString(settingName);
        if(!Common.isEmpty(propertiesList)) {
            StringTokenizer st = new StringTokenizer(propertiesList,","); //$NON-NLS-1$
            while(st.hasMoreTokens()) {
                String property = st.nextToken();
                if(!Common.isEmpty(property)) {
                    String value = getString(makeSettingsPropertyName(settingName,property));
                    properties.setProperty(property,(value != null?value:"")); //$NON-NLS-1$
                }
            }
        }
        return properties;
    }

    protected Properties storeProperties(String settingName, Properties oldProperties, Properties properties)
    {
        if(oldProperties != null) {
            for(Iterator iter=oldProperties.keySet().iterator(); iter.hasNext(); ) {
                String key = (String)iter.next();
                if(!properties.containsKey(key)) {
                    setValue(makeSettingsPropertyName(settingName,key),""); //$NON-NLS-1$
                }
            }
        }
        StringBuffer propertiesList = new StringBuffer(""); //$NON-NLS-1$
        if(properties != null) {
            Iterator iter=properties.entrySet().iterator();
            if(iter.hasNext()) {
                do {
                    Map.Entry entry = (Map.Entry)iter.next();
                    String key = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    propertiesList.append(key).append((iter.hasNext()?",":"")); //$NON-NLS-1$ //$NON-NLS-2$
                    setValue(makeSettingsPropertyName(settingName,key),value);
                } while(iter.hasNext()); 
            }
            setValue(settingName,propertiesList.toString());
        }
        return properties;
    }
    
    private String makeSettingName(String settingName, String text, int index)
    {
        return new StringBuffer(settingName).append(".").append(text).append(index).toString(); //$NON-NLS-1$
    }
    
    protected ArrayList loadArrayList(String settingName)
    {
        ArrayList list = new ArrayList();
        int count = getInt(settingName);
        if(count > 0) {
            for(int i=0; i<count; i++) {
                String name = getString(makeSettingName(settingName,"item",i)); //$NON-NLS-1$
                if(!Common.isEmpty(name)) {
                    list.add(name.trim());
                }
            }
        }
        return list;
    }
    
    protected int storeArrayList(String settingName, int oldCount, ArrayList list)
    {
        int count = 0;
        if(list != null) {
            count = list.size();
            for(int i=0; i<count; i++) {
                String item = (String)list.get(i);
                setValue(makeSettingName(settingName,"item",i),item); //$NON-NLS-1$
            }
        }
        if(oldCount > count) {
            for(int i=count; i<oldCount; i++) {
                setValue(makeSettingName(settingName,"item",i),""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        setValue(settingName,count);
        return count;
    }
    
    /**
     * @return Returns the compressor.
     */
    public int getCompressor()
    {
        return mCompressor;
    }

    /**
     * @return Returns the default compressor.
     */
    public int getDefaultCompressor()
    {
        return MakeNSISRunner.COMPRESSOR_DEFAULT;
    }

    /**
     * @param compressor The compressor to set.
     */
    public void setCompressor(int compressor)
    {
        mCompressor = compressor;
    }
    
    /**
     * @return Returns the headerInfo.
     */
    public boolean getHdrInfo()
    {
        return mHdrInfo;
    }
    
    /**
     * @return Returns the headerInfo.
     */
    public boolean getDefaultHdrInfo()
    {
        return false;
    }
    
    /**
     * @param headerInfo The headerInfo to set.
     */
    public void setHdrInfo(boolean headerInfo)
    {
        mHdrInfo = headerInfo;
    }
    
    /**
     * @return Returns the license.
     */
    public boolean getLicense()
    {
        return mLicense;
    }
    /**
     * @return Returns the default license.
     */
    public boolean getDefaultLicense()
    {
        return false;
    }

    /**
     * @param license The license to set.
     */
    public void setLicense(boolean license)
    {
        mLicense = license;
    }
    
    /**
     * @return Returns the noCD.
     */
    public boolean getNoCD()
    {
        return mNoCD;
    }
    
    /**
     * @return Returns the default noCD.
     */
    public boolean getDefaultNoCD()
    {
        return false;
    }
    
    /**
     * @param noCD The noCD to set.
     */
    public void setNoCD(boolean noCD)
    {
        mNoCD = noCD;
    }
    
    /**
     * @return Returns the noConfig.
     */
    public boolean getNoConfig()
    {
        return mNoConfig;
    }
    /**
     * @return Returns the default noConfig.
     */
    public boolean getDefaultNoConfig()
    {
        return false;
    }

    /**
     * @param noConfig The noConfig to set.
     */
    public void setNoConfig(boolean noConfig)
    {
        mNoConfig = noConfig;
    }

    /**
     * @return Returns the verbosity.
     */
    public int getVerbosity()
    {
        return mVerbosity;
    }
    
    /**
     * @return Returns the default verbosity.
     */
    public int getDefaultVerbosity()
    {
        return VERBOSITY_ALL;
    }
    
    /**
     * @param verbosity The verbosity to set.
     */
    public void setVerbosity(int verbosity)
    {
        mVerbosity = verbosity;
    }
    
    /**
     * @return Returns the default instructions.
     */
    public ArrayList getDefaultInstructions()
    {
        return new ArrayList();
    }
    
    /**
     * @return Returns the instructions.
     */
    public ArrayList getInstructions()
    {
        return (mInstructions !=null?new ArrayList(mInstructions):new ArrayList());
    }

    /**
     * @param instructions The instructions to set.
     */
    public void setInstructions(ArrayList instructions)
    {
        mInstructions = (instructions==null?new ArrayList():instructions);
    }

    /**
     * @return Returns the default symbols.
     */
    public Properties getDefaultSymbols()
    {
        return new Properties();
    }
    
    private Properties createPropertiesCopy(Properties properties)
    {
        Properties copy = new Properties();
        if(properties != null) {
            copy.putAll(properties);
        }
        return copy;
    }
    
    /**
     * @return Returns the symbols.
     */
    public Properties getSymbols()
    {
        return createPropertiesCopy(mSymbols);
    }

    /**
     * @param symbols The symbols to set.
     */
    public void setSymbols(Properties symbols)
    {
        if(mSymbols !=null && mOldSymbols == null) {
            mOldSymbols = mSymbols;
        }
        mSymbols = (symbols==null?new Properties():symbols);
    }
}
