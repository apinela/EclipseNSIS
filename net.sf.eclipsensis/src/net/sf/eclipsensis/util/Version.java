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

import java.util.Arrays;
import java.util.StringTokenizer;

public class Version implements Comparable
{
    public static final int MAJOR = 0;
    public static final int MINOR = 1;
    public static final int MICRO = 2;
    public static final int BUILD = 3;

    private int[] mNumbers = null;
    private String[] mQualifiers = null;
    private String mDisplayText = null;
    public static final Version EMPTY_VERSION = new Version("0.0"); //$NON-NLS-1$

    public Version(String version)
    {
        this(version,"."); //$NON-NLS-1$
    }

    public Version(String version, String separators)
    {
        mDisplayText = version;
        StringTokenizer st = new StringTokenizer(mDisplayText,separators);
        mNumbers = new int[st.countTokens()];
        mQualifiers = new String[mNumbers.length];
        Arrays.fill(mNumbers,0);
        for(int i=0; i<mNumbers.length; i++) {
            outer: {
                String token = st.nextToken();
                char[] chars = token.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    if(!Character.isDigit(chars[j])) {
                        mNumbers[i] = (j>0?Integer.parseInt(token.substring(0,j)):0);
                        mQualifiers[i] = token.substring(j);
                        break outer;
                    }
                }
                mNumbers[i] = Integer.parseInt(token);
                mQualifiers[i] = null;
            }
        }
    }

    public Version(Version version)
    {
        mNumbers = (version.mNumbers == null?null:(int[])version.mNumbers.clone());
        mQualifiers = (version.mQualifiers == null?null:(String[])version.mQualifiers.clone());
        mDisplayText = version.mDisplayText;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if(obj instanceof Version) {
            Version v2 = (Version)obj;
            if(mNumbers.length == v2.mNumbers.length) {
                for (int i = 0; i < mNumbers.length; i++) {
                    if(mNumbers[i] != v2.mNumbers[i]) {
                        return false;
                    }
                    if(!Common.stringsAreEqual(mQualifiers[i],v2.mQualifiers[i],true)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int hashCode = 0;
        for (int i = 0; i < mNumbers.length; i++) {
            hashCode += mNumbers[i];
            if(mQualifiers[i] != null) {
                hashCode += mQualifiers[i].hashCode();
            }
        }
        return hashCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return mDisplayText;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        Version v2 = (Version)o;
        if(!equals(v2)) {
            int max = Math.max(mNumbers.length,v2.mNumbers.length);
            for(int i=0; i<max; i++) {
                int n1 = (i >= mNumbers.length?0:mNumbers[i]);
                int n2 = (i >= v2.mNumbers.length?0:v2.mNumbers[i]);
                int diff = n1 - n2;
                if(diff == 0) {
                    String q1 = (i >= mQualifiers.length?null:mQualifiers[i]);
                    String q2 = (i >= v2.mQualifiers.length?null:v2.mQualifiers[i]);
                    if(q1 != null && q2 == null) {
                        return -1;
                    }
                    else if(q1 == null && q2 != null) {
                        return 1;
                    }
                    else if(q1 != null && q2 != null) {
                        if(!q1.equalsIgnoreCase(q2)) {
                            return q1.compareTo(q2);
                        }
                    }
                    continue;
                }
                else {
                    return diff;
                }
            }
        }
        return 0;
    }

    public int getNumber(int index)
    {
        if(mNumbers.length > index) {
            return mNumbers[index];
        }
        return 0;
    }

    public String getQualifier(int index)
    {
        if(mQualifiers.length > index) {
            return mQualifiers[index];
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * @return Returns the numbers.
     */
    public int[] getNumbers()
    {
        return (int[])mNumbers.clone();
    }

    /**
     * @return Returns the qualifiers.
     */
    public String[] getQualifiers()
    {
        return (String[])mQualifiers.clone();
    }

    public void setDisplayText(String displayText)
    {
        mDisplayText = displayText;
    }
}
