/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import java.util.List;

import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

public class NSISTemplateCodeScanner extends NSISCodeScanner
{
    protected NSISWordPatternRule mTemplateVariableRule = null;

    /**
     * @param preferenceStore
     */
    public NSISTemplateCodeScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#addRules(java.util.List, org.eclipse.jface.text.rules.IToken)
     */
    protected void addRules(List rules, IToken defaultToken)
    {
        rules.add(getTemplateVariableRule(defaultToken));
        super.addRules(rules, defaultToken);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#reset(boolean)
     */
    public void reset(boolean full)
    {
        if(full) {
            mTemplateVariableRule = null;
        }
        super.reset(full);
    }
    
    protected IRule getTemplateVariableRule(IToken defaultToken)
    {
        if(mTemplateVariableRule == null) {
            synchronized(this) {
                if(mTemplateVariableRule == null) {
                    mTemplateVariableRule = new NSISWordPatternRule(new NSISWordDetector(){
                        private boolean mFoundEndSequence = false;
                        /*
                         * (non-Javadoc) Method declared on IWordDetector.
                         */
                        public boolean isWordStart(char character)
                        {
                            mFoundEndSequence = false;
                            return Character.isLetter(character);
                        }
                        
                        public boolean isWordPart(char character)
                        {
                            if(!mFoundEndSequence) {
                                if(character == '%') {
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
                    }, "%","%",new Token(new TextAttribute(JFaceResources.getColorRegistry().get(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR)))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return mTemplateVariableRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR)) {
            mTemplateVariableRule = null;
            reset();
        }
        else {
            super.adaptToProperty(store, property);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR)) {
            return true;
        }
        else {
            return super.canAdaptToProperty(store, property);
        }
    }
}