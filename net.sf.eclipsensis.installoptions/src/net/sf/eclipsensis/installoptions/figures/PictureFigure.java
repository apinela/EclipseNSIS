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

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;

public class PictureFigure extends ImageFigure implements IInstallOptionsFigure
{
    private LineBorder mLineBorder = new LineBorder(ColorManager.getColor(ColorManager.BLACK)){
        public void paint(IFigure figure, Graphics graphics, Insets insets) 
        {
            graphics.setLineStyle(Graphics.LINE_DASHDOT);
            super.paint(figure,graphics,insets);
        }
    };

    public PictureFigure(IPropertySource propertySource)
    {
        super();
        setImage((Image)propertySource.getPropertyValue(InstallOptionsPicture.PROPERTY_IMAGE));
        List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
        setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
    }

    public void refresh()
    {
    }

    public void setBounds(org.eclipse.draw2d.geometry.Rectangle rect)
    {
        Dimension dim = getSize();
        if(dim.width != rect.width || dim.height != rect.height) {
            Image image = getImage();
            if(image != null) {
                if(rect.width > image.getBounds().width || rect.height > image.getBounds().height) {
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
