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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class NSISKeywords implements INSISConstants
{
    public static String[] ALL_KEYWORDS;
    public static String[] SINGLELINE_COMPILETIME_COMMANDS;
    public static String[] MULTILINE_COMPILETIME_COMMANDS;
    public static String[] INSTALLER_ATTRIBUTES;
    public static String[] COMMANDS;
    public static String[] INSTRUCTIONS;
    public static String[] INSTALLER_PAGES;
    public static String[] INSTRUCTION_PARAMETERS;
    public static String[] INSTRUCTION_OPTIONS;
    public static String[] CALLBACKS;
    public static String[] PATH_CONSTANTS_AND_VARIABLES;
    public static String[] ALL_CONSTANTS_AND_VARIABLES;
    public static String[] REGISTERS;
    public static String[] PATH_VARIABLES;
    public static String[] VARIABLES;
    public static String[] ALL_VARIABLES;
    public static String[] PATH_CONSTANTS;
    public static String[] STRING_CONSTANTS;
    public static String[] ALL_CONSTANTS;
    public static String[] PREDEFINES;
    public static String[] PLUGINS;
    
    private static Map cKeywordsMap = new CaseInsensitiveMap();
    private static Map cPluginsMap = null;
    private static Set cAllKeywordsSet = new CaseInsensitiveSet();
    private static ArrayList cListeners = new ArrayList();
    private static IPropertyChangeListener cPropertyChangeListener  = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event)
        {
            if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
                loadPlugins();
                loadKeywords();
            }
        }
        
    };
    private static IEclipseNSISPluginListener cShutdownListener = new IEclipseNSISPluginListener() {
        public void stopped()
        {
            NSISPreferences.getPreferences().getPreferenceStore().removePropertyChangeListener(cPropertyChangeListener);
        }
    };
    
    static {
        loadKeywords();
        loadPlugins();
        NSISPreferences.getPreferences().getPreferenceStore().addPropertyChangeListener(cPropertyChangeListener);
        EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
    }
    
    private static void loadPlugins()
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
                            cPluginsMap = (Map)Common.readObject(cacheFile);
                        }
                        catch (Exception e) {
                            cPluginsMap = null;
                        }
                    }
                    if(cPluginsMap == null) {
                        cPluginsMap = new CaseInsensitiveMap();
                        changed = true;
                    }

                    int length = NSIS_PLUGINS_EXTENSION.length();
                    CaseInsensitiveSet set = new CaseInsensitiveSet();
                    for (int i = 0; i < pluginFiles.length; i++) {
                        String name = pluginFiles[i].getName();
                        name = name.substring(0,name.length()-length);
                        set.add(name);
                        PluginInfo pi = (PluginInfo)cPluginsMap.get(name);
                        if(pi == null || pi.getTimeStamp() != pluginFiles[i].lastModified()) {
                            String[] exports = WinAPI.GetPluginExports(pluginFiles[i].getAbsolutePath());
                            Arrays.sort(exports, String.CASE_INSENSITIVE_ORDER);
                            pi = new PluginInfo(name, exports,
                                                pluginFiles[i].lastModified());
                            cPluginsMap.put(name, pi);
                            changed = true;
                        }
                    }
                    if(cPluginsMap.size() != pluginFiles.length) {
                        for (Iterator iter = cPluginsMap.entrySet().iterator(); iter.hasNext();) {
                            Map.Entry entry = (Map.Entry)iter.next();
                            if(!set.contains(entry.getKey())) {
                                iter.remove();
                                changed = true;
                            }
                        }
                    }

                    if(changed) {
                        try {
                            Common.writeObject(cacheFile, cPluginsMap);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    PLUGINS = (String[])cPluginsMap.keySet().toArray(Common.EMPTY_STRING_ARRAY);
                    Arrays.sort(PLUGINS, String.CASE_INSENSITIVE_ORDER);
                }
            }
        }
    }
    
    private static void loadKeywords()
    {
        cKeywordsMap.clear();
        cAllKeywordsSet.clear();
        
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
        
        cKeywordsMap.putAll(registers);
        cKeywordsMap.putAll(pathVariables);
        cKeywordsMap.putAll(variables);
        cKeywordsMap.putAll(pathConstants);
        cKeywordsMap.putAll(stringConstants);
        cKeywordsMap.putAll(predefines);
        cKeywordsMap.putAll(singlelineCompiletimeCommands);
        cKeywordsMap.putAll(multilineCompiletimeCommands);
        cKeywordsMap.putAll(installerAttributes);
        cKeywordsMap.putAll(commands);
        cKeywordsMap.putAll(instructions);
        cKeywordsMap.putAll(installerPages);
        cKeywordsMap.putAll(instructionParameters);
        cKeywordsMap.putAll(instructionOptions);
        cKeywordsMap.putAll(callbacks);
        
        String[] temp = Common.EMPTY_STRING_ARRAY;

        Set set = getValidKeywords(registers);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (REGISTERS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(REGISTERS, String.CASE_INSENSITIVE_ORDER);
        
        set = getValidKeywords(pathVariables);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (PATH_VARIABLES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(PATH_VARIABLES, String.CASE_INSENSITIVE_ORDER);

        set = getValidKeywords(variables);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (VARIABLES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(VARIABLES, String.CASE_INSENSITIVE_ORDER);

        set = getValidKeywords(pathConstants);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (PATH_CONSTANTS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(PATH_CONSTANTS, String.CASE_INSENSITIVE_ORDER);

        set = getValidKeywords(stringConstants);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (STRING_CONSTANTS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(STRING_CONSTANTS, String.CASE_INSENSITIVE_ORDER);

        set = getValidKeywords(predefines);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (PREDEFINES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(PREDEFINES, String.CASE_INSENSITIVE_ORDER);

        PATH_CONSTANTS_AND_VARIABLES = (String[])Common.joinArrays(new Object[]{PATH_CONSTANTS, PATH_VARIABLES});
        Arrays.sort(PATH_CONSTANTS_AND_VARIABLES, String.CASE_INSENSITIVE_ORDER);

        ALL_CONSTANTS = (String[])Common.joinArrays(new Object[]{PATH_CONSTANTS, STRING_CONSTANTS});
        Arrays.sort(ALL_CONSTANTS, String.CASE_INSENSITIVE_ORDER);

        ALL_VARIABLES = (String[])Common.joinArrays(new Object[]{REGISTERS, PATH_CONSTANTS_AND_VARIABLES, VARIABLES});
        Arrays.sort(ALL_VARIABLES, String.CASE_INSENSITIVE_ORDER);

        ALL_CONSTANTS_AND_VARIABLES = (String[])Common.joinArrays(new Object[]{ALL_CONSTANTS, ALL_VARIABLES, PREDEFINES});
        Arrays.sort(ALL_CONSTANTS_AND_VARIABLES, String.CASE_INSENSITIVE_ORDER);
        
        set = getValidKeywords(singlelineCompiletimeCommands);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (SINGLELINE_COMPILETIME_COMMANDS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(multilineCompiletimeCommands);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (MULTILINE_COMPILETIME_COMMANDS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(installerAttributes);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTALLER_ATTRIBUTES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(commands);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (COMMANDS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(instructions);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTRUCTIONS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(installerPages);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTALLER_PAGES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(INSTALLER_PAGES, String.CASE_INSENSITIVE_ORDER);

        set = getValidKeywords(instructionParameters);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTRUCTION_PARAMETERS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        INSTRUCTION_PARAMETERS = (String[])Common.appendArray(INSTRUCTION_PARAMETERS, INSTALLER_PAGES);
        
        set = getValidKeywords(instructionOptions);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTRUCTION_OPTIONS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        set = getValidKeywords(callbacks);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (CALLBACKS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

        ALL_KEYWORDS = temp;
        Arrays.sort(ALL_KEYWORDS, String.CASE_INSENSITIVE_ORDER);
        notifyListeners();
    }
    
    private static Set getValidKeywords(Map keywordMap)
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
    
    public static boolean isValidKeyword(String keyword)
    {
        return cAllKeywordsSet.contains(keyword);
    }

    public static void notifyListeners()
    {
        for (Iterator iter = cListeners.iterator(); iter.hasNext();) {
            INSISKeywordsListener listener = (INSISKeywordsListener)iter.next();
            listener.keywordsChanged();
        }
    }
    
    public static void addKeywordsListener(INSISKeywordsListener listener)
    {
        if(!cListeners.contains(listener)) {
            cListeners.add(listener);
        }
    }
    
    public static void removeKeywordsListener(INSISKeywordsListener listener)
    {
        if(!cListeners.contains(listener)) {
            cListeners.remove(listener);
        }
    }

    public static String getKeyword(String name)
    {
        if(cKeywordsMap.containsKey(name)) {
            String newName = (String)cKeywordsMap.get(name);
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

    public static class VariableMatcher
    {
        private int mPotentialMatchIndex = -1;
        private String mText = null;
        
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
                for(int i=Math.max(mPotentialMatchIndex,0); i<ALL_CONSTANTS_AND_VARIABLES.length; i++) {
                    int n = ALL_CONSTANTS_AND_VARIABLES[i].compareToIgnoreCase(mText);
                    if(n < 0) {
                        continue;
                    }
                    else if(n >= 0) {
                        if(ALL_CONSTANTS_AND_VARIABLES[i].regionMatches(true,0,mText,0,mText.length())) {
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
            return (mPotentialMatchIndex >= 0 && ALL_CONSTANTS_AND_VARIABLES[mPotentialMatchIndex].equalsIgnoreCase(mText));
        }
    }
    
    public static String[] getPluginExports(String name)
    {
        PluginInfo pi = (PluginInfo)cPluginsMap.get(name);
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
