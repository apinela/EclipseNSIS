/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class LineFigure extends SWTControlFigure
{
    public LineFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    protected void setControlBounds(Control control, int x, int y, int width, int height)
    {
        if( (getStyle() & SWT.HORIZONTAL) > 0) {
            height = 2;
        }
        else {
            width = 2;
        }
        super.setControlBounds(control, x, y, width, height);
    }

    protected void handleClickThrough(Control control)
    {
    }

    protected boolean isTransparentAt(int x, int y)
    {
        // TODO Auto-generated method stub
        if( (getStyle() & SWT.HORIZONTAL) > 0) {
            return y > TRANSPARENCY_TOLERANCE + 2;
        }
        else {
            return x > TRANSPARENCY_TOLERANCE + 2;
        }
    }

    public boolean isClickThrough()
    {
        return true;
    }

    protected Control createSWTControl(Composite parent, int style)
    {
        return new Label(parent, SWT.SEPARATOR|style);
    }

    public int getDefaultStyle()
    {
        return SWT.HORIZONTAL;
    }

    protected boolean supportsScrollBars()
    {
        return false;
    }

}
