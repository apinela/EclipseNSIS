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
import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;

public abstract class NSISSettings implements INSISSettingsConstants
{
    protected static File cPluginStateLocation = EclipseNSISPlugin.getPluginStateLocation();
    
    private boolean mHdrInfo = false;
    private boolean mLicense = false;
    private boolean mNoConfig = false;
    private boolean mNoCD = false;
    private int mVerbosity = INSISPreferenceConstants.VERBOSITY_ALL;
    private int mCompressor = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private boolean mSolidCompression = false;
    private ArrayList mInstructions = null;
    private LinkedHashMap mSymbols = null;

    private int mDefaultDefaultCompressor = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private boolean mDefaultDefaultHdrInfo = false;
    private ArrayList mDefaultDefaultInstructions = new ArrayList();
    private boolean mDefaultDefaultLicense = false;
    private boolean mDefaultDefaultNoCD = false;
    private boolean mDefaultDefaultNoConfig = false;
    private boolean mDefaultDefaultSolidCompression = false;
    private LinkedHashMap mDefaultDefaultSymbols = new LinkedHashMap();
    private int mDefaultDefaultVerbosity = VERBOSITY_ALL;

    private int mDefaultCompressor;
    private boolean mDefaultHdrInfo;
    private ArrayList mDefaultInstructions;
    private boolean mDefaultLicense;
    private boolean mDefaultNoCD;
    private boolean mDefaultNoConfig;
    private boolean mDefaultSolidCompression;
    private LinkedHashMap mDefaultSymbols;
    private int mDefaultVerbosity;

    public NSISSettings()
    {
        setDefaultHdrInfo(getDefaultDefaultHdrInfo());
        setDefaultLicense(getDefaultDefaultLicense());
        setDefaultNoConfig(getDefaultDefaultNoConfig());
        setDefaultNoCD(getDefaultDefaultNoCD());
        setDefaultVerbosity(getDefaultDefaultVerbosity());
        setDefaultCompressor(getDefaultDefaultCompressor());
        setDefaultSolidCompression(getDefaultDefaultSolidCompression());
        setDefaultSymbols(getDefaultDefaultSymbols());
        setDefaultInstructions(getDefaultDefaultInstructions());
    }

    public void load()
    {
        setHdrInfo(getBoolean(HDRINFO));
        setLicense(getBoolean(LICENSE));
        setNoConfig(getBoolean(NOCONFIG));
        setNoCD(getBoolean(NOCD));
        setVerbosity(getInt(VERBOSITY));
        setCompressor(getInt(COMPRESSOR));
        setSolidCompression(getBoolean(SOLID_COMPRESSION));
        setInstructions((ArrayList)loadObject(INSTRUCTIONS));
        setSymbols((LinkedHashMap)loadObject(SYMBOLS));
    }

    public void store()
    {
        setValue(HDRINFO, getHdrInfo(), getDefaultHdrInfo());
        setValue(LICENSE, getLicense(), getDefaultLicense());
        setValue(NOCONFIG, getNoConfig(), getDefaultNoConfig());
        setValue(NOCD, getNoCD(), getDefaultNoCD());
        setValue(VERBOSITY, getVerbosity(), getDefaultVerbosity());
        setValue(COMPRESSOR, getCompressor(), getDefaultCompressor());
        setValue(SOLID_COMPRESSION, getSolidCompression(), getDefaultSolidCompression());
        storeObject(SYMBOLS, getSymbols(), getDefaultSymbols());
        storeObject(INSTRUCTIONS, getInstructions(), getDefaultInstructions());
    }
    
    public int getDefaultDefaultCompressor()
    {
        return mDefaultDefaultCompressor;
    }

    public boolean getDefaultDefaultHdrInfo()
    {
        return mDefaultDefaultHdrInfo;
    }

    public ArrayList getDefaultDefaultInstructions()
    {
        return mDefaultDefaultInstructions;
    }

    public boolean getDefaultDefaultLicense()
    {
        return mDefaultDefaultLicense;
    }

    public boolean getDefaultDefaultNoCD()
    {
        return mDefaultDefaultNoCD;
    }

