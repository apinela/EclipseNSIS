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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class LineFigure extends SWTControlFigure
{
    public LineFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public void setBounds(Rectangle rect)
    {
        rect = rect.getCopy();
        if( (getStyle() & SWT.HORIZONTAL) > 0) {
            rect.height = 2;
        }
        else {
            rect.width = 2;
        }
        super.setBounds(rect);
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
