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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.*;

public class NSISHelpIndex implements Serializable
{
    private static final long serialVersionUID = 7257547506234414246L;
    
    private static final Comparator cIndexIndexComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            int[] a = (int[])o1;
            int[] b = (int[])o2;
            
            return a[0]-b[0];
        }
    };
    private static final Comparator cIndexEntryComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            return getString(o1).compareTo(getString(o2));
        }
        
        private String getString(Object o)
        {
            if(o instanceof String) {
                return (String)o;
            }
            else if(o instanceof NSISHelpIndexEntry) {
                return ((NSISHelpIndexEntry)o).getSortKey();
            }
            return null;
        }
    };
    
    private List mEntries = null;
    
    private transient Map mEntryMap = new CaseInsensitiveMap();
    private transient Map mTitlemap = new CaseInsensitiveMap();
    private int[][] mIndexIndex = null;

    private String getTitle(String url)
    {
        int n = url.lastIndexOf('#');
        if(n > 0) {
            url = url.substring(0,n);
        }
        String title = ""; //$NON-NLS-1$
        if(mTitlemap.containsKey(url)) {
            title = (String)mTitlemap.get(url);
        }
        else {
            Reader r = null;
            HTMLTitleParserCallback callback = null;
            try {
                URLConnection conn = new URL(url).openConnection();
                r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                callback = new HTMLTitleParserCallback(r);
                NSISHelpURLProvider.HTML_PARSER.parse(r,callback,true);
            }
            catch (IOException e) {
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            finally {
                if(callback != null) {
                    title = callback.getTitle();
                }
                IOUtility.closeIO(r);
            }
            mTitlemap.put(url,title);
        }
        return title;
    }
    
    NSISHelpIndex()
    {
    }
    
    void addEntry(String name, String url)
    {
        NSISHelpIndexEntry entry = (NSISHelpIndexEntry)mEntryMap.get(name);
        if(entry == null) {
            mEntryMap.put(name, new NSISHelpIndexEntry(name, url));
        }
        else {
            entry.addURL(url);
        }
    }

    public NSISHelpIndexEntry findEntry(String name)
    {
        if(name != null && name.length() > 0 && mIndexIndex != null) {
            name = name.toLowerCase();
            int[] key = {name.charAt(0),0};
            int n = Arrays.binarySearch(mIndexIndex,key,cIndexIndexComparator);
            if(n < 0) {
                n = -n-1;
                if(n >= mIndexIndex.length) {
                    return null;
                }
                if(n == 0) {
                    return (NSISHelpIndexEntry)mEntries.get(0);
                }
                else {
                    return (NSISHelpIndexEntry)mEntries.get(mIndexIndex[n][1]-1);
                }
            }
            int m = Collections.binarySearch(mEntries.subList(mIndexIndex[n][1],
                                                          (n < (mIndexIndex.length-1)?mIndexIndex[n+1][1]:mEntries.size())),
                                         name, cIndexEntryComparator);
            if(m < 0) {
                m = mIndexIndex[n][1]-m-1;
                NSISHelpIndexEntry entry = (NSISHelpIndexEntry)mEntries.get(m);
                if(!entry.getSortKey().startsWith(name)) {
                    if(m > mIndexIndex[n][1]) {
                        m--;
                    }
                    entry = (NSISHelpIndexEntry)mEntries.get(m);
                }
                return entry; 
            }
            return (NSISHelpIndexEntry)mEntries.get(mIndexIndex[n][1]+m);
        }
        return null;
    }

    public List getEntries()
    {
        if(mEntries == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(mEntries);
    }

    void doneLoading()
    {
        if(mEntries != null) {
            mEntries.clear();
        }
        else {
            mEntries = new ArrayList();
        }
        mIndexIndex = null;
        mEntries.addAll(mEntryMap.values());
        mEntryMap.clear();
        Collections.sort(mEntries);
        int i = 0;
        List indexIndex = new ArrayList();
        int lastChar = 0;
        for (Iterator iter = mEntries.iterator(); iter.hasNext();) {
            NSISHelpIndexEntry entry = (NSISHelpIndexEntry)iter.next();
            entry.sort();
            char c = Character.toLowerCase(entry.getName().charAt(0));
            if(c > lastChar) {
                lastChar = c;
                indexIndex.add(new int[] {c,i});
            }
            i++;
        }
        mIndexIndex = (int[][])indexIndex.toArray(new int[indexIndex.size()][]);
    }

    public class NSISHelpIndexEntry implements Serializable, Comparable
    {
        private static final long serialVersionUID = 460774407714231630L;

        private String mName;
        private List mURLs = new ArrayList();
        private String mSortKey;

        private NSISHelpIndexEntry(String name, String url)
        {
            mName = name;
            mURLs.add(new NSISHelpIndexURL(url));
            mSortKey = name.toLowerCase();
        }

        private void addURL(String url)
        {
            if(mURLs.size() == 1) {
                NSISHelpIndexURL url2 = (NSISHelpIndexURL)mURLs.get(0);
                url2.setLocation(getTitle(url2.getURL()));
            }
            mURLs.add(new NSISHelpIndexURL(url,getTitle(url)));
        }

        public String getName()
        {
            return mName;
        }

        private String getSortKey()
        {
            return mSortKey;
        }

        public List getURLs()
        {
            return Collections.unmodifiableList(mURLs);
        }

        public int compareTo(Object o)
        {
            return mSortKey.compareTo(((NSISHelpIndexEntry)o).mSortKey);
        }
        
        private void sort()
        {
            Collections.sort(mURLs);
        }

        public int hashCode()
        {
            return mSortKey.hashCode();
        }

        public String toString()
        {
            return mName;
        }
        
        public boolean equals(Object other)
        {
            if(other != this) {
                if(other instanceof NSISHelpIndexEntry) {
                    return Common.stringsAreEqual(mSortKey, ((NSISHelpIndexEntry)other).mSortKey);
                }
                return false;
            }
            return true;
        }
    }
    
    public class NSISHelpIndexURL implements Serializable, Comparable
    {
        private static final long serialVersionUID = -3957228848764619499L;
        private String mURL;
        private String mLocation;

        private NSISHelpIndexURL(String url)
        {
            mURL = url;
        }

        private NSISHelpIndexURL(String url, String location)
        {
            mURL = url;
            mLocation = location;
        }

        public String getLocation()
        {
            return mLocation;
        }

        private void setLocation(String location)
        {
            mLocation = location;
        }

        public String getURL()
        {
            return mURL;
        }

        public int compareTo(Object o)
        {
            NSISHelpIndexURL url = (NSISHelpIndexURL)o;
            if(!Common.stringsAreEqual(mLocation,url.mLocation)) {
                return (mLocation != null && url.mLocation != null?mLocation.compareTo(url.mLocation):(mLocation != null?1:-1));
            }
            return 0;
        }
    }

    private class HTMLTitleParserCallback extends ParserCallback
    {
        private StringBuffer mTitle = new StringBuffer(""); //$NON-NLS-1$
        private boolean mInTitle = false;
        private Reader mReader;
        
        private HTMLTitleParserCallback(Reader reader)
        {
            mReader = reader;
        }

        public String getTitle()
        {
            return mTitle.toString();
        }

        public void handleEndTag(Tag t, int pos)
        {
            if(Tag.TITLE.equals(t) && mInTitle) {
                mInTitle = false;
                try {
                    mReader.close();
                }
                catch (IOException e) {
                }
            }
        }

        public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
        {
            if(Tag.TITLE.equals(t) && !mInTitle) {
                mInTitle = true;
            }
        }

        public void handleText(char[] data, int pos)
        {
            if(mInTitle) {
                if(mTitle.length() > 0) {
                    mTitle.append(INSISConstants.LINE_SEPARATOR);
                }
                mTitle.append(data);
            }
        }
    }
}