    public boolean getDefaultDefaultNoConfig()
    {
        return mDefaultDefaultNoConfig;
    }

    public boolean getDefaultDefaultSolidCompression()
    {
        return mDefaultDefaultSolidCompression;
    }

    public LinkedHashMap getDefaultDefaultSymbols()
    {
        return mDefaultDefaultSymbols;
    }

    public int getDefaultDefaultVerbosity()
    {
        return mDefaultDefaultVerbosity;
    }

    protected void setValue(String name, String value, String defaultValue)
    {
        if(Common.stringsAreEqual(value, defaultValue)) {
            removeString(name);
        }
        else {
            setValue(name, value);
        }
    }
    
    protected void setValue(String name, boolean value, boolean defaultValue)
    {
        if(value == defaultValue) {
            removeBoolean(name);
        }
        else {
            setValue(name, value);
        }
    }
    
    protected void setValue(String name, int value, int defaultValue)
    {
        if(value == defaultValue) {
            removeInt(name);
        }
        else {
            setValue(name, value);
        }
    }

    protected void storeObject(String name, Object value, Object defaultValue)
    {
        if(Common.objectsAreEqual(value, defaultValue)) {
            removeObject(name);
        }
        else {
            storeObject(name, value);
        }
    }

    public boolean showStatistics()
    {
        return true;
    }

    public boolean getSolidCompression()
    {
        return mSolidCompression;
    }

    public boolean getDefaultSolidCompression()
    {
        return mDefaultSolidCompression;
    }

    public void setSolidCompression(boolean solidCompression)
    {
        mSolidCompression = solidCompression;
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
        return mDefaultCompressor;
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
        return mDefaultHdrInfo;
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
        return mDefaultLicense;
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
        return mDefaultNoCD;
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
        return mDefaultNoConfig;
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
        return mDefaultVerbosity;
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
        return mDefaultInstructions;
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
    public LinkedHashMap getDefaultSymbols()
    {
        return mDefaultSymbols;
    }

    /**
     * @return Returns the symbols.
     */
    public LinkedHashMap getSymbols()
    {
        return (mSymbols == null?new LinkedHashMap():new LinkedHashMap(mSymbols));
    }

    /**
     * @param symbols The symbols to set.
     */
    public void setSymbols(LinkedHashMap symbols)
    {
        mSymbols = (symbols==null?new LinkedHashMap():symbols);
    }

    public void setDefaultCompressor(int defaultCompressor)
    {
        mDefaultCompressor = defaultCompressor;
    }

    public void setDefaultHdrInfo(boolean defaultHdrInfo)
    {
        mDefaultHdrInfo = defaultHdrInfo;
    }

    public void setDefaultInstructions(ArrayList defaultInstructions)
    {
        mDefaultInstructions = defaultInstructions;
    }

    public void setDefaultLicense(boolean defaultLicense)
    {
        mDefaultLicense = defaultLicense;
    }

    public void setDefaultNoCD(boolean defaultNoCD)
    {
        mDefaultNoCD = defaultNoCD;
    }

    public void setDefaultNoConfig(boolean defaultNoConfig)
    {
        mDefaultNoConfig = defaultNoConfig;
    }

    public void setDefaultSolidCompression(boolean defaultSolidCompression)
    {
        mDefaultSolidCompression = defaultSolidCompression;
    }

    public void setDefaultSymbols(LinkedHashMap defaultSymbols)
    {
        mDefaultSymbols = defaultSymbols;
    }

    public void setDefaultVerbosity(int defaultVerbosity)
    {
        mDefaultVerbosity = defaultVerbosity;
    }

    public abstract String getString(String name);
    public abstract boolean getBoolean(String name);
    public abstract int getInt(String name);
    public abstract void setValue(String name, String value);
    public abstract void setValue(String name, boolean value);
    public abstract void setValue(String name, int value);
    public abstract void removeString(String name);
    public abstract void removeBoolean(String name);
    public abstract void removeInt(String name);
    public abstract void removeObject(String name);
    public abstract void storeObject(String name, Object object);
    public abstract Object loadObject(String name);
}
