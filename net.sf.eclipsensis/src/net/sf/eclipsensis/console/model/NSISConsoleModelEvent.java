/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console.model;

import net.sf.eclipsensis.console.NSISConsoleLine;

import org.eclipse.swt.widgets.Event;

/**
 * @deprecated
 */
public class NSISConsoleModelEvent extends Event
{
    public static final int APPEND=0;
    public static final int CLEAR=1;

    private int mType;
    private NSISConsoleLine mLine;

    /**
     * @param type
     * @param line
     */
    public NSISConsoleModelEvent(int type, NSISConsoleLine line)
    {
        super();
        mType = type;
        mLine = line;
    }
    /**
     * @return Returns the line.
     */
    public NSISConsoleLine getLine()
    {
        return mLine;
    }
    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return mType;
    }
}
