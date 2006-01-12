/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class CheckBoxFigure extends ButtonFigure
{
    protected boolean mState;
    protected boolean mLeftText;

    public CheckBoxFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public CheckBoxFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    protected void init(IPropertySource propertySource)
    {
        List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setLeftText(flags != null && flags.contains(InstallOptionsModel.FLAGS_RIGHT));
        setState(InstallOptionsModel.STATE_CHECKED.equals(propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_STATE)));
        super.init(propertySource);
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
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Button button = (Button)super.createUneditableSWTControl(parent, style);
        button.setSelection(mState);
        if(mLeftText) {
            style = WinAPI.GetWindowLong(button.handle,WinAPI.GWL_STYLE);
            WinAPI.SetWindowLong(button.handle,WinAPI.GWL_STYLE,style|WinAPI.BS_LEFTTEXT);
        }
        return button;
    }
}
