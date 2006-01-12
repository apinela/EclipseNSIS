/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

public class INIProblem
{
    public static final int TYPE_WARNING=0;
    public static final int TYPE_ERROR=1;

    private int mLine;
    private String mMessage;
    private int mType;

    INIProblem(int line, String message, int type)
    {
        super();
        mLine = line;
        mMessage = message;
        mType = type;
    }

    public int getLine()
    {
        return mLine;
    }

    public int getType()
    {
        return mType;
    }

    public String getMessage()
    {
        return mMessage;
    }
}
