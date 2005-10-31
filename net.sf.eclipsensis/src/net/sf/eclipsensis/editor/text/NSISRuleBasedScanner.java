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

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.IPropertyAdaptable;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

public abstract class NSISRuleBasedScanner extends BufferedRuleBasedScanner implements NSISScanner, IPropertyAdaptable
{
    protected IPreferenceStore mPreferenceStore;
    
    public NSISRuleBasedScanner(IPreferenceStore preferenceStore)
    {
        mPreferenceStore = preferenceStore;
        reset();
    }

    protected boolean isCaseSensitive()
    {
        return false;
    }
    
    public final void reset()
    {
        reset(false);
    }
    
    public void reset(boolean full)
    {
        setDefaultReturnToken(getDefaultToken());
        List rules = new ArrayList();
        addRules(rules);
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
        if(!isCaseSensitive() && Character.isUpperCase((char)c)) {
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
            attr = syntaxStyle.createTextAttribute();
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
     * @return Returns the preferenceStore.
     */
    public IPreferenceStore getPreferenceStore()
    {
        return mPreferenceStore;
    }
    
    /**
     * @return
     */
    protected abstract void addRules(List rules);

    /**
     * @return
     */
    protected abstract IToken getDefaultToken();

    protected void addWords(WordRule wordRule, String preferenceName, String keywordsGroup)
    {
        boolean caseSensitive = isCaseSensitive();
        IToken token = createTokenFromPreference(preferenceName);
        String[] array = NSISKeywords.getInstance().getKeywordsGroup(keywordsGroup);
        if(!Common.isEmptyArray(array)) {
            for (int i = 0; i < array.length; i++) {
                wordRule.addWord((caseSensitive?array[i]:array[i].toLowerCase()), token);
            }
        }
    }
}