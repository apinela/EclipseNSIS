/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.lang;

import java.io.Serializable;

public class NSISLanguage implements Serializable
{
	private static final long serialVersionUID = -3444530357264653581L;

    private String mName;
    private String mDisplayName;
    private int mLangId;
    private String mLangDef;
    
    /**
     * @param name
     * @param displayName
     * @param langId
     */
    NSISLanguage(String name, String displayName, int langId)
    {
        mName = name;
        mDisplayName = displayName;
        mLangId = langId;
        mLangDef = new StringBuffer("${LANG_").append(name.toUpperCase()).append("}").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return Returns the langId.
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
    
    /**
     * @return Returns the langDef.
     */
    public String getLangDef()
    {
        return mLangDef;
    }

    public String toString()
    {
        return getDisplayName();
    }
    
    public int hashCode()
    {
        return mLangId;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof NSISLanguage) {
            NSISLanguage language = (NSISLanguage)o;
            return mName.equals(language.mName) && (mLangId == language.mLangId);
        }
        return false;
    }
}
