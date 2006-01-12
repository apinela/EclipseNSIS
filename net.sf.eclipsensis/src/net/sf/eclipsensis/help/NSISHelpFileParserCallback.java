/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

public class NSISHelpFileParserCallback extends ParserCallback
{
    private static final Set HEADINGS = new HashSet();
    
    private Collection mURLs;
    private Map mURLContentsMap;
    private String mPrefix;
    private StringBuffer mBuffer = new StringBuffer(""); //$NON-NLS-1$
    private boolean mCollecting = false;
    private boolean mHeading = false;
    private String mAnchor;
    
    static {
        HEADINGS.add(Tag.H1);
        HEADINGS.add(Tag.H2);
        HEADINGS.add(Tag.H3);
        HEADINGS.add(Tag.H4);
        HEADINGS.add(Tag.H5);
        HEADINGS.add(Tag.H6);
    }
    
    public NSISHelpFileParserCallback(String prefix, Collection coll, Map map)
    {
        super();
        mPrefix = prefix;
        mURLs = coll;
        mURLContentsMap = map;
    }

    public void handleEndTag(Tag t, int pos)
    {
        if(mCollecting) {
            if(t.equals(Tag.A) && mBuffer.length() == NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX.length()) {
                return;
            }
            if(HEADINGS.contains(t)) {
                mBuffer.append("</p>"); //$NON-NLS-1$
                mHeading = false;
            }
            else if(t.equals(Tag.A)) {
                mBuffer.append("</span>"); //$NON-NLS-1$
            }
            else {
                mBuffer.append("</").append(t).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(mCollecting) {
            if(HEADINGS.contains(t)) {
                mBuffer.append("<p class=\"heading\">"); //$NON-NLS-1$
                mHeading = true;
            }
            else if(t.equals(Tag.A)) {
                mBuffer.append("<span class=\"link\">"); //$NON-NLS-1$
            }
            else {
                mBuffer.append("<").append(t); //$NON-NLS-1$
                if(a != null && a.getAttributeCount() > 0) {
                    for(Enumeration e = a.getAttributeNames(); e.hasMoreElements(); ) {
                        Object name = e.nextElement();
                        Object value = a.getAttribute(name);
                        mBuffer.append(" ").append(name).append("=\"").append(value).append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                mBuffer.append(">"); //$NON-NLS-1$
            }
        }
    }

    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(t.equals(Tag.A)) {
            if(a != null && a.isDefined(Attribute.NAME)) {
                if(mCollecting) {
                    mBuffer.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_SUFFIX);
                    mURLContentsMap.put(mAnchor,mBuffer.toString());
                    mBuffer.setLength(0);
                    mAnchor = null;
                    mCollecting = false;
                }
                mAnchor = mPrefix+a.getAttribute(Attribute.NAME);
                if(mURLs.contains(mAnchor)) {
                    mCollecting = true;
                    mBuffer.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX);
                    return;
                }
                else {
                    mAnchor = null;
                }
            }
        }
        handleSimpleTag(t,a,pos);
    }

    public void handleText(char[] data, int pos)
    {
        if(mCollecting) {
            boolean isNewLine = false; //For some reason CR is being converted to NL by the parser. 
                                       //So one needs to be dropped.
            boolean found = false;
            for (int i = 0; i < data.length; i++) {
                if(isNewLine) {
                    isNewLine = false;
                    if(data[i] == '\n') {
                        continue;
                    }
                }
                isNewLine = (data[i] == '\n');
                if(mHeading && !found) {
                    found = (data[i]==' ');
                    continue;
                }
                mBuffer.append(data[i]);
            }
        }
    }
}
