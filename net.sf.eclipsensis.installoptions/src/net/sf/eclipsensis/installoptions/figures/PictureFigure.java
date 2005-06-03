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

import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class PictureFigure extends ImageFigure implements IInstallOptionsFigure
{
    private LineBorder mLineBorder = new LineBorder(ColorManager.getColor(ColorManager.BLACK)){
        public void paint(IFigure figure, Graphics graphics, Insets insets) 
        {
            graphics.setLineStyle(Graphics.LINE_DASHDOT);
            super.paint(figure,graphics,insets);
        }
    };

    public void refresh()
    {
    }

    public void setBounds(org.eclipse.draw2d.geometry.Rectangle rect)
    {
        Dimension dim = getSize();
        if(dim.width != rect.width || dim.height != rect.height) {
            Image image = getImage();
            if(image != null) {
                Rectangle rect2 = image.getBounds();
                if(rect.width > rect2.width || rect.height > rect2.height) {
                    setBorder(mLineBorder);
                }
                else {
                    setBorder(null);
                }
            }
        }
        super.setBounds(rect);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#setDisabled(boolean)
     */
    public void setDisabled(boolean disabled)
    {
        setEnabled(!disabled);
    }

}
