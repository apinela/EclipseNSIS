/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.File;

import org.eclipse.jface.text.Document;

public class FileDocument extends Document
{
    private File mFile;

    public FileDocument(File file)
    {
        super(new String(IOUtility.loadContentFromFile(file)));
        mFile = file;
    }

    public File getFile()
    {
        return mFile;
    }
}
