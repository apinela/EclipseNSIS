/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.List;

import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

public class NSISStringScanner extends NSISRuleBasedScanner
{
    private IToken mDefaultToken;
    protected NSISVariablesWordRule mVariablesWordRule;
    protected NSISWordPatternRule mSymbolsRule;
    protected NSISWordPatternRule mLangstringsRule;

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
    protected void addRules(List rules)
    {
        rules.add(getSymbolsRule());
        rules.add(getVariablesRule());
        rules.add(getLangstringsRule());
    }

    /**
     * @param provider
     * @return
     */
    protected synchronized IToken getDefaultToken()
    {
        if(mDefaultToken == null) {
            mDefaultToken= createTokenFromPreference(INSISPreferenceConstants.STRINGS_STYLE);
        }
        return mDefaultToken;
    }

    protected synchronized IRule getSymbolsRule()
    {
        if(mSymbolsRule == null) {
            mSymbolsRule = new NSISWordPatternRule(new NSISWordDetector(){
                private boolean mFoundEndSequence = false;
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    mFoundEndSequence = false;
                    return (Character.isLetterOrDigit(character) || character == '_');
                }
                
                public boolean isWordPart(char character)
                {
                    if(!mFoundEndSequence) {
                        if(character == '}') {
                            mFoundEndSequence = true;
                            return true;
                        }
                        else {
                            mFoundEndSequence = false;
                            return super.isWordPart(character);
                        }
                    }
                    else {
                        mFoundEndSequence = false;
                        return false;
                    }
                }
            }, "${","}",createTokenFromPreference(INSISPreferenceConstants.SYMBOLS_STYLE)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return mSymbolsRule;
    }

    protected synchronized IRule getLangstringsRule()
    {
        if(mLangstringsRule == null) {
            mLangstringsRule = new NSISWordPatternRule(new NSISWordDetector(){
                private boolean mFoundEndSequence = false;
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    mFoundEndSequence = false;
                    return (Character.isLetterOrDigit(character) || character == '_' || character == '^');
                }
                
                public boolean isWordPart(char character)
                {
                    if(!mFoundEndSequence) {
                        if(character == ')') {
                            mFoundEndSequence = true;
                            return true;
                        }
                        else {
                            mFoundEndSequence = false;
                            return super.isWordPart(character);
                        }
                    }
                    else {
                        mFoundEndSequence = false;
                        return false;
                    }
                }
            }, "$(",")",createTokenFromPreference(INSISPreferenceConstants.LANGSTRINGS_STYLE)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return mLangstringsRule;
    }

    protected synchronized IRule getVariablesRule()
    {
        if(mVariablesWordRule == null) {
            mVariablesWordRule = new NSISVariablesWordRule(createTokenFromPreference(INSISPreferenceConstants.PREDEFINED_VARIABLES_STYLE),
                                                           createTokenFromPreference(INSISPreferenceConstants.USERDEFINED_VARIABLES_STYLE));
        }
        return mVariablesWordRule;
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
        else if (INSISPreferenceConstants.LANGSTRINGS_STYLE.equals(property)) {
            mLangstringsRule = null;
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
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#reset(boolean)
     */
    public void reset(boolean full)
    {
        if(full) {
            mDefaultToken = null;
            mSymbolsRule = null;
            mLangstringsRule = null;
            mVariablesWordRule = null;
        }
        super.reset(full);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return (INSISPreferenceConstants.STRINGS_STYLE.equals(property) ||
                INSISPreferenceConstants.SYMBOLS_STYLE.equals(property) ||
                INSISPreferenceConstants.LANGSTRINGS_STYLE.equals(property) ||
                INSISPreferenceConstants.USERDEFINED_VARIABLES_STYLE.equals(property) ||
                INSISPreferenceConstants.PREDEFINED_VARIABLES_STYLE.equals(property));
    }
}