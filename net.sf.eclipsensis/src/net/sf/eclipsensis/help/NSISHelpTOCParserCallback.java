/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;

import net.sf.eclipsensis.util.CaseInsensitiveMap;

public class NSISHelpTOCParserCallback extends HTMLEditorKit.ParserCallback
{
    private static final String ATTR_VALUE_TEXT_SITEMAP="text/sitemap";
    private static final String ATTR_VALUE_LOCAL="Local";
    private static final String ATTR_VALUE_NAME="Name";
    
    private Map mTopicMap = null;
    private Map mKeywordHelpMap = new CaseInsensitiveMap();
    private boolean mCanProcess = false;
    private String mLocation = null;
    private String mKeyword = null;
    
    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndTag(javax.swing.text.html.HTML.Tag, int)
     */
    public void handleEndTag(Tag t, int pos)
    {
        if(t.equals(Tag.OBJECT) && mCanProcess) {
            if(mLocation != null && mKeyword != null) {
                if(mTopicMap.containsKey(mKeyword)) {
                    List keywords = (List)mTopicMap.get(mKeyword);
                    for (Iterator iter = keywords.iterator(); iter.hasNext();) {
                        String keyword = (String)iter.next();
                        if(NSISKeywords.isValidKeyword(keyword)) {
                            mKeywordHelpMap.put(keyword, mLocation);
                        }
                    }
                }
                else if(NSISKeywords.isValidKeyword(mKeyword)) {
                    mKeywordHelpMap.put(mKeyword,mLocation);
                }
            }
            mCanProcess = false;
            mLocation = null;
            mKeyword = null;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleSimpleTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
     */
    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(mCanProcess && t.equals(Tag.PARAM)) {
            if(a.isDefined(Attribute.NAME) && a.isDefined(Attribute.VALUE)) {
                String name = (String)a.getAttribute(Attribute.NAME);
                String value = (String)a.getAttribute(Attribute.VALUE);
                if(ATTR_VALUE_LOCAL.equalsIgnoreCase(name)) {
                    mLocation = value;
                }
                else if(ATTR_VALUE_NAME.equalsIgnoreCase(name)) {
                    mKeyword = value;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleStartTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
     */
    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(t.equals(Tag.OBJECT)) {
            if(a.isDefined(Attribute.TYPE)) {
                if(ATTR_VALUE_TEXT_SITEMAP.equalsIgnoreCase((String)a.getAttribute(Attribute.TYPE))) {
                    mCanProcess = true;
                }
            }
        }
    }

    /**
     * @param topicMap The tOCKeywordsMap to set.
     */
    public void setTopicMap(Map topicMap)
    {
        mTopicMap = topicMap;
    }
    
    /**
     * @return Returns the keywordMap.
     */
    public Map getKeywordHelpMap()
    {
        return mKeywordHelpMap;
    }
}
