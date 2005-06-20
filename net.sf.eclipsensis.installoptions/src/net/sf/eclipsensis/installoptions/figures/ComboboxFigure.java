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

import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.views.properties.IPropertySource;

public class ComboboxFigure extends Figure implements  IListItemsFigure
{
    private FigureCanvas mCanvas;
    private ListFigure mListFigure;
    private ComboFigure mComboFigure;
    private int mComboHeight;
    
    /**
     * 
     */
    public ComboboxFigure(FigureCanvas canvas, IPropertySource propertySource)
    {
        super();
        mCanvas = canvas;
        Combo cb = new Combo(mCanvas,SWT.DROP_DOWN);
        cb.setVisible(false);
        cb.setBounds(-100,-100,10,10);
        Point p = cb.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        cb.dispose();
        mComboHeight = p.y;
        
        setLayoutManager(new XYLayout());
        final Rectangle[] bounds = calculateBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        mComboFigure = new ComboFigure(canvas, new PropertySourceWrapper(propertySource){
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                    return bounds[0];
                }
                else {
                    return super.getPropertyValue(id);
                }
            }
        });
        mListFigure = new ListFigure(canvas, new PropertySourceWrapper(propertySource){
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                    return bounds[1];
                }
                else {
                    return super.getPropertyValue(id);
                }
            }
        });
        mListFigure.setStyle(SWT.BORDER|SWT.SINGLE);
        add(mComboFigure);
        add(mListFigure);
    }
        
    public ListFigure getListFigure()
    {
        return mListFigure;
    }
    
    public ComboFigure getComboFigure()
    {
        return mComboFigure;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#setDisabled(boolean)
     */
    public void setDisabled(boolean disabled)
    {
        mListFigure.setDisabled(disabled);
        mComboFigure.setDisabled(disabled);
    }

    public void setState(String state)
    {
        mComboFigure.setState(state);
        mListFigure.setState(state);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#refresh()
     */
    public void refresh()
    {
        mComboFigure.refresh();
        mListFigure.refresh();
    }

    private Rectangle[] calculateBounds(Rectangle rect)
    {
        Rectangle rect1 = new Rectangle(0,0,rect.width,Math.min(mComboHeight,rect.height));
        return new Rectangle[] {rect1,
                                new Rectangle(0,Math.max(0,rect1.height-1),rect.width,Math.max(0,rect.height-rect1.height+1))};
    }
    
    public void setBounds(Rectangle rect)
    {
        Rectangle[] bounds = calculateBounds(rect);
        getLayoutManager().setConstraint(mComboFigure,bounds[0]);
        getLayoutManager().setConstraint(mListFigure,bounds[1]);
        super.setBounds(rect);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IEditableElementFigure#getState()
     */
    public String getState()
    {
        return mComboFigure.getState();
    }
    
    public int getStyle()
    {
        return mComboFigure.getStyle();
    }

    public List getListItems()
    {
        return mListFigure.getListItems();
    }
    
    public void setListItems(List listItems)
    {
        mListFigure.setListItems(listItems);
    }
}
