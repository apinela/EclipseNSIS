/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.lang;

public class NSISLanguage
{
    private String mName;
    private String mDisplayName;
    private int mLangId;
    
    /**
     * @param name
     * @param displayName
     * @param codePage
     * @param locale
     */
    NSISLanguage(String name, String displayName, int langId)
    {
        mName = name;
        mDisplayName = displayName;
        mLangId = langId;
    }

    /**
     * @return Returns the codePage.
     */
    public int getLangId()
    {
        return mLangId;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return mDisplayName;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }
    
    public String toString()
    {
        return getDisplayName();
    }
}
