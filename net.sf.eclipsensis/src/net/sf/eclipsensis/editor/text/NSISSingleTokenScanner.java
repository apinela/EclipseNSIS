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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IToken;


/**
 * Single token scanner.
 */
public abstract class NSISSingleTokenScanner extends NSISRuleBasedScanner
{
    protected String mProperty = null;

    /**
     * @param preferenceStore
     */
    public NSISSingleTokenScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#addRules(java.util.List, org.eclipse.jface.text.rules.IToken)
     */
    protected void addRules(List rules, IToken defaultToken)
    {
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(canAdaptToProperty(store, property)) {
            setDefaultReturnToken(getDefaultToken());
        }

    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return (mProperty!= null && mProperty.equals(property));
    }
}