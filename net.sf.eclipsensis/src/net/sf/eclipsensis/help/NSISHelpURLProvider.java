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
    private static final String NSIS_DOCS_HELP_FORMAT = "/"+INSISConstants.PLUGIN_NAME+"/"+INSISConstants.NSIS_HELP_PREFIX+"Docs/Chapter{0}.html#{1}"; //$NON-NLS-1$
    private static final Pattern NSIS_DOCS_TOPIC_PATTERN = Pattern.compile("[a-zA-Z]+([1-9][0-9]*)(?:\\.[1-9][0-9]*)*\\.html#([1-9][0-9]*(?:\\.[1-9][0-9]*)*)");
    private static final String NSIS_CHM_HELP_FORMAT = "mk:@MSITStore:{0}::/{1}"; //$NON-NLS-1$
    
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
    
    private final String mDocsHelpPrefix;
    private final String mDocsHelpSuffix;
    private final String mCHMHelpPrefix;
    private final String mCHMHelpSuffix;
    
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
        mDocsHelpPrefix = EclipseNSISPlugin.getResourceString("docs.help.prefix","Chapter"); //$NON-NLS-1$ //$NON-NLS-2$
        mDocsHelpSuffix = EclipseNSISPlugin.getResourceString("docs.help.suffix","html"); //$NON-NLS-1$ //$NON-NLS-2$
        mCHMHelpPrefix = EclipseNSISPlugin.getResourceString("chm.help.prefix","Section"); //$NON-NLS-1$ //$NON-NLS-2$
        mCHMHelpSuffix = EclipseNSISPlugin.getResourceString("chm.help.suffix","html"); //$NON-NLS-1$ //$NON-NLS-2$
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
            //File homeFile
            File htmlHelpFile = new File(home,NSIS_CHM_HELP_FILE);
            if(htmlHelpFile.exists()) {
                File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
                File cacheFile = new File(stateLocation,getClass().getName()+".HelpURLs.xml"); //$NON-NLS-1$
                long cacheTimeStamp = 0;
                if(cacheFile.exists()) {
                    cacheTimeStamp = cacheFile.lastModified();
                }
                
                long htmlHelpTimeStamp = htmlHelpFile.lastModified();
                if(htmlHelpTimeStamp != cacheTimeStamp) {
                    Map topicMap = new CaseInsensitiveMap();
                    
                    String[] mappedHelpTopics = Common.loadArrayProperty(mBundle, "mapped.help.topics");
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
                                mDocsHelpURLs = loadDocsHelpURLs(parserCallback.getKeywordHelpMap());
                                mCHMHelpURLs = loadCHMHelpURLs(htmlHelpFile.getAbsolutePath(),parserCallback.getKeywordHelpMap());
                                Common.writeObjectToXMLFile(cacheFile,new Object[]{mDocsHelpURLs,mCHMHelpURLs});
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
                        obj = Common.readObjectFromXMLFile(cacheFile);
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

    private Map loadDocsHelpURLs(Map keywordHelpMap)
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
    
    private Map loadCHMHelpURLs(String htmlHelpFile, Map keywordHelpMap)
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
                WinAPI.HtmlHelp(WinAPI.GetDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
            }
        }
        else {
            WorkbenchHelp.displayHelpResource(url);
        }
    }
}
