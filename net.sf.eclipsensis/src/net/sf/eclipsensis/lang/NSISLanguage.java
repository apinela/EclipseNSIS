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

import java.io.*;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.Common;

public class NSISLanguage implements Serializable
{
	private static final long serialVersionUID = -3444530357264653581L;

    private String mName;
    private String mDisplayName;
    private int mLangId;
    private String mLangDef;

    private transient Map mLangStrings = null;

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
        return mName.hashCode() | mLangId << 16;
    }

    public boolean equals(Object o)
    {
        if(o instanceof NSISLanguage) {
            NSISLanguage language = (NSISLanguage)o;
            return (mName.equals(language.mName) && mLangId == language.mLangId);
        }
        return false;
    }

    public synchronized String getLangString(String key)
    {
        String nsisHome = NSISPreferences.INSTANCE.getNSISHome();
        if(!Common.isEmpty(nsisHome)) {
            if(mLangStrings == null) {
                mLangStrings = new CaseInsensitiveMap();
                File langFile = new File(new File(nsisHome,INSISConstants.LANGUAGE_FILES_LOCATION),mName+INSISConstants.LANGUAGE_FILES_EXTENSION);
                if(langFile.exists()) {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(langFile));
                        String line = br.readLine();
                        while(line != null) {
                            line = line.trim();
                            if(line.charAt(0) == '#') {
                                line = line.substring(1).trim();
                                if(line.charAt(0) == '^') {
                                    String name = line;
                                    if(line != null) {
                                        mLangStrings.put(name,line);
                                    }
                                }
                            }
                            line = br.readLine();
                        }
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    finally {
                        Common.closeIO(br);
                    }
                }
            }
            return (String)mLangStrings.get(key);
        }
        return null;
    }
}
