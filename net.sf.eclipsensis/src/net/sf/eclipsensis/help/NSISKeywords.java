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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.CaseInsensitiveSet;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.Version;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class NSISKeywords
{
    public static String[] ALL_KEYWORDS;
    public static String[] SINGLELINE_COMPILETIME_COMMANDS;
    public static String[] MULTILINE_COMPILETIME_COMMANDS;
    public static String[] INSTALLER_ATTRIBUTES;
    public static String[] COMMANDS;
    public static String[] INSTRUCTIONS;
    public static String[] INSTRUCTION_PARAMETERS;
    public static String[] INSTRUCTION_OPTIONS;
    public static String[] PREDEFINED_PATH_VARIABLES;
    public static String[] PREDEFINED_VARIABLES;
    public static String[] CALLBACKS;
    
    private static Map cKeywordsMap = new CaseInsensitiveMap();
    private static Set cAllKeywordsSet = new CaseInsensitiveSet();
    private static ArrayList cListeners = new ArrayList();
    private static IPropertyChangeListener cPropertyChangeListener  = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event)
        {
            if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
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
        NSISPreferences.getPreferences().getPreferenceStore().addPropertyChangeListener(cPropertyChangeListener);
        EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
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

        Map predefinedPathVariables = new CaseInsensitiveMap();
        Map predefinedVariables = new CaseInsensitiveMap();
        Map singlelineCompiletimeCommands = new CaseInsensitiveMap();
        Map multilineCompiletimeCommands = new CaseInsensitiveMap();
        Map installerAttributes = new CaseInsensitiveMap();
        Map commands = new CaseInsensitiveMap();
        Map instructions = new CaseInsensitiveMap();
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
                        if(name.equals("predefined.path.variables")) { //$NON-NLS-1$
                            map = predefinedPathVariables;
                        }
                        else if(name.equals("predefined.variables")) { //$NON-NLS-1$
                            map = predefinedVariables;
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
        
        cKeywordsMap.putAll(predefinedPathVariables);
        cKeywordsMap.putAll(predefinedVariables);
        cKeywordsMap.putAll(singlelineCompiletimeCommands);
        cKeywordsMap.putAll(multilineCompiletimeCommands);
        cKeywordsMap.putAll(installerAttributes);
        cKeywordsMap.putAll(commands);
        cKeywordsMap.putAll(instructions);
        cKeywordsMap.putAll(instructionParameters);
        cKeywordsMap.putAll(instructionOptions);
        cKeywordsMap.putAll(callbacks);
        
        String[] temp = Common.EMPTY_STRING_ARRAY;
        Set set = getValidKeywords(predefinedPathVariables);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (PREDEFINED_PATH_VARIABLES = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));
        
        set = getValidKeywords(predefinedVariables);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (String[])set.toArray(Common.EMPTY_STRING_ARRAY));
        PREDEFINED_VARIABLES = (String[])temp.clone();
        Arrays.sort(PREDEFINED_VARIABLES, String.CASE_INSENSITIVE_ORDER);
        
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

        set = getValidKeywords(instructionParameters);
        cAllKeywordsSet.addAll(set);
        temp = (String[])Common.appendArray(temp, (INSTRUCTION_PARAMETERS = (String[])set.toArray(Common.EMPTY_STRING_ARRAY)));

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
                for(int i=Math.max(mPotentialMatchIndex,0); i<PREDEFINED_VARIABLES.length; i++) {
                    int n = PREDEFINED_VARIABLES[i].compareToIgnoreCase(mText);
                    if(n < 0) {
                        continue;
                    }
                    else if(n >= 0) {
                        if(PREDEFINED_VARIABLES[i].regionMatches(true,0,mText,0,mText.length())) {
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
            return (mPotentialMatchIndex >= 0 && PREDEFINED_VARIABLES[mPotentialMatchIndex].equalsIgnoreCase(mText));
        }
    }
}
