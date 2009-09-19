/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.io.File;
import java.util.Properties;

public class NSISExe
{
    private File mFile;
    private Version mVersion;
    private Properties mDefinedSymbols;

    public NSISExe(File file, Version version, Properties definedSymbols)
    {
        mFile = file;
        mVersion = version;
        mDefinedSymbols = definedSymbols;
    }

    public File getFile()
    {
        return mFile;
    }

    public Version getVersion()
    {
        return mVersion;
    }

    public Properties getDefinedSymbols()
    {
        return mDefinedSymbols;
    }
}
