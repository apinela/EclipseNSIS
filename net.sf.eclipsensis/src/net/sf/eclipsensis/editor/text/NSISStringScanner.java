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

import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WordRule;

public class NSISStringScanner extends NSISRuleBasedScanner
{
    private IToken mDefaultToken;
    protected NSISVariablesWordRule mVariablesWordRule;
    private NSISWordPatternRule mSymbolsRule;

    /**
     * @param preferenceStore
     */
    public NSISStringScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /**
     * @return
     */
    protected void addRules(List rules, IToken defaultToken)
    {
        rules.add(getSymbolsRule(defaultToken));
        rules.add(getVariablesRule(defaultToken));
    }

    /**
     * @param provider
     * @return
     */
    protected IToken getDefaultToken()
    {
        if(mDefaultToken == null) {
            synchronized(this) {
                if(mDefaultToken == null) {
                    mDefaultToken= createTokenFromPreference(INSISPreferenceConstants.STRINGS_STYLE);
                }
            }
        }
        return mDefaultToken;
    }

    protected IRule getSymbolsRule(IToken defaultToken)
    {
        if(mSymbolsRule == null) {
            synchronized(this) {
                if(mSymbolsRule == null) {
                    mSymbolsRule = new NSISWordPatternRule(new NSISWordDetector(){
                        /*
                         * (non-Javadoc) Method declared on IWordDetector.
                         */
                        public boolean isWordStart(char character)
                        {
                            return (character == '$');
                        }
                        
                        public boolean isWordPart(char character)
                        {
                            return super.isWordPart(character) || character == '{' || character == '}';
                        }
                    }, "${","}",createTokenFromPreference(INSISPreferenceConstants.SYMBOLS_STYLE));
                }
            }
        }
        return mSymbolsRule;
    }

    protected IRule getVariablesRule(IToken defaultToken)
    {
        if(mVariablesWordRule == null) {
            synchronized(this) {
                if(mVariablesWordRule == null) {
                    mVariablesWordRule = new NSISVariablesWordRule(createTokenFromPreference(INSISPreferenceConstants.PREDEFINED_VARIABLES_STYLE),
                                                                   createTokenFromPreference(INSISPreferenceConstants.USERDEFINED_VARIABLES_STYLE));
                }
            }
        }
        return mVariablesWordRule;
    }

    protected void addWords(WordRule wordRule, String preferenceName, String[] array)
    {
        IToken token = createTokenFromPreference(preferenceName);
        if(!Common.isEmptyArray(array)) {
            for (int i = 0; i < array.length; i++) {
                wordRule.addWord(array[i].toLowerCase(), token);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if (INSISPreferenceConstants.STRINGS_STYLE.equals(property)) {
            mDefaultToken = null;
        }
        else if (INSISPreferenceConstants.SYMBOLS_STYLE.equals(property)) {
            mSymbolsRule = null;
        }
        else if (INSISPreferenceConstants.USERDEFINED_VARIABLES_STYLE.equals(property) ||
                INSISPreferenceConstants.PREDEFINED_VARIABLES_STYLE.equals(property)) {
            mVariablesWordRule = null;
        }
        else {
            return;
        }
        reset();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return (INSISPreferenceConstants.STRINGS_STYLE.equals(property) ||
                INSISPreferenceConstants.SYMBOLS_STYLE.equals(property) ||
                INSISPreferenceConstants.USERDEFINED_VARIABLES_STYLE.equals(property) ||
                INSISPreferenceConstants.PREDEFINED_VARIABLES_STYLE.equals(property));
    }
}