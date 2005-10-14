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

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;

import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

public class PathRequestFigure extends AbstractInstallOptionsFigure implements IEditableElementFigure
{
    public static final String BROWSE_BUTTON_TEXT = "..."; //$NON-NLS-1$
    public static final int BROWSE_BUTTON_WIDTH;
    public static final int SPACING;

    private TextFigure mTextFigure;
    private ButtonFigure mButtonFigure;
    
    static {
        Font f = Display.getDefault().getSystemFont();
        BROWSE_BUTTON_WIDTH = FigureUtility.dialogUnitsToPixelsX(15,f);
        SPACING = FigureUtility.dialogUnitsToPixelsX(3,f);
    }
    
    /**
     * 
     */
    public PathRequestFigure(Composite parent, final IPropertySource propertySource)
    {
        super();
        setLayoutManager(new XYLayout());
        final Rectangle[] newBounds = calculateBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        mTextFigure = new TextFigure(parent, new PropertySourceWrapper(propertySource){
                public Object getPropertyValue(Object id)
                {
                    if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                        return newBounds[0];
                    }
                    else {
                        return super.getPropertyValue(id);
                    }
                }
            });
        mButtonFigure = new ButtonFigure(parent, new PropertySourceWrapper(propertySource){
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                    return newBounds[1];
                }
                else if( InstallOptionsModel.PROPERTY_TEXT.equals(id)) {
                    return BROWSE_BUTTON_TEXT;
                }
                else if( InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
                    List flags = new ArrayList((List)propertySource.getPropertyValue(id));
                    flags.removeAll(SCROLL_FLAGS);
                    return flags;
                }
                else {
                    return super.getPropertyValue(id);
                }
            }
        });
        add(mTextFigure);
        add(mButtonFigure);
    }
    
    public TextFigure getTextFigure()
    {
        return mTextFigure;
    }
    
    public ButtonFigure getButtonFigure()
    {
        return mButtonFigure;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#setDisabled(boolean)
     */
    public void setDisabled(boolean disabled)
    {
        mTextFigure.setDisabled(disabled);
        mButtonFigure.setDisabled(disabled);
    }

    public void setState(String state)
    {
        mTextFigure.setState(state);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#refresh()
     */
    public void refresh()
    {
        mTextFigure.refresh();
        mButtonFigure.refresh();
    }

    private Rectangle[] calculateBounds(Rectangle rect)
    {
        return new Rectangle[]{new Rectangle(0,0,Math.max(0,rect.width-(BROWSE_BUTTON_WIDTH+SPACING)),rect.height),
                               new Rectangle(Math.max(0,rect.width-BROWSE_BUTTON_WIDTH),0,Math.min(rect.width,BROWSE_BUTTON_WIDTH),rect.height)};
        
    }

    public void setBounds(Rectangle rect)
    {
        Rectangle[] newBounds = calculateBounds(rect);
        getLayoutManager().setConstraint(mTextFigure,newBounds[0]);
        getLayoutManager().setConstraint(mButtonFigure,newBounds[1]);
        super.setBounds(rect);
    }

    public void setHScroll(boolean hScroll)
    {
        mTextFigure.setHScroll(hScroll);
    }

    public void setVScroll(boolean vScroll)
    {
        mTextFigure.setVScroll(vScroll);
    }

    public boolean isDisabled()
    {
        return mTextFigure.isDisabled();
    }

    public boolean isHScroll()
    {
        return mTextFigure.isHScroll();
    }

    public boolean isVScroll()
    {
        return mTextFigure.isVScroll();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IEditableElementFigure#getState()
     */
    public String getState()
    {
        return mTextFigure.getState();
    }

    public Rectangle getDirectEditArea()
    {
        return getTextFigure().getClientArea();
    }
}
