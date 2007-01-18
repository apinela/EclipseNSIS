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

import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

public class NSISCommentScanner extends NSISRuleBasedScanner
{
    private NSISTaskTagRule mTaskTagsRule;
    private boolean mCaseSensitive;

   /**
     * @param preferenceStore
     */
    public NSISCommentScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /**
     * @return
     */
    protected IToken getDefaultToken()
    {
        return createTokenFromPreference(INSISPreferenceConstants.COMMENTS_STYLE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#isCaseSensitive()
     */
    protected boolean isCaseSensitive()
    {
        return mCaseSensitive;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#addRules(java.util.List, org.eclipse.jface.text.rules.IToken)
     */
    protected void addRules(List rules)
    {
        mCaseSensitive = getPreferenceStore().getBoolean(INSISPreferenceConstants.CASE_SENSITIVE_TASK_TAGS);
        IRule taskTagsRule = getTaskTagsRule();
        if(taskTagsRule != null) {
            rules.add(taskTagsRule);
        }
    }

    protected synchronized IRule getTaskTagsRule()
    {
        if(mTaskTagsRule == null) {
            mTaskTagsRule = new NSISTaskTagRule(createTokenFromPreference(INSISPreferenceConstants.TASK_TAGS_STYLE));
        }
        return mTaskTagsRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return property.equals(INSISPreferenceConstants.COMMENTS_STYLE) ||
               property.equals(INSISPreferenceConstants.TASK_TAGS_STYLE) ||
               property.equals(INSISPreferenceConstants.TASK_TAGS) ||
               property.equals(INSISPreferenceConstants.CASE_SENSITIVE_TASK_TAGS);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISPreferenceConstants.TASK_TAGS) ||
                property.equals(INSISPreferenceConstants.TASK_TAGS_STYLE) ||
                property.equals(INSISPreferenceConstants.CASE_SENSITIVE_TASK_TAGS)) {
            mTaskTagsRule = null;
        }
        else if(!property.equals(INSISPreferenceConstants.COMMENTS_STYLE)) {
            return;
        }
        reset();
    }
}