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

import java.util.*;

import net.sf.eclipsensis.settings.IPropertyAdaptable;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;

public abstract class NSISRuleBasedScanner extends BufferedRuleBasedScanner implements NSISScanner, IPropertyAdaptable
{
    protected IPreferenceStore mPreferenceStore = null;
    
    public NSISRuleBasedScanner(IPreferenceStore preferenceStore)
    {
        mPreferenceStore = preferenceStore;
        reset();
    }

    /**
     * @param defaultToken
     */
    protected void reset()
    {
        IToken defaultToken = getDefaultToken();
        setDefaultReturnToken(defaultToken);
        List rules = new ArrayList();
        addRules(rules, fDefaultReturnToken);
        rules.add(new WhitespaceRule(new NSISWhitespaceDetector()));
        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ICharacterScanner#read()
     */
    public int read()
    {
        int c = super.read();
        if(Character.isUpperCase((char)c)) {
            c = Character.toLowerCase((char)c);
        }
        return c;
    }

    public int getOffset()
    {
        return fOffset;
    }
    
    protected IToken createTokenFromPreference(String name)
    {
        TextAttribute attr = null;
        NSISSyntaxStyle syntaxStyle = null;
        String text = mPreferenceStore.getString(name);
        if(!Common.isEmpty(text)) {
            try {
                syntaxStyle = NSISSyntaxStyle.parse(text);
            }
            catch(Exception ex) {
                syntaxStyle = null;
            }
        }
        if(syntaxStyle != null) {
            int style = (syntaxStyle.mBold?SWT.BOLD:0) | (syntaxStyle.mItalic?SWT.ITALIC:0);
            attr = new TextAttribute(ColorManager.getColor(syntaxStyle.mForeground),
                                     ColorManager.getColor(syntaxStyle.mBackground),
                                     style);
        }
        
        return (attr == null?fDefaultReturnToken:new Token(attr));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
     */
    public void setRange(IDocument document, int offset, int length)
    {
        super.setRange(document, offset, length);
        Arrays.sort(fDelimiters,new Comparator() {
            public int compare(Object a, Object b)
            {
                return ((char[])b).length-((char[])a).length;
            }
        });
    }
 
    /**
     * @return
     */
    protected abstract void addRules(List rules, IToken defaultToken);

    /**
     * @return
     */
    protected abstract IToken getDefaultToken();
}