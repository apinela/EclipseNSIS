/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.List;

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

public class NSISCodeScanner extends NSISStringScanner
{
    private NSISHexNumberRule mHexNumberRule;
    private NumberRule mNumberRule;
    private NSISPluginRule mPluginsRule;
    private WordRule mCompileTimeCommandsRule;
    private WordRule mKeywordsRule;
    private WordRule mInstructionOptionsRule;
    private WordRule mCallbacksRule;

    /**
     * @param preferenceStore
     */
    public NSISCodeScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /**
     * @return
     */
    protected void addRules(List rules)
    {
        rules.add(getPluginsRule());
        rules.add(getCompiletimeCommandsRule());
        rules.add(getInstructionOptionsRule());
        rules.add(getCallbacksRule());
        rules.add(getSymbolsRule());
        rules.add(getVariablesRule());
        rules.add(getLangstringsRule());
        rules.add(getKeywordsRule());
        rules.add(getHexNumberRule());
        rules.add(getNumberRule());
    }

    /**
     * @return
     */
    protected IToken getDefaultToken()
    {
        return new Token(new TextAttribute(null));
    }

    protected synchronized IRule getCallbacksRule()
    {
        if(mCallbacksRule == null) {
            mCallbacksRule = new NSISWordRule(new NSISWordDetector(){
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    return (character == '.' || character == 'U' || character == 'u');
                }

                public boolean isWordPart(char character)
                {
                    return (Character.isLetter(character) || character == '_') || character == '.';
                }
            });
            addWords(mCallbacksRule, INSISPreferenceConstants.CALLBACKS_STYLE, NSISKeywords.CALLBACKS);
        }
        return mCallbacksRule;
    }

    protected synchronized IRule getInstructionOptionsRule()
    {
        if(mInstructionOptionsRule == null) {
            mInstructionOptionsRule = new NSISWordRule(new NSISWordDetector(){
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    return (character == '/');
                }
            });
            addWords(mInstructionOptionsRule, INSISPreferenceConstants.INSTRUCTION_OPTIONS_STYLE, NSISKeywords.INSTRUCTION_OPTIONS);
        }
        return mInstructionOptionsRule;
    }

    protected synchronized IRule getPluginsRule()
    {
        if(mPluginsRule == null) {
            mPluginsRule = new NSISPluginRule(new NSISWordDetector(){
                public boolean isWordPart(char character)
                {
                    return super.isWordPart(character) || NSISPluginRule.PLUGIN_CALL_VALID_CHARS.indexOf(character) >= 0;
                }

                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    return Character.isLetter(character);
                }
            }, createTokenFromPreference(INSISPreferenceConstants.PLUGINS_STYLE));
        }
        return mPluginsRule;
    }

    protected synchronized IRule getKeywordsRule()
    {
        if(mKeywordsRule == null) {
            mKeywordsRule = new NSISWordRule(new NSISWordDetector(){
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    return Character.isLetter(character);
                }
            }, fDefaultReturnToken);
            addWords(mKeywordsRule, INSISPreferenceConstants.INSTALLER_ATTRIBUTES_STYLE, NSISKeywords.INSTALLER_ATTRIBUTES);
            addWords(mKeywordsRule, INSISPreferenceConstants.COMMANDS_STYLE, NSISKeywords.COMMANDS);
            addWords(mKeywordsRule, INSISPreferenceConstants.INSTRUCTIONS_STYLE, NSISKeywords.INSTRUCTIONS);
            addWords(mKeywordsRule, INSISPreferenceConstants.INSTRUCTION_PARAMETERS_STYLE, NSISKeywords.INSTRUCTION_PARAMETERS);
        }
        return mKeywordsRule;
    }

    protected synchronized IRule getCompiletimeCommandsRule()
    {
        if(mCompileTimeCommandsRule == null) {
            mCompileTimeCommandsRule = new NSISWordRule(new NSISWordDetector(){
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    return (character == '!');
                }
            });
            addWords(mCompileTimeCommandsRule, INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE, NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS);
            addWords(mCompileTimeCommandsRule, INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE, NSISKeywords.MULTILINE_COMPILETIME_COMMANDS);
        }
        return mCompileTimeCommandsRule;
    }

    protected synchronized IRule getNumberRule()
    {
        if(mNumberRule == null) {
            mNumberRule = new NumberRule(createTokenFromPreference(INSISPreferenceConstants.NUMBERS_STYLE));
        }
        return mNumberRule;
    }

    protected synchronized IRule getHexNumberRule()
    {
        if(mHexNumberRule == null) {
            mHexNumberRule = new NSISHexNumberRule(createTokenFromPreference(INSISPreferenceConstants.NUMBERS_STYLE));
        }
        return mHexNumberRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if (INSISPreferenceConstants.PLUGINS_STYLE.equals(property)) {
            mPluginsRule = null;
        }
        else if (INSISPreferenceConstants.CALLBACKS_STYLE.equals(property)) {
            mCallbacksRule = null;
        }
        else if (INSISPreferenceConstants.INSTRUCTION_OPTIONS_STYLE.equals(property)) {
            mInstructionOptionsRule = null;
        }
        else if (INSISPreferenceConstants.INSTALLER_ATTRIBUTES_STYLE.equals(property) ||
                 INSISPreferenceConstants.COMMANDS_STYLE.equals(property) ||
                 INSISPreferenceConstants.INSTRUCTIONS_STYLE.equals(property) ||
                 INSISPreferenceConstants.INSTRUCTION_PARAMETERS_STYLE.equals(property)) {
            mKeywordsRule = null;
        }
        else if (INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE.equals(property)) {
            mCompileTimeCommandsRule = null;
        }
        else if (INSISPreferenceConstants.NUMBERS_STYLE.equals(property)) {
            mNumberRule = null;
            mHexNumberRule = null;
        }
        else {
            super.adaptToProperty(store, property);
            return;
        }
        reset();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#reset(boolean)
     */
    public void reset(boolean full)
    {
        if(full) {
            mCallbacksRule = null;
            mPluginsRule = null;
            mInstructionOptionsRule = null;
            mKeywordsRule = null;
            mCompileTimeCommandsRule = null;
            mNumberRule = null;
            mHexNumberRule = null;
        }
        super.reset(full);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        if(INSISPreferenceConstants.CALLBACKS_STYLE.equals(property) ||
           INSISPreferenceConstants.PLUGINS_STYLE.equals(property) ||
           INSISPreferenceConstants.INSTRUCTIONS_STYLE.equals(property) ||
           INSISPreferenceConstants.INSTRUCTION_OPTIONS_STYLE.equals(property) ||
           INSISPreferenceConstants.INSTRUCTION_PARAMETERS_STYLE.equals(property) ||
           INSISPreferenceConstants.INSTALLER_ATTRIBUTES_STYLE.equals(property) ||
           INSISPreferenceConstants.COMMANDS_STYLE.equals(property) ||
           INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE.equals(property) ||
           INSISPreferenceConstants.NUMBERS_STYLE.equals(property)) {
            return true;
        }
        else {
            return super.canAdaptToProperty(store, property);
        }
    }
}