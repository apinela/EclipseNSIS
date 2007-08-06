/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

public class NSISBrowserInformation
{
    private String mKeyword;
    private String mInformation;

    public NSISBrowserInformation(String keyword, String information)
    {
        mKeyword = keyword;
        mInformation = information;
    }

    public String getInformation()
    {
        return mInformation;
    }
    public String getKeyword()
    {
        return mKeyword;
    }
}
