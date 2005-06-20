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

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class UneditableElementFigure extends SWTControlFigure implements IUneditableElementFigure
{
    protected String mText;

    /**
     * @param editPart
     */
    public UneditableElementFigure(FigureCanvas canvas, IPropertySource propertySource)
    {
        super(canvas, propertySource);
    }
    
    protected void init(IPropertySource propertySource)
    {
        setText((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TEXT));
        super.init(propertySource);
   }
    
    public String getText()
    {
        return mText==null?"":mText; //$NON-NLS-1$
    }
    
    public void setText(String text) 
    {
        mText = text;
    }
}
