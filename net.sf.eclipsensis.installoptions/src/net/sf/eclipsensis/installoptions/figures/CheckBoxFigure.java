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

import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class CheckBoxFigure extends ButtonFigure
{
    protected boolean mState = false;
    protected boolean mLeftText = false;

    public CheckBoxFigure(GraphicalEditPart editPart)
    {
        super(editPart);
    }
    
    public void setState(boolean state)
    {
        mState = state;
    }
    
    public void setLeftText(boolean leftText)
    {
        mLeftText = leftText;
    }
    
    public boolean isLeftText()
    {
        return mLeftText;
    }

    public int getDefaultStyle()
    {
        return SWT.LEFT|SWT.CHECK;
    }

    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        Button button = (Button)super.createSWTControl(parent);
        button.setSelection(mState);
        if(mLeftText) {
            int style = WinAPI.GetWindowLong(button.handle,WinAPI.GWL_STYLE);
            WinAPI.SetWindowLong(button.handle,WinAPI.GWL_STYLE,style|WinAPI.BS_LEFTTEXT);
        }
        return button;
    }
}
