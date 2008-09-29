/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.util.ArrayList;

class MakeNSISDelegate extends AbstractMakeNSISDelegate
{
    static {
        System.loadLibrary("MakeNSISRunner");
    }

    public boolean isUnicode()
    {
        return false;
    }

    protected native long init();

    protected native void destroy();

    public native void reset();

    public native ArrayList getErrors();

    public native String getOutputFileName();

    public native String getScriptFileName();

    public native ArrayList getWarnings();
}