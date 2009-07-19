/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.util.List;

class MakeNSISDelegateU extends AbstractMakeNSISDelegate
{
    static {
        System.loadLibrary("MakeNSISRunnerU"); //$NON-NLS-1$
    }

    public boolean isUnicode()
    {
        return true;
    }

    @Override
	protected native long init();

    @Override
	protected native void destroy();

    public native void reset();

    public native List<String> getErrors();

    public native String getOutputFileName();

    public native String getScriptFileName();

    public native List<String> getWarnings();
}
