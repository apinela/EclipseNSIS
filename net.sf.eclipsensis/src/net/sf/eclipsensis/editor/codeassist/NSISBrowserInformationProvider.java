/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.help.NSISHelpURLProvider;

public class NSISBrowserInformationProvider extends NSISInformationProvider
{
    protected Object getInformation(String word)
    {
        String info = NSISHelpURLProvider.getInstance().getKeywordHelp(word);
        if(info == null) {
            StringBuffer buf = new StringBuffer(NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX);
            Object obj = super.getInformation(word);
            if(obj != null) {
                buf.append(obj);
            }
            buf.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_SUFFIX);
            return buf.toString();
        }
        else {
            NSISBrowserUtility.updateColorStyles();
            return new NSISBrowserInformation(word, info);
        }
    }
}