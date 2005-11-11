/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import org.eclipse.core.runtime.IPath;

public class NSISScriptProblem
{
    public static final int TYPE_WARNING=0;
    public static final int TYPE_ERROR=1;

    private IPath mPath;
    private int mType;
    private String mText;
    private int mLine;

    public NSISScriptProblem(IPath path, int type, String text)
    {
       this(path, type, text, 1);
    }

    public NSISScriptProblem(IPath path, int type, String text, int line)
    {
        mPath = path;
        mType = type;
        mText = text;
        mLine = line;
    }

    public int getLine()
    {
        return mLine;
    }

    public Object getPath()
    {
        return mPath;
    }

    public String getText()
    {
        return mText;
    }

    public int getType()
    {
        return mType;
    }
}
