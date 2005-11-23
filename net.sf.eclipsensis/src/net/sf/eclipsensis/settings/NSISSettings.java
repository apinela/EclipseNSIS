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

    public abstract String getString(String name);
    public abstract boolean getBoolean(String name);
    public abstract int getInt(String name);
    public abstract void setValue(String name, String value);
    public abstract void setValue(String name, boolean value);
    public abstract void setValue(String name, int value);
    public abstract void removeString(String name);
    public abstract void removeBoolean(String name);
    public abstract void removeInt(String name);

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
        setValue(HDRINFO,mHdrInfo);
        setValue(LICENSE, mLicense);
        setValue(NOCONFIG, mNoConfig);
        setValue(NOCD, mNoCD);
        setValue(VERBOSITY,mVerbosity);
        setValue(COMPRESSOR, mCompressor);
        setValue(SOLID_COMPRESSION, mSolidCompression);
        storeObject(SYMBOLS, mSymbols);
        storeObject(INSTRUCTIONS, mInstructions);
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
        return false;
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
    public LinkedHashMap getDefaultSymbols()
    {
        return new LinkedHashMap();
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

    public abstract void storeObject(String name, Object object);
    public abstract Object loadObject(String name);
}
