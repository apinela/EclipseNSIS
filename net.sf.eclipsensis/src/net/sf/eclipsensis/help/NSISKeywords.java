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

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.eclipsensis.util.Common;

public class NSISKeywords
{
    public static final String[] ALL_KEYWORDS;
    public static final String[] SINGLELINE_COMPILETIME_COMMANDS;
    public static final String[] MULTILINE_COMPILETIME_COMMANDS;
    public static final String[] INSTALLER_ATTRIBUTES;
    public static final String[] COMMANDS;
    public static final String[] INSTRUCTIONS;
    public static final String[] INSTRUCTION_PARAMETERS;
    public static final String[] INSTRUCTION_OPTIONS;
    public static final String[] PREDEFINED_PATH_VARIABLES;
    public static final String[] PREDEFINED_VARIABLES;
    public static final String[] CALLBACKS;
    
    private static String[] appendArray(String[] array, String[] array2)
    {
        String[] newArray = new String[array.length+array2.length];
        System.arraycopy(array,0,newArray,0,array.length);
        System.arraycopy(array2,0,newArray,array.length,array2.length);
        return newArray;
    }

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISKeywords.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        String[] temp = Common.EMPTY_STRING_ARRAY;
        temp = appendArray(temp, (PREDEFINED_PATH_VARIABLES = Common.loadArrayProperty(bundle, "predefined.path.variables"))); //$NON-NLS-1$
        temp = appendArray(temp, Common.loadArrayProperty(bundle, "predefined.variables")); //$NON-NLS-1$
        PREDEFINED_VARIABLES = (String[])temp.clone();
        Arrays.sort(PREDEFINED_VARIABLES, String.CASE_INSENSITIVE_ORDER);
        
        temp = appendArray(temp, (SINGLELINE_COMPILETIME_COMMANDS = Common.loadArrayProperty(bundle, "singleline.compiletime.commands"))); //$NON-NLS-1$
        temp = appendArray(temp, (MULTILINE_COMPILETIME_COMMANDS = Common.loadArrayProperty(bundle, "multiline.compiletime.commands"))); //$NON-NLS-1$
        temp = appendArray(temp, (INSTALLER_ATTRIBUTES = Common.loadArrayProperty(bundle, "installer.attributes"))); //$NON-NLS-1$
        temp = appendArray(temp, (COMMANDS = Common.loadArrayProperty(bundle, "commands"))); //$NON-NLS-1$
        temp = appendArray(temp, (INSTRUCTIONS = Common.loadArrayProperty(bundle, "instructions"))); //$NON-NLS-1$
        temp = appendArray(temp, (INSTRUCTION_PARAMETERS = Common.loadArrayProperty(bundle, "instruction.parameters"))); //$NON-NLS-1$
        temp = appendArray(temp, (INSTRUCTION_OPTIONS = Common.loadArrayProperty(bundle, "instruction.options"))); //$NON-NLS-1$
        temp = appendArray(temp, (CALLBACKS = Common.loadArrayProperty(bundle, "callbacks"))); //$NON-NLS-1$
        ALL_KEYWORDS = temp;
        Arrays.sort(ALL_KEYWORDS, String.CASE_INSENSITIVE_ORDER);
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
