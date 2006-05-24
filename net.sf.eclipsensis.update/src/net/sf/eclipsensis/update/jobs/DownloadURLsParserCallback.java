/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.util.Common;

class DownloadURLsParserCallback extends ParserCallback
{
    private static final String ID_MIRRORS = "mirrors"; //$NON-NLS-1$
    
    private static final int TD_NONE = -1;
    private static int COUNT=0;
    private static final int TD_HOST = COUNT++;
    private static final int TD_LOCATION = COUNT++;
    private static final int TD_CONTINENT = COUNT++;
    private static final int TD_DOWNLOAD = COUNT++;
    
    private Pattern mPattern;
    private boolean mSearching = true;
    private boolean mInTD = false;
    private int mExpectedTD = TD_NONE;
    private String[] mCurrentSite  = null;
    private List mSites = new ArrayList();
    
    public DownloadURLsParserCallback(String version)
    {
        mPattern = Pattern.compile(new StringBuffer("/nsis/nsis\\-").append( //$NON-NLS-1$
                        version.replaceAll("\\.","\\.")).append( //$NON-NLS-1$ //$NON-NLS-2$
                        "\\-setup\\.exe\\?use\\_mirror=(\\w+)").toString(),Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    }

    public void handleEndTag(Tag t, int pos)
    {
        if (mSearching) {
            if(t.equals(Tag.TABLE)) {
                mExpectedTD = TD_NONE;
            }
            else if(t.equals(Tag.TD) && mExpectedTD != TD_NONE) {
                mExpectedTD = ++mExpectedTD % (COUNT);
                mInTD = false;
                if(mExpectedTD == 0 && mCurrentSite != null) {
                    boolean canAdd = true;
                    for (int i = 0; i < mCurrentSite.length; i++) {
                        if(Common.isEmpty(mCurrentSite[i])) {
                            canAdd = false;
                            break;
                        }
                    }
                    if(canAdd) {
                        mSites.add(mCurrentSite);
                    }
                }
            }
        }
    }

    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if (mSearching && mInTD) {
            if(t.equals(Tag.IMG) && mExpectedTD == TD_HOST) {
                if(a.isDefined(Attribute.SRC)) {
                    mCurrentSite[TD_HOST] = (String)a.getAttribute(Attribute.SRC);
                }
            }
        }
    }

    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if (mSearching) {
            if(t.equals(Tag.TABLE)) {
                if(a.isDefined(Attribute.ID)) {
                    if(Common.stringsAreEqual(ID_MIRRORS,(String)a.getAttribute(Attribute.ID),true)) {
                        mExpectedTD = TD_HOST;
                    }
                }
            }
            else if(t.equals(Tag.TR)) {
                mCurrentSite = new String[COUNT];
            }
            else if(t.equals(Tag.TD) && mExpectedTD != TD_NONE) {
                if(a.isDefined(Attribute.COLSPAN)) {
                    mExpectedTD = TD_NONE;
                    mSearching = false;
                }
                else {
                    mInTD = true;
                }
            }
            else if(mInTD && mExpectedTD == TD_DOWNLOAD && t.equals(Tag.A)) {
                if(a.isDefined(Attribute.HREF)) {
                    String url = (String)a.getAttribute(Attribute.HREF);
                    Matcher matcher = mPattern.matcher(url);
                    if(matcher.matches()) {
                        mCurrentSite[TD_DOWNLOAD] = matcher.group(1);
                    }
                }
            }
        }
    }

    public void handleText(char[] data, int pos)
    {
        if (mSearching && mInTD) {
            if(mExpectedTD == TD_LOCATION || mExpectedTD == TD_CONTINENT) {
                mCurrentSite[mExpectedTD] = new String(data);
            }
        }
    }

    public List getSites()
    {
        return mSites;
    }

}