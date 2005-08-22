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

import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class TextFigure extends EditableElementFigure
{
    private boolean mMultiLine;
    private boolean mReadOnly;
    private boolean mNoWordWrap;
    private boolean mOnlyNumbers;
    
    public TextFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public TextFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }
    
    protected void init(IPropertySource propertySource)
    {
        List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setOnlyNumbers(flags != null && flags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
        setMultiLine(flags != null && flags.contains(InstallOptionsModel.FLAGS_MULTILINE));
        setNoWordWrap(flags != null && flags.contains(InstallOptionsModel.FLAGS_NOWORDWRAP));
        setHScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_HSCROLL));
        setVScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_VSCROLL));
        setReadOnly(flags != null && flags.contains(InstallOptionsModel.FLAGS_READONLY));
        super.init(propertySource);
   }

    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }

    public void setOnlyNumbers(boolean onlyNumbers)
    {
        mOnlyNumbers = onlyNumbers;
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
    
    /**
     * @return
     */
    protected Control createSWTControl(Composite parent, int style)
    {
        Text text = new Text(parent, style);
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
            if(!mNoWordWrap && !isHScroll()) {
                style |= SWT.WRAP;
            }
        }
        if(mReadOnly) {
            style |= SWT.READ_ONLY;
        }
        return style;
    }

    protected String getTheme()
    {
        return "EDIT"; //$NON-NLS-1$
    }

    protected int getThemePartId()
    {
        return WinAPI.EP_EDITTEXT;
    }

    protected int getThemeStateId()
    {
        if(isDisabled()) {
            return WinAPI.ETS_DISABLED;
        }
        if(mReadOnly) {
            return WinAPI.ETS_READONLY;
        }
        return WinAPI.ETS_NORMAL;
    }

    protected boolean isNeedsTheme()
    {
        return true;
    }
}
