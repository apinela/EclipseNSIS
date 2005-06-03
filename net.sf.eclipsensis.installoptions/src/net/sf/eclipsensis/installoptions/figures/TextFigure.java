/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class TextFigure extends EditableElementFigure
{
    private boolean mHScroll = false;
    private boolean mVScroll = false;
    private boolean mMultiLine = false;
    private boolean mReadOnly = false;
    private boolean mNoWordWrap = false;
    private boolean mOnlyNumbers = false;
    
    public TextFigure(GraphicalEditPart editPart)
    {
        super(editPart);
    }

    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }

    public void setOnlyNumbers(boolean onlyNumbers)
    {
        mOnlyNumbers = onlyNumbers;
    }
    public boolean isHScroll()
    {
        return mHScroll;
    }
    
    public void setHScroll(boolean scroll)
    {
        mHScroll = scroll;
    }
    
    public boolean isMultiLine()
    {
        return mMultiLine;
    }
    
    public void setMultiLine(boolean multiLine)
    {
        mMultiLine = multiLine;
    }
    
    public boolean isNoWordWrap()
    {
        return mNoWordWrap;
    }
    
    public void setNoWordWrap(boolean noWordWrap)
    {
        mNoWordWrap = noWordWrap;
    }
    
    public boolean isReadOnly()
    {
        return mReadOnly;
    }
    
    public void setReadOnly(boolean readOnly)
    {
        mReadOnly = readOnly;
    }
    
    public boolean isVScroll()
    {
        return mVScroll;
    }
    
    public void setVScroll(boolean scroll)
    {
        mVScroll = scroll;
    }
    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        Text text = new Text(parent, getStyle());
        text.setText(getState());
        return text;
    }

    /**
     * @return
     */
    public int getDefaultStyle()
    {
        int style = SWT.BORDER;
        if(mMultiLine) {
            style |= SWT.MULTI;
            if(!mNoWordWrap && !mHScroll) {
                style |= SWT.WRAP;
            }
        }
        if(mHScroll) {
            style |= SWT.H_SCROLL;
        }
        if(mVScroll) {
            style |= SWT.V_SCROLL;
        }
        if(mReadOnly) {
            style |= SWT.READ_ONLY;
        }
        return style;
    }
}
