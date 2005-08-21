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

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

public class NSISHelpURLProvider implements INSISConstants, INSISKeywordsListener, IEclipseNSISService
{
    public static NSISHelpURLProvider INSTANCE = null;
    
    private static final String CHMLINK_JS = "chmlink.js"; //$NON-NLS-1$
    private static final String NSIS_HELP_FORMAT = new StringBuffer("/").append( //$NON-NLS-1$
                                    INSISConstants.PLUGIN_ID).append("/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append(
                                    "Docs/{0}").toString(); //$NON-NLS-1$
    private static final Pattern NSIS_HELP_PATTERN = Pattern.compile(new StringBuffer(".*/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append("Docs/(.*)").toString()); //$NON-NLS-1$
    private static final String NSIS_CHM_HELP_FORMAT = "mk:@MSITStore:{0}::/{1}"; //$NON-NLS-1$
    
    private ParserDelegator mParserDelegator = new ParserDelegator();

    private String mStartPage = null;
    private String mCHMStartPage = null;
    private Map mHelpURLs = null;
    private Map mCHMHelpURLs = null;
    private Map mNSISContribPaths = new LinkedHashMap();
    
    private ResourceBundle mBundle;
    
    public void start(IProgressMonitor monitor)
    {
        monitor.subTask(EclipseNSISPlugin.getResourceString("loading.helpurls.message")); //$NON-NLS-1$
        try {
            mBundle = ResourceBundle.getBundle(NSISHelpURLProvider.class.getName());
        } 
        catch (MissingResourceException x) {
            mBundle = null;
        }
        loadNSISContribPaths();
        loadHelpURLs();
        NSISKeywords.INSTANCE.addKeywordsListener(this);
        INSTANCE = this;
    }

    public void stop(IProgressMonitor monitor)
    {
        NSISKeywords.INSTANCE.removeKeywordsListener(this);
        INSTANCE = null;
    }
    
    private void loadNSISContribPaths()
    {
        Map temp = new HashMap();
        List list = new ArrayList();
        for(Enumeration enum = mBundle.getKeys(); enum.hasMoreElements(); ) {
            String key = (String)enum.nextElement();
            if(key.startsWith("nsis.contrib.path")) { //$NON-NLS-1$
                String[] tokens = Common.tokenize(key,'#');
                Version v;
                if(tokens.length > 1) {
                    v = new Version(tokens[1]);
                }
                else {
                    v = NSISValidator.MINIMUM_NSIS_VERSION;
                }
                temp.put(v, key);
                list.add(v);
            }
        }
        Collections.sort(list);
        for(Iterator iter=list.iterator(); iter.hasNext(); ) {
            Version v = (Version)iter.next();
            mNSISContribPaths.put(v, (String)mBundle.getString((String)temp.get(v)));
        }
    }

    private void loadHelpURLs()
    {
        mHelpURLs = null;
        mCHMHelpURLs = null;
        mStartPage = null;
        mCHMStartPage = null;

        String home = NSISPreferences.INSTANCE.getNSISHome();
        if(!Common.isEmpty(home)) {
            File htmlHelpFile = new File(home,NSIS_CHM_HELP_FILE);
            if(htmlHelpFile.exists()) {
                try {
                    String startPage = mBundle.getString("help.start.page"); //$NON-NLS-1$
                    mStartPage = MessageFormat.format(NSIS_HELP_FORMAT,new Object[]{startPage});
                    mCHMStartPage = MessageFormat.format(NSIS_CHM_HELP_FORMAT,new Object[]{htmlHelpFile.getAbsolutePath(),startPage});
                }
                catch(MissingResourceException mre) {
                }
                
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
                                    keywords[j] = NSISKeywords.INSTANCE.getKeyword(keywords[j]);
                                    if(NSISKeywords.INSTANCE.isValidKeyword(keywords[j])) {
                                        list.add(keywords[j]);
                                    }
                                }
                                topicMap.put(mappedHelpTopics[i],list);
                            }
                        }
                    }
                    File helpLocation = new File(stateLocation,CACHED_HELP_LOCATION);
                    if(helpLocation.exists() && helpLocation.isFile()) {
                        helpLocation.delete();
                    }
                    if(!helpLocation.isDirectory()) {
                        helpLocation.mkdirs();
                    }
                    String temp = WinAPI.ExtractHtmlHelpAndTOC(htmlHelpFile.getAbsolutePath(),helpLocation.getAbsolutePath());
                    if(!Common.isEmpty(temp)) {
                        File tocFile = new File(temp);
                        if(tocFile.exists()) {
                            try {
                                NSISHelpTOCParserCallback parserCallback = new NSISHelpTOCParserCallback();
                                parserCallback.setTopicMap(topicMap);
                                mParserDelegator.parse(new FileReader(tocFile), parserCallback, false);
                                Map keywordHelpMap = parserCallback.getKeywordHelpMap();
                                mHelpURLs = new CaseInsensitiveMap();
                                mCHMHelpURLs = new CaseInsensitiveMap();
                                if(!Common.isEmptyMap(keywordHelpMap)) {
                                    MessageFormat mf = new MessageFormat(NSIS_HELP_FORMAT);
                                    StringBuffer buf = new StringBuffer();
                                    String[] args = new String[]{null};
                                    MessageFormat chmFormat = new MessageFormat(NSIS_CHM_HELP_FORMAT);
                                    StringBuffer chmBuf = new StringBuffer();
                                    String[] chmArgs = new String[]{htmlHelpFile.getAbsolutePath(),null};
                                    for (Iterator iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                                        buf.delete(0,buf.length());
                                        chmBuf.delete(0,chmBuf.length());

                                        String keyword = (String)iter.next();
                                        String location = (String)keywordHelpMap.get(keyword);

                                        args[0] = location;
                                        mHelpURLs.put(keyword,mf.format(args,buf,null).toString());

                                        chmArgs[1] = location;
                                        mCHMHelpURLs.put(keyword,chmFormat.format(chmArgs,chmBuf,null).toString());
                                    }
                                }
                                
                                Common.writeObject(cacheFile,new Object[]{mHelpURLs,mCHMHelpURLs});
                                cacheFile.setLastModified(htmlHelpTimeStamp);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            tocFile.delete();
                        }
                        
                        //Fix the chmlink.js
                        File chmlinkJs = new File(helpLocation,CHMLINK_JS);
                        if(chmlinkJs.exists()) {
                            chmlinkJs.delete();
                        }
                        PrintWriter writer = null;
                        BufferedReader reader= null;
                        try {
                            reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(CHMLINK_JS)));
                            writer = new PrintWriter(new BufferedWriter(new FileWriter(chmlinkJs)));
                            String line;
                            while ((line=reader.readLine()) != null) {
                                writer.println(line);
                            }
                        } 
                        catch (IOException io) {
                            io.printStackTrace();
                        } 
                        finally {
                            if (reader != null) {
                                try { 
                                    reader.close();
                                } 
                                catch (IOException e) {}
                            }
                            if (writer != null) {
                                writer.close();
                            }
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
                        mHelpURLs = (Map)((Object[])obj)[0];
                        mCHMHelpURLs = (Map)((Object[])obj)[1];
                    }
                }
            }
        }
    }
    
    public String getHelpStartPage()
    {
        return mStartPage;
    }
    
    public String getCHMHelpStartPage()
    {
        return mCHMStartPage;
    }
    
    public void keywordsChanged()
    {
        loadHelpURLs();
    }
    
    public String convertHelpURLToCHMHelpURL(String helpURL)
    {
        String chmHelpURL = null;
        if(!Common.isEmpty(helpURL)) {
            String home = NSISPreferences.INSTANCE.getNSISHome();
            if(!Common.isEmpty(home)) {
                File htmlHelpFile = new File(home,NSIS_CHM_HELP_FILE);
                if(htmlHelpFile.exists()) {
                    Matcher matcher = NSIS_HELP_PATTERN.matcher(helpURL);
                    if(matcher.matches()) {
                        if(matcher.groupCount() == 1) {
                            String link=matcher.group(1);
                            chmHelpURL = MessageFormat.format(NSIS_CHM_HELP_FORMAT,new String[]{htmlHelpFile.getAbsolutePath(),link});
                        }
                    }
                }
            }
        }
        return chmHelpURL;
    }
    
    private String getHelpURL(String keyWord, boolean useEclipseHelp)
    {
        if(!Common.isEmpty(keyWord)) {
            if(useEclipseHelp) {
                if(mHelpURLs != null) {
                    return (String)mHelpURLs.get(keyWord);
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
    
    public void showHelp()
    {
        String url = null;
        if(NSISPreferences.INSTANCE.isUseEclipseHelp()) {
            url = getHelpStartPage();
        }
        if(Common.isEmpty(url)) {
            url = getCHMHelpStartPage();
            if(!Common.isEmpty(url)) {
                openCHMHelpURL(url);
            }
        }
        else {
            PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
        }
    }
    
    public void showHelpURL(String keyword)
    {
        String url = null;
        if(NSISPreferences.INSTANCE.isUseEclipseHelp()) {
            url = getHelpURL(keyword, true);
        }
        if(Common.isEmpty(url)) {
            url = getHelpURL(keyword, false);
            if(!Common.isEmpty(url)) {
                openCHMHelpURL(url);
            }
        }
        else {
            PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
        }
    }

    /**
     * @param url
     */
    public void openCHMHelpURL(String url)
    {
        if(!NSISHTMLHelp.showHelp(url)) {
            WinAPI.HtmlHelp(WinAPI.GetDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
        }
    }
    
    public String getNSISContribPath()
    {
        Version nsisVersion = NSISPreferences.INSTANCE.getNSISVersion();
        String nsisContribPath = null;
        for(Iterator iter=mNSISContribPaths.keySet().iterator(); iter.hasNext(); ) {
            Version v = (Version)iter.next();
            if(nsisVersion.compareTo(v) >= 0) {
                nsisContribPath = (String)mNSISContribPaths.get(v);
            }
            else {
                break;
            }
        }
        return nsisContribPath;
    }
}
