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

public class NSISScriptMultiLineComment implements INSISScriptElement
{
    private String mText = ""; //$NON-NLS-1$
    
    /**
     * @param text
     */
    public NSISScriptMultiLineComment(String text)
    {
        setText(text);
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
        writer.print("/*"); //$NON-NLS-1$
        writer.print(mText);
        writer.println("*/"); //$NON-NLS-1$
    }
}
