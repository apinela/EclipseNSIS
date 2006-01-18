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
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.codeassist.NSISBrowserInformationProvider;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class NSISHelpURLProvider implements INSISConstants, INSISKeywordsListener, IEclipseNSISService
{
    public static final String KEYWORD_HELP_HTML_PREFIX;
    public static final String KEYWORD_HELP_HTML_SUFFIX="\n</body></html>"; //$NON-NLS-1$

    private static NSISHelpURLProvider cInstance = null;

    private static final String NO_HELP_FILE=PLUGIN_HELP_LOCATION_PREFIX+"nohelp.html"; //$NON-NLS-1$
    private static final String CHMLINK_JS = "chmlink.js"; //$NON-NLS-1$
    private static final String NSIS_HELP_FORMAT = new StringBuffer("/").append( //$NON-NLS-1$
                                    INSISConstants.PLUGIN_ID).append("/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append(
                                    "Docs/{0}").toString(); //$NON-NLS-1$
    private static final Pattern NSIS_HELP_PATTERN = Pattern.compile(new StringBuffer(".*/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_HELP_PREFIX).append("Docs/(.*)").toString()); //$NON-NLS-1$
    private static final String NSIS_CHM_HELP_FORMAT = "mk:@MSITStore:{0}::/{1}"; //$NON-NLS-1$

    private String mStartPage = null;
    private String mCHMStartPage = null;
    private Map mHelpURLs = null;
    private Map mCHMHelpURLs = null;
    private Map mKeywordHelp = null;
    private ParserDelegator mParserDelegator;
    private Map mNSISContribPaths;

    private ResourceBundle mBundle;
    
    private boolean mNSISHelpAvailable;
    private File mCacheFile;
    private File mHelpLocation;
    private File mNoHelpFile;
    
    static {
        File styleSheet = null;
        try {
            styleSheet = IOUtility.ensureLatest(EclipseNSISPlugin.getDefault().getBundle(), 
                                        new Path("/hoverhelp/hoverstyle.css"), //$NON-NLS-1$
                                        new File(EclipseNSISPlugin.getPluginStateLocation(),"hoverhelp")); //$NON-NLS-1$
        }
        catch (IOException e1) {
            styleSheet = null;
        }
        final StringBuffer htmlPrefix = new StringBuffer("<html>\n<head>\n"); //$NON-NLS-1$
        if(styleSheet != null) {
            htmlPrefix.append("<link rel=\"stylesheet\" href=\"").append(styleSheet.toURI()).append( //$NON-NLS-1$
                    "\" charset=\"ISO-8859-1\" type=\"text/css\">\n"); //$NON-NLS-1$
        }
        else {
            htmlPrefix.append("<style type=\"text/css\">\n").append( //$NON-NLS-1$
                    ".heading { font-weight: bold; font-size: 120%; }\n").append(  //$NON-NLS-1$
                    ".link { font-weight: bold; }\n</style>\n"); //$NON-NLS-1$
        }
        if(NSISBrowserInformationProvider.COLORS_CSS_FILE != null) {
            htmlPrefix.append("<link rel=\"stylesheet\" href=\"").append( //$NON-NLS-1$
            NSISBrowserInformationProvider.COLORS_CSS_FILE.toURI()).append(
            "\" charset=\"ISO-8859-1\" type=\"text/css\">\n"); //$NON-NLS-1$
        }
        htmlPrefix.append("</head>\n<body>\n"); //$NON-NLS-1$
        KEYWORD_HELP_HTML_PREFIX = htmlPrefix.toString();
    }

    public static NSISHelpURLProvider getInstance()
    {
        return cInstance;
    }

    public NSISHelpURLProvider()
    {
        super();
        File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
        mCacheFile = new File(stateLocation, getClass().getName() + ".HelpURLs.ser"); //$NON-NLS-1$
        mHelpLocation = new File(stateLocation, CACHED_HELP_LOCATION);
        mNoHelpFile = new File(stateLocation.getAbsolutePath()+NO_HELP_FILE);
    }

    public File getNoHelpFile()
    {
        return mNoHelpFile;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            monitor.subTask(EclipseNSISPlugin
                    .getResourceString("loading.helpurls.message")); //$NON-NLS-1$
            try {
                mBundle = ResourceBundle.getBundle(NSISHelpURLProvider.class
                        .getName());
            }
            catch (MissingResourceException x) {
                mBundle = null;
            }
            mParserDelegator = new ParserDelegator();
            mNSISContribPaths = new LinkedHashMap();
            loadNSISContribPaths();
            loadHelpURLs();
            NSISKeywords.getInstance().addKeywordsListener(this);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            mStartPage = null;
            mCHMStartPage = null;
            mHelpURLs = null;
            mCHMHelpURLs = null;
            mKeywordHelp = null;
            mNSISHelpAvailable = false;
            mParserDelegator = null;
            mNSISContribPaths = null;
            mBundle = null;
            NSISKeywords.getInstance().removeKeywordsListener(this);
        }
    }

    public boolean isNSISHelpAvailable()
    {
        checkHelpFile();
        return mNSISHelpAvailable;
    }

    private void loadNSISContribPaths()
    {
        Map temp = new HashMap();
        List list = new ArrayList();
        for(Enumeration e = mBundle.getKeys(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
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
            mNSISContribPaths.put(v, mBundle.getString((String)temp.get(v)));
        }
    }

    private void loadHelpURLs()
    {
        mHelpURLs = null;
        mCHMHelpURLs = null;
        mStartPage = null;
        mCHMStartPage = null;
        mKeywordHelp = null;
        mNSISHelpAvailable = false;

        try {
            String home = NSISPreferences.INSTANCE.getNSISHome();
            if (!Common.isEmpty(home)) {
                File htmlHelpFile = new File(home, NSIS_CHM_HELP_FILE);
                if (htmlHelpFile.exists()) {
                    try {
                        String startPage = mBundle.getString("help.start.page"); //$NON-NLS-1$
                        mStartPage = MessageFormat.format(NSIS_HELP_FORMAT, new Object[]{startPage});
                        mCHMStartPage = MessageFormat.format(NSIS_CHM_HELP_FORMAT, new Object[]{htmlHelpFile.getAbsolutePath(), startPage});
                    }
                    catch (MissingResourceException mre) {
                    }

                    long cacheTimeStamp = 0;
                    if (mCacheFile.exists()) {
                        cacheTimeStamp = mCacheFile.lastModified();
                    }

                    long htmlHelpTimeStamp = htmlHelpFile.lastModified();
                    if (htmlHelpTimeStamp == cacheTimeStamp) {
                        Object obj = null;
                        try {
                            obj = IOUtility.readObject(mCacheFile);
                        }
                        catch (Exception e) {
                            obj = null;
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                        if (obj != null && obj.getClass().isArray() && Array.getLength(obj) == 3) {
                            mHelpURLs = (Map)((Object[])obj)[0];
                            mCHMHelpURLs = (Map)((Object[])obj)[1];
                            mKeywordHelp = (Map)((Object[])obj)[2];
                            mNSISHelpAvailable = true;
                            return;
                        }
                    }

                    if (mCacheFile.exists()) {
                        mCacheFile.delete();
                    }

                    Map topicMap = new CaseInsensitiveMap();

                    String[] mappedHelpTopics = Common.loadArrayProperty(mBundle, "mapped.help.topics"); //$NON-NLS-1$
                    if (!Common.isEmptyArray(mappedHelpTopics)) {
                        for (int i = 0; i < mappedHelpTopics.length; i++) {
                            String[] keywords = Common.loadArrayProperty(mBundle, mappedHelpTopics[i]);
                            if (!Common.isEmptyArray(keywords)) {
                                ArrayList list = new ArrayList();
                                for (int j = 0; j < keywords.length; j++) {
                                    keywords[j] = NSISKeywords.getInstance().getKeyword(keywords[j]);
                                    if (NSISKeywords.getInstance().isValidKeyword(keywords[j])) {
                                        list.add(keywords[j]);
                                    }
                                }
                                topicMap.put(mappedHelpTopics[i], list);
                            }
                        }
                    }

                    if (IOUtility.isValidFile(mHelpLocation)) {
                        mHelpLocation.delete();
                    }
                    if (!IOUtility.isValidDirectory(mHelpLocation)) {
                        mHelpLocation.mkdirs();
                    }
                    String temp = WinAPI.ExtractHtmlHelpAndTOC(htmlHelpFile.getAbsolutePath(), mHelpLocation.getAbsolutePath());

                    if (!Common.isEmpty(temp)) {
                        File tocFile = new File(temp);
                        if (tocFile.exists()) {
                            try {
                                NSISHelpTOCParserCallback parserCallback = new NSISHelpTOCParserCallback();
                                parserCallback.setTopicMap(topicMap);
                                mParserDelegator.parse(new FileReader(tocFile), parserCallback, false);

                                Map keywordHelpMap = parserCallback.getKeywordHelpMap();
                                mHelpURLs = new CaseInsensitiveMap();
                                mCHMHelpURLs = new CaseInsensitiveMap();
                                if (!Common.isEmptyMap(keywordHelpMap)) {
                                    MessageFormat mf = new MessageFormat(NSIS_HELP_FORMAT);
                                    StringBuffer buf = new StringBuffer();
                                    String[] args = new String[]{null};
                                    MessageFormat chmFormat = new MessageFormat(NSIS_CHM_HELP_FORMAT);
                                    StringBuffer chmBuf = new StringBuffer();
                                    String[] chmArgs = new String[]{htmlHelpFile.getAbsolutePath(), null};
                                    for (Iterator iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                                        buf.setLength(0);
                                        chmBuf.setLength(0);

                                        String keyword = (String)iter.next();
                                        String location = (String)keywordHelpMap.get(keyword);

                                        args[0] = location;
                                        mHelpURLs.put(keyword, mf.format(args, buf, null).toString());

                                        chmArgs[1] = location;
                                        mCHMHelpURLs.put(keyword, chmFormat.format(chmArgs, chmBuf, null).toString());
                                    }

                                    Set urls = new CaseInsensitiveSet(keywordHelpMap.values());
                                    Map urlContentsMap = new CaseInsensitiveMap();
                                    List processedFiles = new ArrayList();
                                    for (Iterator iter = urls.iterator(); iter.hasNext();) {
                                        String url = (String)iter.next();
                                        int n = url.indexOf('#');
                                        if (n > 1) {
                                            String htmlFile = url.substring(0, n).toLowerCase();
                                            if (!processedFiles.contains(htmlFile)) {
                                                processedFiles.add(htmlFile);
                                                NSISHelpFileParserCallback callback = new NSISHelpFileParserCallback(htmlFile + "#", urls, urlContentsMap); //$NON-NLS-1$
                                                mParserDelegator.parse(new FileReader(new File(tocFile.getParentFile(), htmlFile)), callback, false);
                                            }
                                        }
                                    }

                                    mKeywordHelp = new CaseInsensitiveMap();
                                    for (Iterator iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                                        String keyword = (String)iter.next();
                                        String url = (String)keywordHelpMap.get(keyword);
                                        String help = (String)urlContentsMap.get(url);
                                        if (help != null) {
                                            mKeywordHelp.put(keyword, help);
                                        }
                                    }
                                }

                                IOUtility.writeObject(mCacheFile, new Object[]{mHelpURLs, mCHMHelpURLs, mKeywordHelp});
                                mCacheFile.setLastModified(htmlHelpTimeStamp);
                            }
                            catch (Exception e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                            tocFile.delete();
                        }

                        //Fix the chmlink.js
                        File chmlinkJs = new File(mHelpLocation, CHMLINK_JS);
                        if (chmlinkJs.exists()) {
                            chmlinkJs.delete();
                        }
                        PrintWriter writer = null;
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(CHMLINK_JS)));
                            writer = new PrintWriter(new BufferedWriter(new FileWriter(chmlinkJs)));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.println(line);
                            }
                        }
                        catch (IOException io) {
                            EclipseNSISPlugin.getDefault().log(io);
                        }
                        finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                }
                                catch (IOException e) {
                                }
                            }
                            if (writer != null) {
                                writer.close();
                            }
                        }
                    }

                    mNSISHelpAvailable = true;
                }
            }
        }
        finally {
            if(!mNSISHelpAvailable) {
                if(IOUtility.isValidFile(mCacheFile)) {
                    mCacheFile.delete();
                }
                if(IOUtility.isValidDirectory(mHelpLocation)) {
                    IOUtility.deleteDirectory(mHelpLocation);
                }
                if(!IOUtility.isValidFile(mNoHelpFile)) {
                    if(IOUtility.isValidDirectory(mNoHelpFile)) {
                        mNoHelpFile.delete();
                    }
                    File parent = mNoHelpFile.getParentFile();
                    if(!IOUtility.isValidDirectory(parent)) {
                        if(IOUtility.isValidFile(parent)) {
                            parent.delete();
                        }
                        parent.mkdirs();
                    }
                    String text = EclipseNSISPlugin.getFormattedString("missing.chm.format",  //$NON-NLS-1$
                            new String[] {EclipseNSISPlugin.getResourceString("help.style")}); //$NON-NLS-1$
                    IOUtility.writeContentToFile(mNoHelpFile, text.getBytes());
                }
                try {
                    mStartPage = mCHMStartPage = mNoHelpFile.toURI().toURL().toString();
                }
                catch (MalformedURLException e) {
                    mStartPage = null;
                    mCHMStartPage = null;
                }
            }
        }        
    }

    public String getKeywordHelp(String keyword)
    {
        checkHelpFile();
        return (String)(mKeywordHelp==null?null:mKeywordHelp.get(keyword));
    }

    public String getHelpStartPage()
    {
        checkHelpFile();
        return mStartPage;
    }

    public String getCHMHelpStartPage()
    {
        checkHelpFile();
        return mCHMStartPage;
    }

    public void keywordsChanged()
    {
        loadHelpURLs();
    }

    private void checkHelpFile()
    {
        boolean helpFileExists = false;
        String home = NSISPreferences.INSTANCE.getNSISHome();
        if (!Common.isEmpty(home)) {
            File htmlHelpFile = new File(home, NSIS_CHM_HELP_FILE);
            if (htmlHelpFile.exists()) {
                helpFileExists = true;
            }
        }
        if(mNSISHelpAvailable ^ helpFileExists) {
            loadHelpURLs();
        }
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
            checkHelpFile();
            if(useEclipseHelp) {
                if(!mNSISHelpAvailable) {
                    return mStartPage;
                }
                if(mHelpURLs != null) {
                    return (String)mHelpURLs.get(keyWord);
                }
            }
            else {
                if(!mNSISHelpAvailable) {
                    return mCHMStartPage;
                }
                if(mCHMHelpURLs != null) {
                    return (String)mCHMHelpURLs.get(keyWord);
                }
            }
        }
        return null;
    }

    public void showHelp()
    {
        checkHelpFile();
        String url = null;
        if(NSISPreferences.INSTANCE.isUseEclipseHelp()) {
            url = getHelpStartPage();
        }
        if(Common.isEmpty(url)) {
            url = getCHMHelpStartPage();
            openCHMHelpURL(url);
        }
        else {
            showPlatformHelp(url);
        }
    }

    public void showPlatformHelp(String url)
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
    }

    public void showHelpURL(String keyword)
    {
        checkHelpFile();
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
            showPlatformHelp(url);
        }
    }

    /**
     * @param url
     */
    public void openCHMHelpURL(String url)
    {
        checkHelpFile();
        if(!NSISHTMLHelp.showHelp(url)) {
            if(mNSISHelpAvailable) {
                WinAPI.HtmlHelp(WinAPI.GetDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
            }
            else {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        Common.openError(Display.getCurrent().getActiveShell(), 
                                EclipseNSISPlugin.getResourceString("missing.help.file.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                    }
                });
            }
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
