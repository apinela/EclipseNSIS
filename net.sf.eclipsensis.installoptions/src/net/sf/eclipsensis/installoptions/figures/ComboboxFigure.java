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

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public class ComboboxFigure extends Figure implements  IListItemsFigure
{
    private GraphicalEditPart mEditPart;
    private ListFigure mListFigure;
    private ComboFigure mComboFigure;
    private List mListItems;
    
    /**
     * 
     */
    public ComboboxFigure(GraphicalEditPart editPart)
    {
        super();
        mEditPart = editPart;
        setLayoutManager(new XYLayout());
        mComboFigure = new ComboFigure(mEditPart);
        mListFigure = new ListFigure(mEditPart);
        mListFigure.setStyle(SWT.BORDER|SWT.SINGLE);
        mListFigure.setVisible(false);
        mComboFigure.setVisible(false);
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

    public void setBounds(Rectangle rect)
    {
        Point p = mComboFigure.getSWTPreferredSize();
        if(p != null) {
            mListFigure.setVisible(true);
            mComboFigure.setVisible(true);
            Rectangle rect1 = new Rectangle(0,0,rect.width,Math.min(p.y,rect.height));
            Rectangle rect2 = new Rectangle(0,Math.max(0,rect1.height-1),rect.width,Math.max(0,rect.height-rect1.height+1));
            getLayoutManager().setConstraint(mListFigure,rect2);
            getLayoutManager().setConstraint(mComboFigure,rect1);
            super.setBounds(rect);
        }
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
        return (mListItems==null?Collections.EMPTY_LIST:mListItems);
    }
    
    public void setListItems(List listItems)
    {
        mListFigure.setListItems(listItems);
    }
}
