/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sunil.Kamath
 */
public class MakeNSISResults
{
    public static final int RETURN_SUCCESS = 0;
    
    private int mReturnCode = RETURN_SUCCESS;
    private String mOutputFileName = null;
    private String mScriptFileName = null;
    private ArrayList mErrors = null;
    private ArrayList mWarnings = null;
    
    /**
     * @return Returns the errors.
     */
    public List getError()
    {
        return (mErrors==null?null:Collections.unmodifiableList(mErrors));
    }
    /**
     * @param errors The error to set.
     */
    void setErrors(ArrayList errors)
    {
        mErrors = errors;
    }
    /**
     * @return Returns the outputFileName.
     */
    public String getOutputFileName()
    {
        return mOutputFileName;
    }
    /**
     * @param outputFileName The outputFileName to set.
     */
    void setOutputFileName(String outputFileName)
    {
        mOutputFileName = outputFileName;
    }
    /**
     * @return Returns the scriptFileName.
     */
    public String getScriptFileName()
    {
        return mScriptFileName;
    }
    /**
     * @param scriptFileName The scriptFileName to set.
     */
    void setScriptFileName(String scriptFileName)
    {
        mScriptFileName = scriptFileName;
    }
    /**
     * @return Returns the warnings.
     */
    public List getWarnings()
    {
        return (mWarnings==null?null:Collections.unmodifiableList(mWarnings));
    }
    /**
     * @param warnings The warnings to set.
     */
    void setWarnings(ArrayList warnings)
    {
        mWarnings = warnings;
    }
    
    /**
     * @return Returns the returnCode.
     */
    public int getReturnCode()
    {
        return mReturnCode;
    }
    
    /**
     * @param returnCode The returnCode to set.
     */
    void setReturnCode(int returnCode)
    {
        mReturnCode = returnCode;
    }
}
