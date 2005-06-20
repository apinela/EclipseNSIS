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

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class LabelFigure extends UneditableElementFigure
{
    public LabelFigure(FigureCanvas canvas, IPropertySource propertySource)
    {
        super(canvas, propertySource);
    }

    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        Label label = new Label(parent, getStyle());
        label.setText(mText);
        return label;
    }

    /**
     * @return
     */
    public int getDefaultStyle()
    {
        return SWT.LEFT|SWT.WRAP;
    }
}
