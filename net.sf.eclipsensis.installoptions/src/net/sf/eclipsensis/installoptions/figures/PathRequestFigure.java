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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class PathRequestFigure extends Figure implements IEditableElementFigure
{
    public static final String BROWSE_BUTTON_TEXT = "..."; //$NON-NLS-1$
    public static final int BROWSE_BUTTON_WIDTH;
    public static final int SPACING;

    private GraphicalEditPart mEditPart;
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
    public PathRequestFigure(GraphicalEditPart editPart)
    {
        super();
        mEditPart = editPart;
        setLayoutManager(new XYLayout());
        mTextFigure = new TextFigure(mEditPart);
        mButtonFigure = new ButtonFigure(mEditPart);
        mButtonFigure.setText(BROWSE_BUTTON_TEXT);
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

    public void setBounds(Rectangle rect)
    {
        Rectangle rect1 = new Rectangle(0,0,Math.max(0,rect.width-(BROWSE_BUTTON_WIDTH+SPACING)),rect.height);
        getLayoutManager().setConstraint(mTextFigure,rect1);
        Rectangle rect2 = new Rectangle(Math.max(0,rect.width-BROWSE_BUTTON_WIDTH),0,Math.min(rect.width,BROWSE_BUTTON_WIDTH),rect.height);
        getLayoutManager().setConstraint(mButtonFigure,rect2);
        super.setBounds(rect);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IEditableElementFigure#getState()
     */
    public String getState()
    {
        return mTextFigure.getState();
    }
}
