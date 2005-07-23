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
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class NSISKeywords implements INSISConstants, IEclipseNSISService
{
    public static NSISKeywords INSTANCE = null;
    
    public static final String ALL_KEYWORDS="ALL_KEYWORDS";
    public static final String SINGLELINE_COMPILETIME_COMMANDS="SINGLELINE_COMPILETIME_COMMANDS";
    public static final String MULTILINE_COMPILETIME_COMMANDS="MULTILINE_COMPILETIME_COMMANDS";
    public static final String INSTALLER_ATTRIBUTES="INSTALLER_ATTRIBUTES";
    public static final String COMMANDS="COMMANDS";
    public static final String INSTRUCTIONS="INSTRUCTIONS";
    public static final String INSTALLER_PAGES="INSTALLER_PAGES";
    public static final String INSTRUCTION_PARAMETERS="INSTRUCTION_PARAMETERS";
    public static final String INSTRUCTION_OPTIONS="INSTRUCTION_OPTIONS";
    public static final String CALLBACKS="CALLBACKS";
    public static final String PATH_CONSTANTS_AND_VARIABLES="PATH_CONSTANTS_AND_VARIABLES";
    public static final String ALL_CONSTANTS_AND_VARIABLES="ALL_CONSTANTS_AND_VARIABLES";
    public static final String REGISTERS="REGISTERS";
    public static final String PATH_VARIABLES="PATH_VARIABLES";
    public static final String VARIABLES="VARIABLES";
    public static final String ALL_VARIABLES="ALL_VARIABLES";
    public static final String PATH_CONSTANTS="PATH_CONSTANTS";
    public static final String STRING_CONSTANTS="STRING_CONSTANTS";
    public static final String ALL_CONSTANTS="ALL_CONSTANTS";
    public static final String PREDEFINES="PREDEFINES";
    public static final String PLUGINS="PLUGINS";
    
    private String[] mPlugins;
    
    private Map mKeywordGroupsMap = new HashMap();
    private Map mKeywordsMap = new CaseInsensitiveMap();
    private Map mPluginsMap = null;
    private Set mAllKeywordsSet = new CaseInsensitiveSet();
    private ArrayList mListeners = new ArrayList();
    private IPropertyChangeListener mPropertyChangeListener  = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
                loadPlugins();
                loadKeywords();
            }
        }
        
    };

    public void start(IProgressMonitor monitor)
    {
        monitor.subTask("Loading keywords");
        loadKeywords();
        loadPlugins();
        NSISPreferences.getPreferences().getPreferenceStore().addPropertyChangeListener(mPropertyChangeListener);
        INSTANCE = this;
    }

    public void stop(IProgressMonitor monitor)
    {
        INSTANCE = null;
        NSISPreferences.getPreferences().getPreferenceStore().removePropertyChangeListener(mPropertyChangeListener);
    }
    
    private void loadPlugins()
    {
        String nsisHome = NSISPreferences.getPreferences().getNSISHome();
        if(!Common.isEmpty(nsisHome)) {
            File nsisHomeDir = new File(nsisHome);
            if(nsisHomeDir.exists() && nsisHomeDir.isDirectory()) {
                File nsisPluginsDir = new File(nsisHomeDir, NSIS_PLUGINS_LOCATION);
                if(nsisPluginsDir.exists() && nsisPluginsDir.isDirectory()) {
                    File[] pluginFiles = nsisPluginsDir.listFiles(new FileFilter() {
                        private String mExtension = NSIS_PLUGINS_EXTENSION.toLowerCase();
                        public boolean accept(File pathname)
                        {
                            if(pathname.isFile()) {
                                String name = pathname.getName().toLowerCase();
                                return name.endsWith(mExtension);
                            }
                            return false;
                        }
                        
                    });
                    boolean changed = false;
                    File cacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISKeywords.class.getName()+".Plugins.ser"); //$NON-NLS-1$
                    if(cacheFile.exists()) {
                        try {
                            mPluginsMap = (Map)Common.readObject(cacheFile);
                        }
                        catch (Exception e) {
                            mPluginsMap = null;
                        }
                    }
                    if(mPluginsMap == null) {
                        mPluginsMap = new CaseInsensitiveMap();
                        changed = true;
                    }

                    int length = NSIS_PLUGINS_EXTENSION.length();
                    CaseInsensitiveSet set = new CaseInsensitiveSet();
                    for (int i = 0; i < pluginFiles.length; i++) {
                        String name = pluginFiles[i].getName();
                        name = name.substring(0,name.length()-length);
                        set.add(name);
                        PluginInfo pi = (PluginInfo)mPluginsMap.get(name);
                        if(pi == null || pi.getTimeStamp() != pluginFiles[i].lastModified()) {
                            String[] exports = WinAPI.GetPluginExports(pluginFiles[i].getAbsolutePath());
                            Arrays.sort(exports, String.CASE_INSENSITIVE_ORDER);
                            pi = new PluginInfo(name, exports,
                                                pluginFiles[i].lastModified());
                            mPluginsMap.put(name, pi);
                            changed = true;
                        }
                    }
                    if(mPluginsMap.size() != pluginFiles.length) {
                        for (Iterator iter = mPluginsMap.entrySet().iterator(); iter.hasNext();) {
                            Map.Entry entry = (Map.Entry)iter.next();
                            if(!set.contains(entry.getKey())) {
                                iter.remove();
                                changed = true;
                            }
                        }
                    }

                    if(changed) {
                        try {
                            Common.writeObject(cacheFile, mPluginsMap);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mPlugins = (String[])mPluginsMap.keySet().toArray(Common.EMPTY_STRING_ARRAY);
                    Arrays.sort(mPlugins, String.CASE_INSENSITIVE_ORDER);
                }
            }
        }
    }
    
    private void loadKeywords()
    {
        mKeywordsMap.clear();
        mAllKeywordsSet.clear();
        
        ResourceBundle bundle;
        Version nsisVersion = NSISPreferences.getPreferences().getNSISVersion();
        try {
            bundle = ResourceBundle.getBundle(NSISKeywords.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        Map registers = new CaseInsensitiveMap();
        Map pathVariables = new CaseInsensitiveMap();
        Map variables = new CaseInsensitiveMap();
        Map pathConstants = new CaseInsensitiveMap();
        Map stringConstants = new CaseInsensitiveMap();
        Map predefines = new CaseInsensitiveMap();
        Map singlelineCompiletimeCommands = new CaseInsensitiveMap();
        Map multilineCompiletimeCommands = new CaseInsensitiveMap();
        Map installerAttributes = new CaseInsensitiveMap();
        Map commands = new CaseInsensitiveMap();
        Map instructions = new CaseInsensitiveMap();
        Map installerPages = new CaseInsensitiveMap();
        Map instructionParameters = new CaseInsensitiveMap();
        Map instructionOptions = new CaseInsensitiveMap();
        Map callbacks = new CaseInsensitiveMap();

        if(bundle != null && nsisVersion != null) {
            HashMap versionMap = new HashMap();
            for(Enumeration enum=bundle.getKeys(); enum.hasMoreElements();) {
                String key = (String)enum.nextElement();
                int n = key.indexOf('#');
                String name = key.substring(0,n);
                Version version = new Version(key.substring(n+1));
                if(nsisVersion.compareTo(version) >= 0) {
                    ArrayList list = (ArrayList)versionMap.get(version);
                    if(list == null) {
                        list = new ArrayList();
                        versionMap.put(version, list);
                    }
                    list.add(new String[]{name,key});
                }
            }
            
            ArrayList versionList = new ArrayList(versionMap.keySet());
            Collections.sort(versionList);
            
            for (Iterator iter = versionList.iterator(); iter.hasNext();) {
                Version version = (Version)iter.next();
                ArrayList list = (ArrayList)versionMap.get(version);
                for (Iterator iter2 = list.iterator(); iter2.hasNext();) {
                    String[] element = (String[])iter2.next();
                    String name = element[0];
                    String key = element[1];
                    String[] values = Common.loadArrayProperty(bundle,key);
                    if(!Common.isEmptyArray(values)) {
                        Map map = null;
                        if(name.equals("registers")) { //$NON-NLS-1$
                            map = registers;
                        }
                        else if(name.equals("path.variables")) { //$NON-NLS-1$
                            map = pathVariables;
                        }
                        else if(name.equals("variables")) { //$NON-NLS-1$
                            map = variables;
                        }
                        else if(name.equals("path.constants")) { //$NON-NLS-1$
                            map = pathConstants;
                        }
                        else if(name.equals("string.constants")) { //$NON-NLS-1$
                            map = stringConstants;
                        }
                        else if(name.equals("predefines")) { //$NON-NLS-1$
                            map = predefines;
                        }
                        else if(name.equals("singleline.compiletime.commands")) { //$NON-NLS-1$
                            map = singlelineCompiletimeCommands;
                        }
                        else if(name.equals("multiline.compiletime.commands")) { //$NON-NLS-1$
                            map = multilineCompiletimeCommands;
                        }
                        else if(name.equals("installer.attributes")) { //$NON-NLS-1$
                            map = installerAttributes;
                        }
                        else if(name.equals("commands")) { //$NON-NLS-1$
                            map = commands;
                        }
                        else if(name.equals("instructions")) { //$NON-NLS-1$
                            map = instructions;
                        }
                        else if(name.equals("installer.pages")) { //$NON-NLS-1$
                            map = installerPages;
                        }
                        else if(name.equals("instruction.parameters")) { //$NON-NLS-1$
                            map = instructionParameters;
                        }
                        else if(name.equals("instruction.options")) { //$NON-NLS-1$
                            map = instructionOptions;
                        }
                        else if(name.equals("callbacks")) { //$NON-NLS-1$
                            map = callbacks;
                        }
    
                        for (int i = 0; i < values.length; i++) {
                            int n = values[i].indexOf('~');
                            if(values[i].charAt(0) == '-') {
                                map.remove(values[i].substring(1));
                            }
                            else if(n > 0) {
                                String oldValue = values[i].substring(0,n);
                                String newValue = values[i].substring(n+1);
                                map.put(oldValue,newValue);
                            }
                            else {
                                if(values[i].charAt(0) == '+') {
                                    values[i] = values[i].substring(1);
                                }
                                map.put(values[i],values[i]);
                            }
                        }
                    }
                }
            }
        }
        
        mKeywordsMap.putAll(registers);
        mKeywordsMap.putAll(pathVariables);
        mKeywordsMap.putAll(variables);
        mKeywordsMap.putAll(pathConstants);
        mKeywordsMap.putAll(stringConstants);
        mKeywordsMap.putAll(predefines);
        mKeywordsMap.putAll(singlelineCompiletimeCommands);
        mKeywordsMap.putAll(multilineCompiletimeCommands);
        mKeywordsMap.putAll(installerAttributes);
        mKeywordsMap.putAll(commands);
        mKeywordsMap.putAll(instructions);
        mKeywordsMap.putAll(installerPages);
        mKeywordsMap.putAll(instructionParameters);
        mKeywordsMap.putAll(instructionOptions);
        mKeywordsMap.putAll(callbacks);
        
        String[] temp = Common.EMPTY_STRING_ARRAY;
        String[] temp2;

        Set set = getValidKeywords(registers);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(REGISTERS,temp2);
        
        set = getValidKeywords(pathVariables);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(PATH_VARIABLES,temp2);

        set = getValidKeywords(variables);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(VARIABLES,temp2);

        set = getValidKeywords(pathConstants);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(PATH_CONSTANTS,temp2);

        set = getValidKeywords(stringConstants);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(STRING_CONSTANTS,temp2);

        set = getValidKeywords(predefines);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(PREDEFINES,temp2);

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(PATH_CONSTANTS), getKeywordsGroup(PATH_VARIABLES)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(PATH_CONSTANTS_AND_VARIABLES,temp2);

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(PATH_CONSTANTS), getKeywordsGroup(STRING_CONSTANTS)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_CONSTANTS,temp2);

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(REGISTERS), getKeywordsGroup(PATH_CONSTANTS_AND_VARIABLES), 
                                                                                  getKeywordsGroup(VARIABLES)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_VARIABLES,temp2);

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(ALL_CONSTANTS), getKeywordsGroup(ALL_VARIABLES), 
                                                                                              getKeywordsGroup(PREDEFINES)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_CONSTANTS_AND_VARIABLES,temp2);
        
        set = getValidKeywords(singlelineCompiletimeCommands);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(SINGLELINE_COMPILETIME_COMMANDS,temp2);

        set = getValidKeywords(multilineCompiletimeCommands);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(MULTILINE_COMPILETIME_COMMANDS, temp2);

        set = getValidKeywords(installerAttributes);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(INSTALLER_ATTRIBUTES,temp2);

        set = getValidKeywords(commands);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(COMMANDS,temp2);

        set = getValidKeywords(instructions);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(INSTRUCTIONS,temp2);

        set = getValidKeywords(installerPages);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(INSTALLER_PAGES,temp2);

        set = getValidKeywords(instructionParameters);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        temp2 = (String[])Common.appendArray(temp2, getKeywordsGroup(INSTALLER_PAGES));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(INSTRUCTION_PARAMETERS,temp2);
        
        set = getValidKeywords(instructionOptions);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(INSTRUCTION_OPTIONS,temp2);

        set = getValidKeywords(callbacks);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(CALLBACKS,temp2);

        Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_KEYWORDS,temp);
        notifyListeners();
    }
    
    private Set getValidKeywords(Map keywordMap)
    {
        HashSet set = new HashSet();
        for (Iterator iter = keywordMap.values().iterator(); iter.hasNext();) {
            String keyword = (String)iter.next();
            String mappedKeyword = getKeyword(keyword);
            if(mappedKeyword.equalsIgnoreCase(keyword)) {
                set.add(keyword);
            }
        }
        return set;
    }
    
    public String[] getKeywordsGroup(String group)
    {
        return (String[])mKeywordGroupsMap.get(group);
    }
    
    public boolean isValidKeyword(String keyword)
    {
        return mAllKeywordsSet.contains(keyword);
    }

    public void notifyListeners()
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            INSISKeywordsListener listener = (INSISKeywordsListener)iter.next();
            listener.keywordsChanged();
        }
    }
    
    public void addKeywordsListener(INSISKeywordsListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void removeKeywordsListener(INSISKeywordsListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public String getKeyword(String name)
    {
        if(mKeywordsMap.containsKey(name)) {
            String newName = (String)mKeywordsMap.get(name);
            if(!newName.equalsIgnoreCase(name)) {
                //This has been renamed. Check if it has been renamed again
                return getKeyword(newName);
            }
            else {
                return newName;
            }
        }
        else {
            return name;
        }
    }
    
    public VariableMatcher createVariableMatcher()
    {
        return new VariableMatcher();
    }

    public class VariableMatcher
    {
        private int mPotentialMatchIndex = -1;
        private String mText = null;
        private String[] mKeywords = getKeywordsGroup(ALL_CONSTANTS_AND_VARIABLES);
        
        public void reset()
        {
            mPotentialMatchIndex = -1;
            mText = null;
        }
        
        public void setText(String text)
        {
            text = text.toLowerCase();
            if(mText != null) {
                if(!text.startsWith(mText)) {
                    reset();
                }
            }
            mText = text;
        }
        
        public boolean hasPotentialMatch()
        {
            if(mText != null) {
                for(int i=Math.max(mPotentialMatchIndex,0); i<mKeywords.length; i++) {
                    int n = mKeywords[i].compareToIgnoreCase(mText);
                    if(n < 0) {
                        continue;
                    }
                    else if(n >= 0) {
                        if(mKeywords[i].regionMatches(true,0,mText,0,mText.length())) {
                            mPotentialMatchIndex = i;
                            return true;
                        }
                        break;
                    }
                }
            }
            return false;
        }
        
        public boolean isMatch()
        {
            return (mPotentialMatchIndex >= 0 && mKeywords[mPotentialMatchIndex].equalsIgnoreCase(mText));
        }
    }
    
    public String[] getPluginExports(String name)
    {
        PluginInfo pi = (PluginInfo)mPluginsMap.get(name);
        if(pi != null) {
            return pi.getExports();
        }
        else {
            return Common.EMPTY_STRING_ARRAY;
        }
    }

    private static class PluginInfo implements Serializable
    {
        private static final long serialVersionUID = 3815184021913290871L;

        private long mTimeStamp;
        private String mName;
        private String[] mExports;
        
        /**
         * @param name
         * @param exports
         * @param timeStamp
         */
        public PluginInfo(String name, String[] exports, long timeStamp)
        {
            mName = name;
            mExports = exports;
            mTimeStamp = timeStamp;
        }
        
        /**
         * @return Returns the exports.
         */
        public String[] getExports()
        {
            return mExports;
        }
        
        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }
        
        /**
         * @return Returns the timeStamp.
         */
        public long getTimeStamp()
        {
            return mTimeStamp;
        }
    }

}
