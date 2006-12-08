/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.io.*;
import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

class DownloadURLsParserCallback extends ParserCallback
{
    private static File cCacheFolder = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),"resources"); //$NON-NLS-1$
    private static final String PROPERTIES_FILE_NAME = "siteimages.properties"; //$NON-NLS-1$
    private static File cCacheFile = new File(cCacheFolder, PROPERTIES_FILE_NAME);
    private static final IPath cLocalPropertiesPath = new Path("/resources/"+PROPERTIES_FILE_NAME); //$NON-NLS-1$

    private static final String FORM_ACTION = "/project/downloading.php"; //$NON-NLS-1$
    private static final String USE_MIRROR = "use_mirror"; //$NON-NLS-1$
    private static final String AUTO_SELECT = "Auto-select"; //$NON-NLS-1$
    private static final String TAG_LABEL = "label"; //$NON-NLS-1$
    private static final String INPUT_RADIO = "radio"; //$NON-NLS-1$

    private boolean mDone = false;
    private boolean mInForm = false;
    private boolean mInSpan = false;
    private boolean mInLabel = false;
    private String mContinent = null;

    private String[] mCurrentSite  = null;
    private List mSites = new ArrayList();
    private Properties mImageURLs = new Properties();

    private void loadImageURLs()
    {
        InputStream is = null;
        try {
            File file = cCacheFile;
            if(!NetworkUtil.downloadLatest(NSISUpdateURLs.getSiteImagesUpdateURL(),file)) {
                file = IOUtility.ensureLatest(EclipseNSISUpdatePlugin.getDefault().getBundle(),cLocalPropertiesPath,cCacheFolder);
            }

            if(file != null && file.exists()) {
                is = new FileInputStream(file);
                mImageURLs.load(is);
            }
        }
        catch (IOException e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(is);
        }
    }

    public DownloadURLsParserCallback()
    {
        loadImageURLs();
    }

    public void handleEndTag(Tag t, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.FORM)) {
                if(mInForm) {
                    mInForm = false;
                    mDone = true;
                }
            }
            else if(t.equals(Tag.SPAN)) {
                if(mInForm) {
                    mInSpan = false;
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm) {
                    mInLabel = false;
                    if(mCurrentSite != null) {
                        if(mCurrentSite[3] != null) {
                            mSites.add(mCurrentSite);
                        }
                        mCurrentSite = null;
                    }
                }
            }
        }
    }

    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.SPAN)) {
                if(mInForm) {
                    if(mInSpan) {
                        handleEndTag(t, pos);
                    }
                    else {
                        handleStartTag(t, a, pos);
                    }
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm) {
                    if(mInLabel) {
                        handleEndTag(t, pos);
                    }
                    else {
                        handleStartTag(t, a, pos);
                    }
                }
            }
            else if(t.equals(Tag.INPUT)) {
                if(mInForm && mInLabel && mCurrentSite != null) {
                    if(a.isDefined(Attribute.TYPE)) {
                        if(INPUT_RADIO.equalsIgnoreCase((String)a.getAttribute(Attribute.TYPE))) {
                            if(a.isDefined(Attribute.NAME)) {
                                if(USE_MIRROR.equalsIgnoreCase((String)a.getAttribute(Attribute.NAME))) {
                                    if(a.isDefined(Attribute.VALUE)) {
                                        mCurrentSite[3] = (String)a.getAttribute(Attribute.VALUE);
                                        mCurrentSite[0] = mImageURLs.getProperty(mCurrentSite[3]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.FORM)) {
                if(!mInForm) {
                    if(a.isDefined(Attribute.ACTION)) {
                        String action = (String)a.getAttribute(Attribute.ACTION);
                        mInForm = FORM_ACTION.equalsIgnoreCase(action);
                    }
                }
            }
            else if(t.equals(Tag.SPAN)) {
                if(mInForm) {
                    mInSpan = true;
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm) {
                    mInLabel = true;
                    if(mContinent != null) {
                        mCurrentSite = new String[4];
                        mCurrentSite[2] = mContinent;
                    }
                }
            }
        }
    }

    public void handleText(char[] data, int pos)
    {
        if(!mDone) {
            if(mInForm) {
                String string = new String(data).trim();
                if(mInSpan) {
                    if(!AUTO_SELECT.equalsIgnoreCase(string)) {
                        mContinent = string;
                    }
                    else {
                        mContinent = null;
                    }
                }
                else if(mInLabel) {
                    if(mCurrentSite != null) {
                        mCurrentSite[1] = string;
                    }
                }
            }
        }
    }

    public List getSites()
    {
        return mSites;
    }

}