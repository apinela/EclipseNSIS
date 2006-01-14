/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.help.NSISHelpURLProvider;

public class NSISHelpInformationControlCreator extends NSISInformationControlCreator
{
    public NSISHelpInformationControlCreator(String[] ids)
    {
        super(ids);
    }

    public NSISHelpInformationControlCreator(String[] ids, int style)
    {
        super(ids, style);
    }

    protected boolean shouldBuildStatusText()
    {
        boolean b = super.shouldBuildStatusText();
        if(b) {
            b = NSISHelpURLProvider.getInstance().isNSISHelpAvailable();
        }
        return b;
    }
}