/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import org.eclipse.core.resources.IFile;

public class NSISConsoleLine
{
    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    private String mText = null;
    private int mType = INFO;
    private IFile mFile = null;
    private int mLineNum = 0;

    public static NSISConsoleLine info(String text)
    {
        return new NSISConsoleLine(text,INFO);
    }

    public static NSISConsoleLine warning(String text)
    {
        return new NSISConsoleLine(text,WARNING);
    }

    public static NSISConsoleLine error(String text)
    {
        return new NSISConsoleLine(text,ERROR);
    }
    
    /**
     * @param string
     */
    public NSISConsoleLine(String text, int type)
    {
        mText = text;
        mType = type;
    }

    public String toString()
    {
        return mText;
    }
    
    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return mType;
    }
    /**
     * @return Returns the file.
     */
    public IFile getFile()
    {
        return mFile;
    }
    /**
     * @param file The file to set.
     */
    public void setFile(IFile file)
    {
        mFile = file;
    }
    /**
     * @return Returns the lineNum.
     */
    public int getLineNum()
    {
        return mLineNum;
    }
    /**
     * @param lineNum The lineNum to set.
     */
    public void setLineNum(int lineNum)
    {
        mLineNum = lineNum;
    }
}
