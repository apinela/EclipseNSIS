/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.List;

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

public class NSISCodeScanner extends NSISStringScanner
{
    private NSISHexNumberRule mHexNumberRule;
    private NumberRule mNumberRule;
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
    protected void addRules(List rules, IToken defaultToken)
    {
        rules.add(getCompiletimeCommandsRule(defaultToken));
        rules.add(getKeywordsRule(defaultToken));
        rules.add(getInstructionOptionsRule(defaultToken));
        rules.add(getCallbacksRule(defaultToken));
        rules.add(getSymbolsRule(defaultToken));
        rules.add(getVariablesRule(defaultToken));
        rules.add(getHexNumberRule(defaultToken));
        rules.add(getNumberRule(defaultToken));
    }

    /**
     * @return
     */
    protected IToken getDefaultToken()
    {
        IToken defaultToken= new Token(new TextAttribute(ColorManager.getColor(ColorManager.DEFAULT)));
        return defaultToken;
    }

    protected IRule getCallbacksRule(IToken defaultToken)
    {
        if(mCallbacksRule == null) {
            synchronized (this) {
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
                            return super.isWordPart(character) || character == '.';
                        }
                    }, defaultToken);
                    addWords(mCallbacksRule, INSISPreferenceConstants.CALLBACKS_STYLE, NSISKeywords.CALLBACKS);
                }
            }
        }
        return mCallbacksRule;
    }

    protected IRule getInstructionOptionsRule(IToken defaultToken)
    {
        if(mInstructionOptionsRule == null) {
            synchronized(this) {
                if(mInstructionOptionsRule == null) {
                    mInstructionOptionsRule = new NSISWordRule(new NSISWordDetector(){
                        /*
                         * (non-Javadoc) Method declared on IWordDetector.
                         */
                        public boolean isWordStart(char character)
                        {
                            return (character == '/');
                        }
                    }, defaultToken);
                    addWords(mInstructionOptionsRule, INSISPreferenceConstants.INSTRUCTION_OPTIONS_STYLE, NSISKeywords.INSTRUCTION_OPTIONS);
                }
            }
        }
        return mInstructionOptionsRule;
    }

    protected IRule getKeywordsRule(IToken defaultToken)
    {
        if(mKeywordsRule == null) {
            synchronized(this) {
                if(mKeywordsRule == null) {
                    mKeywordsRule = new NSISWordRule(new NSISWordDetector(){
                        /*
                         * (non-Javadoc) Method declared on IWordDetector.
                         */
                        public boolean isWordStart(char character)
                        {
                            return Character.isLetter(character);
                        }
                    }, defaultToken);
                    addWords(mKeywordsRule, INSISPreferenceConstants.INSTALLER_ATTRIBUTES_STYLE, NSISKeywords.INSTALLER_ATTRIBUTES);
                    addWords(mKeywordsRule, INSISPreferenceConstants.COMMANDS_STYLE, NSISKeywords.COMMANDS);
                    addWords(mKeywordsRule, INSISPreferenceConstants.INSTRUCTIONS_STYLE, NSISKeywords.INSTRUCTIONS);
                    addWords(mKeywordsRule, INSISPreferenceConstants.INSTRUCTION_PARAMETERS_STYLE, NSISKeywords.INSTRUCTION_PARAMETERS);
                }
            }
        }
        return mKeywordsRule;
    }

    protected IRule getCompiletimeCommandsRule(IToken other)
    {
        if(mCompileTimeCommandsRule == null) {
            synchronized(this) {
                if(mCompileTimeCommandsRule == null) {
                    mCompileTimeCommandsRule = new NSISWordRule(new NSISWordDetector(){
                        /*
                         * (non-Javadoc) Method declared on IWordDetector.
                         */
                        public boolean isWordStart(char character)
                        {
                            return (character == '!');
                        }
                    }, other);
                    addWords(mCompileTimeCommandsRule, INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE, NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS);
                    addWords(mCompileTimeCommandsRule, INSISPreferenceConstants.COMPILETIME_COMMANDS_STYLE, NSISKeywords.MULTILINE_COMPILETIME_COMMANDS);
                }
            }
        }
        return mCompileTimeCommandsRule;
    }

    protected IRule getNumberRule(IToken other)
    {
        if(mNumberRule == null) {
            synchronized(this) {
                if(mNumberRule == null) {
                    mNumberRule = new NumberRule(createTokenFromPreference(INSISPreferenceConstants.NUMBERS_STYLE));
                }
            }
        }
        return mNumberRule;
    }

    protected IRule getHexNumberRule(IToken other)
    {
        if(mHexNumberRule == null) {
            synchronized(this) {
                if(mHexNumberRule == null) {
                    mHexNumberRule = new NSISHexNumberRule(createTokenFromPreference(INSISPreferenceConstants.NUMBERS_STYLE));
                }
            }
        }
        return mHexNumberRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if (INSISPreferenceConstants.CALLBACKS_STYLE.equals(property)) {
            mCallbacksRule = null;
        }
        else if (INSISPreferenceConstants.INSTRUCTIONS_STYLE.equals(property)) {
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
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        if(INSISPreferenceConstants.CALLBACKS_STYLE.equals(property) ||
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