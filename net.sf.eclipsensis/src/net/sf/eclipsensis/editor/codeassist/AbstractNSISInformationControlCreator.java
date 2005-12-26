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

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class AbstractNSISInformationControlCreator implements IInformationControlCreator,INSISConstants
{
    protected int mStyle = SWT.NONE;
    protected DefaultInformationControl.IInformationPresenter mInformationPresenter = new DefaultInformationControl.IInformationPresenter() {
        public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight)
        {
            hoverInfo = hoverInfo.trim();
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

    public AbstractNSISInformationControlCreator(int style)
    {
        mStyle = style;
    }

    public IInformationControl createInformationControl(Shell parent)
    {
        return new DefaultInformationControl(parent,mStyle,mInformationPresenter);
    }
}