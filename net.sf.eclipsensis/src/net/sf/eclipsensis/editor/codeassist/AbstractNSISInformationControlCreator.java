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

import java.util.StringTokenizer;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class AbstractNSISInformationControlCreator implements IInformationControlCreator,INSISConstants
{
    private static final String BREAK_CHARS = ",;|-.?!:";
    protected int mStyle = SWT.NONE;
    protected DefaultInformationControl.IInformationPresenter mInformationPresenter = new DefaultInformationControl.IInformationPresenter() {
        public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight)
        {
            hoverInfo = hoverInfo.trim();
            GC gc = new GC(display);
            try {
                maxWidth -= gc.getFontMetrics().getAverageCharWidth();
                Point p = gc.stringExtent(hoverInfo);
                if (p.x > maxWidth) {
                    StringBuffer buf = new StringBuffer("");
                    StringTokenizer st = new StringTokenizer(hoverInfo, "\r\n");
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        p = gc.stringExtent(token);
                        if (p.x <= maxWidth) {
                            buf.append(token);
                        }
                        else {
                            //Wrap
                            char[] chars = token.toCharArray();
                            int start = 0;
                            int last = -1;
                            boolean previousWasWhitespace = false;
                            boolean init = false;
                            int index;
                            for (int i = 0; i < chars.length; i++) {
                                if (Character.isWhitespace(chars[i])) {
                                    previousWasWhitespace = true;
                                    continue;
                                }
                                else if (((index = BREAK_CHARS.indexOf(chars[i])) >= 0) || previousWasWhitespace) {
                                    p = gc.stringExtent(new String(chars, start, i - start + 1));
                                    if (p.x <= maxWidth) {
                                        last = (index >= 0?i:i - 1);
                                    }
                                    else {
                                        if (init) {
                                            buf.append("\n");
                                        }
                                        if (last >= start) {
                                            buf.append(new String(chars, start, last - start + 1));
                                            start = last + 1;
                                            i = start;
                                        }
                                        else {
                                            buf.append(new String(chars, start, i - start + 1));
                                            start = i;
                                            i--;
                                        }
                                        init = true;
                                        last = -1;
                                    }
                                    previousWasWhitespace = false;
                                }
                            }
                            if (init) {
                                buf.append("\n");
                            }
                            String s = new String(chars, start, chars.length - start);
                            if (gc.stringExtent(s).x > maxWidth && last >= start) {
                                buf.append(new String(chars, start, last - start + 1)).append("\n");
                                start = last + 1;
                                s = new String(chars, start, chars.length - start);
                            }
                            buf.append(s);
                        }
                        if (st.hasMoreTokens()) {
                            buf.append("\n");
                        }
                    }
                    hoverInfo = buf.toString();
                }
            }
            finally {
                gc.dispose();
            }            
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