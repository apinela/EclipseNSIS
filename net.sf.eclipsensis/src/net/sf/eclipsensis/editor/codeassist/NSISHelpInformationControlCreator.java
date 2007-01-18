/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.DefaultInformationControl.IInformationPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

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

    protected IInformationPresenter createInformationPresenter()
    {
        return new WrappingInformationPresenter("\t\t") { //$NON-NLS-1$
            public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight)
            {
                hoverInfo = super.updatePresentation(display, hoverInfo, presentation, maxWidth, maxHeight);
                int n = hoverInfo.indexOf(' ');
                if(n <= 0) {
                    n = hoverInfo.length();
                }
                presentation.addStyleRange(new StyleRange(0,n,
                                                display.getSystemColor(SWT.COLOR_INFO_FOREGROUND),
                                                display.getSystemColor(SWT.COLOR_INFO_BACKGROUND),
                                                SWT.BOLD));
                return hoverInfo;
            }
        };
    }
}