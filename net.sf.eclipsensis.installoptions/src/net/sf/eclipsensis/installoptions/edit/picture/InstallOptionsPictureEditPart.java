/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.picture;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.figures.FigureUtility.NTFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsPicture;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsPictureEditPart extends InstallOptionsLabelEditPart
{
    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the 
            //WM_PRINT message (see SWTControl.getImage(Control)
            //XXX Remove once the cause (and fix) is known.
             return new NTPictureFigure(getInstallOptionsWidget());
        }
        else {
            return new PictureFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return null;
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return null;
    }
    
    //This is a hack because Windows NT Labels don't seem to respond to the 
    //WM_PRINT message (see SWTControl.getImage(Control)
    //XXX Remove once the cause (and fix) is known.
    private class NTPictureFigure extends NTFigure
    {
        protected ImageFigure mImageFigure;
        protected Image mImage;
        
        public NTPictureFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }
        
        protected void createChildFigures()
        {
            mImageFigure = new ImageFigure();
            mImageFigure.setBorder(new DashedLineBorder());
            add(mImageFigure);
        }

        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            setImage((Image)propertySource.getPropertyValue(InstallOptionsPicture.PROPERTY_IMAGE));
        }
        
        protected void setChildConstraints(Rectangle rect)
        {
            setConstraint(mImageFigure, new Rectangle(0,0,rect.width,rect.height));
        }

        protected Image getImage()
        {
            return mImage;
        }

        protected void setImage(Image image)
        {
            if(mImage != image) {
                mImage = image;
                refresh();
            }
        }

        public void refresh()
        {
            super.refresh();
            mImageFigure.setImage(mImage);
        }
    }
}
