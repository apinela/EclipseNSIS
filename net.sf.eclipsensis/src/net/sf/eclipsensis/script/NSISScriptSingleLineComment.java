/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class NSISScriptSingleLineComment implements INSISScriptElement
{
    public static final String PREFIX_SEMICOLON = ";"; //$NON-NLS-1$
    public static final String PREFIX_HASH = "#"; //$NON-NLS-1$
    private String mPrefix = ";"; //$NON-NLS-1$
    private String mText = ""; //$NON-NLS-1$
    
    /**
     * @param text
     */
    public NSISScriptSingleLineComment(String text)
    {
        setText(text);
    }

    /**
     * @param prefix
     * @param text
     */
    public NSISScriptSingleLineComment(String prefix, String text)
    {
        setPrefix(prefix);
        setText(text);
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return mPrefix;
    }
    
    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        if(PREFIX_HASH.equals(prefix) || PREFIX_SEMICOLON.equals(prefix)) {
            mPrefix = prefix;
        }
    }

    /**
     * @return Returns the text.
     */
    public String getText()
    {
        return mText;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text)
    {
        mText = text;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    public void write(NSISScriptWriter writer)
    {
        writer.print(mPrefix);
        writer.println(mText);
    }
}
