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

import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.ui.help.WorkbenchHelp;

public class NSISHelpURLProvider implements INSISConstants, INSISKeywordsListener
{
    private static final String NSIS_DOCS_HELP_FORMAT = new StringBuffer("/").append( //$NON-NLS-1$
                                    INSISConstants.PLUGIN_NAME).append("/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append(
                                    "Docs/Chapter{0}.html#{1}").toString(); //$NON-NLS-1$
    private static final Pattern NSIS_DOCS_HELP_PATTERN = Pattern.compile(new StringBuffer(".*/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append("Docs/Chapter([1-9][0-9]*).html(?:#((?:[1-9][0-9]*)(?:\\.[1-9][0-9]*)*))?").toString()); //$NON-NLS-1$
    private static final Pattern NSIS_DOCS_TOPIC_PATTERN = Pattern.compile("[a-zA-Z]+([1-9][0-9]*)(?:\\.[1-9][0-9]*)*\\.html#([1-9][0-9]*(?:\\.[1-9][0-9]*)*)"); //$NON-NLS-1$
    private static final String NSIS_CHM_HELP_FORMAT = "mk:@MSITStore:{0}::/{1}"; //$NON-NLS-1$
    private static final String NSIS_CHM_SECTION_HELP_FORMAT = "mk:@MSITStore:{0}::/Section{1}.html#{2}"; //$NON-NLS-1$
    private static final String NSIS_CHM_CHAPTER_HELP_FORMAT = "mk:@MSITStore:{0}::/Chapter{1}.html#"; //$NON-NLS-1$
    
    private static NSISHelpURLProvider cInstance = null;
    private static IEclipseNSISPluginListener cShutdownListener = new IEclipseNSISPluginListener() {
        public void stopped()
        {
            if(cInstance != null) {
                synchronized(NSISHelpProducer.class) {
                    if(cInstance != null) {
                        cInstance.dispose();
                        cInstance = null;
                    }                    
                }
            }
        }
    };

    private NSISPreferences mPreferences = NSISPreferences.getPreferences();
    
    private ParserDelegator mParserDelegator = new ParserDelegator();

    private Map mDocsHelpURLs = null;
    private Map mCHMHelpURLs = null;
    
    private ResourceBundle mBundle;
    
    public static NSISHelpURLProvider getInstance()
    {
        init();
        return cInstance;
    }
    
    /**
     * 
     */
    public static void init()
    {
        if(cInstance == null) {
            synchronized(NSISHelpURLProvider.class) {
                if(cInstance == null) {
                    cInstance = new NSISHelpURLProvider();
                    EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
                }                
            }
        }
    }

    private NSISHelpURLProvider()
    {
        try {
            mBundle = ResourceBundle.getBundle(NSISHelpURLProvider.class.getName());
        } 
        catch (MissingResourceException x) {
            mBundle = null;
        }
        loadHelpURLs();
        NSISKeywords.addKeywordsListener(this);
    }
    
    public void dispose()
    {
        NSISKeywords.removeKeywordsListener(this);
    }
    
    private void loadHelpURLs()
    {
        mDocsHelpURLs = null;
        mCHMHelpURLs = null;
        String home = mPreferences.getNSISHome();
        if(!Common.isEmpty(home)) {
            File htmlHelpFile = new File(home,NSIS_CHM_HELP_FILE);
            if(htmlHelpFile.exists()) {
                File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
                File cacheFile = new File(stateLocation,getClass().getName()+".HelpURLs.ser"); //$NON-NLS-1$
                long cacheTimeStamp = 0;
                if(cacheFile.exists()) {
                    cacheTimeStamp = cacheFile.lastModified();
                }
                
                long htmlHelpTimeStamp = htmlHelpFile.lastModified();
                if(htmlHelpTimeStamp != cacheTimeStamp) {
                    if(cacheFile.exists()) {
                        cacheFile.delete();
                    }

                    Map topicMap = new CaseInsensitiveMap();
                    
                    String[] mappedHelpTopics = Common.loadArrayProperty(mBundle, "mapped.help.topics"); //$NON-NLS-1$
                    if(!Common.isEmptyArray(mappedHelpTopics)) {
                        for (int i = 0; i < mappedHelpTopics.length; i++) {
                            String[] keywords = Common.loadArrayProperty(mBundle,mappedHelpTopics[i]);
                            if(!Common.isEmptyArray(keywords)) {
                                ArrayList list = new ArrayList();
                                for (int j = 0; j < keywords.length; j++) {
                                    keywords[j] = NSISKeywords.getKeyword(keywords[j]);
                                    if(NSISKeywords.isValidKeyword(keywords[j])) {
                                        list.add(keywords[j]);
                                    }
                                }
                                topicMap.put(mappedHelpTopics[i],list);
                            }
                        }
                    }
                    String temp = WinAPI.ExtractHtmlHelpTOC(htmlHelpFile.getAbsolutePath(),stateLocation.getAbsolutePath());
                    if(!Common.isEmpty(temp)) {
                        File tocFile = new File(temp);
                        if(tocFile.exists()) {
                            try {
                                NSISHelpTOCParserCallback parserCallback = new NSISHelpTOCParserCallback();
                                parserCallback.setTopicMap(topicMap);
                                mParserDelegator.parse(new FileReader(tocFile), parserCallback, false);
                                mDocsHelpURLs = buildDocsHelpURLs(parserCallback.getKeywordHelpMap());
                                mCHMHelpURLs = buildCHMHelpURLs(htmlHelpFile.getAbsolutePath(),parserCallback.getKeywordHelpMap());
                                Common.writeObject(cacheFile,new Object[]{mDocsHelpURLs,mCHMHelpURLs});
                                cacheFile.setLastModified(htmlHelpTimeStamp);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            tocFile.delete();
                        }
                    }
                }
                else {
                    Object obj = null;
                    try {
                        obj = Common.readObject(cacheFile);
                    }
                    catch (Exception e) {
                        obj = null;
                        e.printStackTrace();
                    }
                    if(obj != null && obj.getClass().isArray()) {
                        mDocsHelpURLs = (Map)((Object[])obj)[0];
                        mCHMHelpURLs = (Map)((Object[])obj)[1];
                    }
                }
            }
        }
    }
    
    public void keywordsChanged()
    {
        loadHelpURLs();
    }
    
    public String convertDocHelpURLToCHMHelpURL(String docHelpURL)
    {
        String chmHelpURL = null;
        if(!Common.isEmpty(docHelpURL)) {
            String home = mPreferences.getNSISHome();
            if(!Common.isEmpty(home)) {
                File htmlHelpFile = new File(home,NSIS_CHM_HELP_FILE);
                if(htmlHelpFile.exists()) {
                    Matcher matcher = NSIS_DOCS_HELP_PATTERN.matcher(docHelpURL);
                    if(matcher.matches()) {
                        if(matcher.groupCount() == 2) {
                            String chapter=matcher.group(1);
                            String hash = matcher.group(2);
                            if(hash == null) {
                                chmHelpURL = MessageFormat.format(NSIS_CHM_CHAPTER_HELP_FORMAT,new String[]{htmlHelpFile.getAbsolutePath(),chapter});
                            }
                            else {
                                String[] temp = Common.tokenize(hash,'.');
                                if(temp.length >= 2) {
                                    String section = new StringBuffer(temp[0]).append(".").append(temp[1]).toString(); //$NON-NLS-1$
                                    chmHelpURL = MessageFormat.format(NSIS_CHM_SECTION_HELP_FORMAT,new String[]{htmlHelpFile.getAbsolutePath(),
                                                                                                                section,hash});
                                }
                                else {
                                    chmHelpURL = MessageFormat.format(NSIS_CHM_CHAPTER_HELP_FORMAT,new String[]{htmlHelpFile.getAbsolutePath(),chapter});
                                }
                            }
                        }
                    }
                }
            }
        }
        return chmHelpURL;
    }

    private Map buildDocsHelpURLs(Map keywordHelpMap)
    {
        Map urlsMap = new CaseInsensitiveMap();
        if(!Common.isEmptyMap(keywordHelpMap)) {
            MessageFormat mf = new MessageFormat(NSIS_DOCS_HELP_FORMAT);
            StringBuffer buf = new StringBuffer();
            String[] args = new String[]{null,null};
            for (Iterator iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                buf.delete(0,buf.length());
                String keyword = (String)iter.next();
                String location = (String)keywordHelpMap.get(keyword);
                Matcher matcher = NSIS_DOCS_TOPIC_PATTERN.matcher(location);
                if(matcher.matches() && matcher.groupCount() == args.length) {
                    args[0] = matcher.group(1);
                    args[1] = matcher.group(2);
                    urlsMap.put(keyword,mf.format(args,buf,null).toString());
                }
            }
        }
        return urlsMap;
    }
    
    private Map buildCHMHelpURLs(String htmlHelpFile, Map keywordHelpMap)
    {
        Map urlsMap = new CaseInsensitiveMap();
        if(!Common.isEmptyMap(keywordHelpMap)) {
            MessageFormat mf = new MessageFormat(NSIS_CHM_HELP_FORMAT);
            StringBuffer buf = new StringBuffer();
            String[] args = new String[]{htmlHelpFile,null};
            for (Iterator iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                buf.delete(0,buf.length());
                String keyword = (String)iter.next();
                args[1] = (String)keywordHelpMap.get(keyword);
                urlsMap.put(keyword,mf.format(args,buf,null).toString());
            }
        }
        return urlsMap;
    }
    
    private String getHelpURL(String keyWord, boolean useDocHelp)
    {
        if(!Common.isEmpty(keyWord)) {
            if(useDocHelp) {
                if(mDocsHelpURLs != null) {
                    return (String)mDocsHelpURLs.get(keyWord);
                }
            }
            else {
                if(mCHMHelpURLs != null) {
                    return (String)mCHMHelpURLs.get(keyWord);
                }
            }
        }
        return null;
    }
    
    public void showHelpURL(String keyword)
    {
        String url = null;
        if(mPreferences.isUseDocsHelp()) {
            url = getHelpURL(keyword, true);
        }
        if(Common.isEmpty(url)) {
            url = getHelpURL(keyword, false);
            if(!Common.isEmpty(url)) {
                openCHMHelpURL(url);
            }
        }
        else {
            WorkbenchHelp.displayHelpResource(url);
        }
    }

    /**
     * @param url
     */
    public void openCHMHelpURL(String url)
    {
        WinAPI.HtmlHelp(WinAPI.GetDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
    }
}
