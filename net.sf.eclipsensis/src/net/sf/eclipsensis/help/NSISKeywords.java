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
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class NSISKeywords implements INSISConstants, IEclipseNSISService
{
    private static NSISKeywords cInstance = null;

    public static final String ALL_KEYWORDS="ALL_KEYWORDS"; //$NON-NLS-1$
    public static final String SINGLELINE_COMPILETIME_COMMANDS="SINGLELINE_COMPILETIME_COMMANDS"; //$NON-NLS-1$
    public static final String MULTILINE_COMPILETIME_COMMANDS="MULTILINE_COMPILETIME_COMMANDS"; //$NON-NLS-1$
    public static final String INSTALLER_ATTRIBUTES="INSTALLER_ATTRIBUTES"; //$NON-NLS-1$
    public static final String COMMANDS="COMMANDS"; //$NON-NLS-1$
    public static final String INSTRUCTIONS="INSTRUCTIONS"; //$NON-NLS-1$
    public static final String INSTALLER_PAGES="INSTALLER_PAGES"; //$NON-NLS-1$
    public static final String HKEY_PARAMETERS="HKEY_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_PARAMETERS="MESSAGEBOX_OPTION_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_RETURN_PARAMETERS="MESSAGEBOX_RETURN_PARAMETERS"; //$NON-NLS-1$
    public static final String INSTRUCTION_PARAMETERS="INSTRUCTION_PARAMETERS"; //$NON-NLS-1$
    public static final String INSTRUCTION_OPTIONS="INSTRUCTION_OPTIONS"; //$NON-NLS-1$
    public static final String CALLBACKS="CALLBACKS"; //$NON-NLS-1$
    public static final String PATH_CONSTANTS_AND_VARIABLES="PATH_CONSTANTS_AND_VARIABLES"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS_AND_VARIABLES="ALL_CONSTANTS_AND_VARIABLES"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS_VARIABLES_AND_SYMBOLS="ALL_CONSTANTS_VARIABLES_AND_SYMBOLS"; //$NON-NLS-1$
    public static final String REGISTERS="REGISTERS"; //$NON-NLS-1$
    public static final String PATH_VARIABLES="PATH_VARIABLES"; //$NON-NLS-1$
    public static final String VARIABLES="VARIABLES"; //$NON-NLS-1$
    public static final String ALL_VARIABLES="ALL_VARIABLES"; //$NON-NLS-1$
    public static final String PATH_CONSTANTS="PATH_CONSTANTS"; //$NON-NLS-1$
    public static final String STRING_CONSTANTS="STRING_CONSTANTS"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS="ALL_CONSTANTS"; //$NON-NLS-1$
    public static final String SYMBOLS="SYMBOLS"; //$NON-NLS-1$
    public static final String PREDEFINES="PREDEFINES"; //$NON-NLS-1$
    public static final String PLUGINS="PLUGINS"; //$NON-NLS-1$

    private Map mKeywordGroupsMap = null;
    private Map mNewerKeywordsMap = null;
    private Set mAllKeywordsSet = null;
    private ArrayList mListeners = null;
    private INSISHomeListener mNSISHomeListener = null;

    public static NSISKeywords getInstance()
    {
        return cInstance;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mKeywordGroupsMap = new HashMap();
            mNewerKeywordsMap = new CaseInsensitiveMap();
            mAllKeywordsSet = new CaseInsensitiveSet();
            mListeners = new ArrayList();
            mNSISHomeListener  = new INSISHomeListener() {
                public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
                {
                    loadKeywords(monitor);
                }
            };
            loadKeywords(monitor);
            NSISPreferences.INSTANCE.addListener(mNSISHomeListener);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.INSTANCE.removeListener(mNSISHomeListener);
            mKeywordGroupsMap = null;
            mNewerKeywordsMap = null;
            mAllKeywordsSet = null;
            mListeners = null;
            mNSISHomeListener = null;
        }
    }

    private void loadKeywords(IProgressMonitor monitor)
    {
        if(monitor != null) {
            monitor.subTask(EclipseNSISPlugin.getResourceString("loading.keywords.message")); //$NON-NLS-1$
        }
        mNewerKeywordsMap.clear();
        mAllKeywordsSet.clear();

        ResourceBundle bundle;
        Version nsisVersion = NSISPreferences.INSTANCE.getNSISVersion();
        try {
            bundle = ResourceBundle.getBundle(NSISKeywords.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        Set registers = new CaseInsensitiveSet();
        Set pathVariables = new CaseInsensitiveSet();
        Set variables = new CaseInsensitiveSet();
        Set pathConstants = new CaseInsensitiveSet();
        Set stringConstants = new CaseInsensitiveSet();
        Set predefines = new CaseInsensitiveSet();
        Set singlelineCompiletimeCommands = new CaseInsensitiveSet();
        Set multilineCompiletimeCommands = new CaseInsensitiveSet();
        Set installerAttributes = new CaseInsensitiveSet();
        Set commands = new CaseInsensitiveSet();
        Set instructions = new CaseInsensitiveSet();
        Set installerPages = new CaseInsensitiveSet();
        Set hkeyParameters = new CaseInsensitiveSet();
        Set messageboxOptionParameters = new CaseInsensitiveSet();
        Set messageboxReturnParameters = new CaseInsensitiveSet();
        Set instructionParameters = new CaseInsensitiveSet();
        Set instructionOptions = new CaseInsensitiveSet();
        Set callbacks = new CaseInsensitiveSet();

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
                        Set set = null;
                        if(name.equals("registers")) { //$NON-NLS-1$
                            set = registers;
                        }
                        else if(name.equals("path.variables")) { //$NON-NLS-1$
                            set = pathVariables;
                        }
                        else if(name.equals("variables")) { //$NON-NLS-1$
                            set = variables;
                        }
                        else if(name.equals("path.constants")) { //$NON-NLS-1$
                            set = pathConstants;
                        }
                        else if(name.equals("string.constants")) { //$NON-NLS-1$
                            set = stringConstants;
                        }
                        else if(name.equals("predefines")) { //$NON-NLS-1$
                            set = predefines;
                        }
                        else if(name.equals("singleline.compiletime.commands")) { //$NON-NLS-1$
                            set = singlelineCompiletimeCommands;
                        }
                        else if(name.equals("multiline.compiletime.commands")) { //$NON-NLS-1$
                            set = multilineCompiletimeCommands;
                        }
                        else if(name.equals("installer.attributes")) { //$NON-NLS-1$
                            set = installerAttributes;
                        }
                        else if(name.equals("commands")) { //$NON-NLS-1$
                            set = commands;
                        }
                        else if(name.equals("instructions")) { //$NON-NLS-1$
                            set = instructions;
                        }
                        else if(name.equals("installer.pages")) { //$NON-NLS-1$
                            set = installerPages;
                        }
                        else if(name.equals("hkey.parameters")) { //$NON-NLS-1$
                            set = hkeyParameters;
                        }
                        else if(name.equals("messagebox.option.parameters")) { //$NON-NLS-1$
                            set = messageboxOptionParameters;
                        }
                        else if(name.equals("messagebox.return.parameters")) { //$NON-NLS-1$
                            set = messageboxReturnParameters;
                        }
                        else if(name.equals("instruction.parameters")) { //$NON-NLS-1$
                            set = instructionParameters;
                        }
                        else if(name.equals("instruction.options")) { //$NON-NLS-1$
                            set = instructionOptions;
                        }
                        else if(name.equals("callbacks")) { //$NON-NLS-1$
                            set = callbacks;
                        }

                        for (int i = 0; i < values.length; i++) {
                            int m = values[i].indexOf('^');
                            int n = values[i].indexOf('~');
                            if(values[i].charAt(0) == '-') {
                                set.remove(values[i].substring(1));
                            }
                            else if(m > 0) {
                                String oldValue = values[i].substring(0,m);
                                String newValue = values[i].substring(m+1);
                                List list2 = (List)mNewerKeywordsMap.get(oldValue);
                                if(list2 == null) {
                                    list2 = new ArrayList();
                                    list2.add(oldValue);
                                    mNewerKeywordsMap.put(oldValue,list2);
                                }
                                if(!list2.contains(newValue)) {
                                    list2.add(newValue);
                                }
                                set.add(oldValue);
                                set.add(newValue);
                            }
                            else if(n > 0) {
                                String oldValue = values[i].substring(0,n);
                                String newValue = values[i].substring(n+1);
                                List list2 = (List)mNewerKeywordsMap.get(oldValue);
                                if(list2 == null) {
                                    list2 = new ArrayList();
                                    mNewerKeywordsMap.put(oldValue,list2);
                                }
                                else {
                                    list2.remove(oldValue);
                                }
                                if(!list2.contains(newValue)) {
                                    list2.add(newValue);
                                }
                                set.remove(oldValue);
                                set.add(newValue);
                            }
                            else {
                                if(values[i].charAt(0) == '+') {
                                    values[i] = values[i].substring(1);
                                }
                                set.add(values[i]);
                            }
                        }
                    }
                }
            }
        }

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

        set = new CaseInsensitiveSet();
        if(!Common.isEmpty(NSISPreferences.INSTANCE.getNSISHome())) {
            Set keySet = NSISPreferences.INSTANCE.getNSISDefaultSymbols().keySet();
            StringBuffer buf = new StringBuffer("${"); //$NON-NLS-1$
            for (Iterator iter = keySet.iterator(); iter.hasNext();) {
                set.add(buf.append((String)iter.next()).append("}").toString()); //$NON-NLS-1$
                buf.setLength(2);
            }
        }
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(SYMBOLS,temp2);

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

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(ALL_CONSTANTS), getKeywordsGroup(ALL_VARIABLES)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_CONSTANTS_AND_VARIABLES,temp2);

        temp2 = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(ALL_CONSTANTS), getKeywordsGroup(ALL_VARIABLES),
                                                         getKeywordsGroup(SYMBOLS), getKeywordsGroup(PREDEFINES)});
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(ALL_CONSTANTS_VARIABLES_AND_SYMBOLS,temp2);

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

        set = getValidKeywords(hkeyParameters);
        mAllKeywordsSet.addAll(set);
        temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY);
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(HKEY_PARAMETERS,temp2);

        set = getValidKeywords(messageboxOptionParameters);
        mAllKeywordsSet.addAll(set);
        temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY);
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(MESSAGEBOX_OPTION_PARAMETERS,temp2);

        set = getValidKeywords(messageboxReturnParameters);
        mAllKeywordsSet.addAll(set);
        temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY);
        Arrays.sort(temp2, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(MESSAGEBOX_RETURN_PARAMETERS,temp2);

        set = getValidKeywords(instructionParameters);
        mAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (temp2 = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        temp2 = (String[])Common.appendArray(temp2, getKeywordsGroup(HKEY_PARAMETERS));
        temp2 = (String[])Common.appendArray(temp2, getKeywordsGroup(MESSAGEBOX_OPTION_PARAMETERS));
        temp2 = (String[])Common.appendArray(temp2, getKeywordsGroup(MESSAGEBOX_RETURN_PARAMETERS));
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

        File cacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISKeywords.class.getName()+".Plugins.ser"); //$NON-NLS-1$
        if(cacheFile.exists() && cacheFile.isFile()) {
            cacheFile.delete();
        }
        NSISPluginManager.INSTANCE.loadDefaultPlugins();
        String[] plugins = NSISPluginManager.INSTANCE.getDefaultPluginNames();
        Arrays.sort(plugins, String.CASE_INSENSITIVE_ORDER);
        mKeywordGroupsMap.put(PLUGINS,plugins);

        notifyListeners(monitor);
    }

    private Set getValidKeywords(Set keywordSet)
    {
        HashSet set = new HashSet();
        for (Iterator iter = keywordSet.iterator(); iter.hasNext();) {
            String keyword = (String)iter.next();
            String mappedKeyword = getKeyword(keyword, false);
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

    public void notifyListeners(IProgressMonitor monitor)
    {
        if(mListeners.size() > 0) {
            if(monitor != null) {
                monitor = new SubProgressMonitor(monitor, 10);
                monitor.beginTask(EclipseNSISPlugin.getResourceString("updating.keywords.message"), mListeners.size()); //$NON-NLS-1$
            }
            INSISKeywordsListener[] listeners = (INSISKeywordsListener[])mListeners.toArray(new INSISKeywordsListener[mListeners.size()]);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].keywordsChanged();
                if(monitor != null) {
                    monitor.worked(1);
                }
            }
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
        return getKeyword(name, true);
    }

    public String getKeyword(String name, boolean getNewest)
    {
        if((!mAllKeywordsSet.contains(name) || getNewest) && mNewerKeywordsMap.containsKey(name)) {
            List list = (List)mNewerKeywordsMap.get(name);
            if(list.size() > 0) {
                if(getNewest) {
                    return getKeyword((String)list.get(list.size()-1), getNewest);
                }
                else {
                    return (String)list.get(0);
                }
            }
        }
        return name;
    }

    public VariableMatcher createVariableMatcher()
    {
        return new VariableMatcher();
    }

    public class VariableMatcher
    {
        private int mPotentialMatchIndex = -1;
        private String mText = null;
        private String[] mKeywords = getKeywordsGroup(ALL_CONSTANTS_VARIABLES_AND_SYMBOLS);

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
}
