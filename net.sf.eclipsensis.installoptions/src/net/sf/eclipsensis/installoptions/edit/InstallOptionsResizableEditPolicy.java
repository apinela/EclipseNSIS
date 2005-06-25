/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.swt.graphics.Color;

/**
 *  
 */
public class InstallOptionsResizableEditPolicy extends ResizableEditPolicy
{
    private EditPart mEditPart;
    
    
    public Command getCommand(Request request)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            return super.getCommand(request);
        }
    }

    public InstallOptionsResizableEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }

    public void showSourceFeedback(Request request)
    {
        if(!((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            super.showSourceFeedback(request);
        }
    }
    
    protected List createSelectionHandles()
    {
        List list = new ArrayList();

        int directions = getResizeDirections();
        if(directions == 0) {
            InstallOptionsHandleKit.addNonResizableHandles((GraphicalEditPart)getHost(), list);
        }
        else {
            if (directions != -1) {
                InstallOptionsHandleKit.addMoveHandle((GraphicalEditPart)getHost(), list);
                if ((directions & PositionConstants.EAST) != 0) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.EAST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.EAST);
                }
                
                if ((directions & PositionConstants.SOUTH_EAST) == PositionConstants.SOUTH_EAST) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.SOUTH_EAST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                            PositionConstants.SOUTH_EAST);
                }
                
                if ((directions & PositionConstants.SOUTH) != 0) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.SOUTH);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.SOUTH);
                }
                
                if ((directions & PositionConstants.SOUTH_WEST) == PositionConstants.SOUTH_WEST) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.SOUTH_WEST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                                PositionConstants.SOUTH_WEST);
                }
                
                if ((directions & PositionConstants.WEST) != 0) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.WEST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                                PositionConstants.WEST);
                }
                
                if ((directions & PositionConstants.NORTH_WEST) == PositionConstants.NORTH_WEST) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.NORTH_WEST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.NORTH_WEST);
                }
                
                if ((directions & PositionConstants.NORTH) != 0) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.NORTH);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.NORTH);
                }
                
                if ((directions & PositionConstants.NORTH_EAST) == PositionConstants.NORTH_EAST) {
                    InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list, 
                            PositionConstants.NORTH_EAST);
                }
                else {
                    InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list, 
                                PositionConstants.NORTH_EAST);  
                }
            } 
            else {
                InstallOptionsHandleKit.addResizableHandles((GraphicalEditPart)getHost(), list);
            }
        }
        return list;
    }

    /**
     * Creates the figure used for feedback.
     * 
     * @return the new feedback figure
     */
    protected IFigure createDragSourceFeedbackFigure()
    {
        IFigure figure = createFigure((GraphicalEditPart)getHost(), null);

        figure.setBounds(getInitialFeedbackBounds());
        addFeedback(figure);
        return figure;
    }

    protected IFigure createFigure(GraphicalEditPart part, IFigure parent)
    {

        Rectangle childBounds = part.getFigure().getBounds().getCopy();

        IFigure walker = part.getFigure().getParent();

        while (walker != ((GraphicalEditPart)part.getParent()).getFigure()) {
            walker.translateToParent(childBounds);
            walker = walker.getParent();
        }

        IFigure child;
        if (parent != null) {
            child = getCustomFeedbackFigure(part.getModel());
            parent.add(child);
            child.setBounds(childBounds);
        }
        else {
            child = new ResizeFeedbackFigure(childBounds);
        }

        Iterator i = part.getChildren().iterator();

        while (i.hasNext()) {
            createFigure((GraphicalEditPart)i.next(), child);
        }

        return child;
    }

    protected IFigure getCustomFeedbackFigure(Object modelPart)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            IFigure figure = new RectangleFigure();
            ((RectangleFigure)figure).setXOR(true);
            ((RectangleFigure)figure).setFill(true);
            figure.setBackgroundColor(IInstallOptionsConstants.GHOST_FILL_COLOR);
            figure.setForegroundColor(ColorConstants.white);
            return figure;
        }
    }

    /**
     * Returns the layer used for displaying feedback.
     * 
     * @return the feedback layer
     */
    protected IFigure getFeedbackLayer()
    {
        return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
    }

    /**
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#initialFeedbackRectangle()
     */
    protected Rectangle getInitialFeedbackBounds()
    {
        return getHostFigure().getBounds();
    }

    private class ResizeFeedbackFigure extends RectangleFigure
    {
        private boolean mInit = false;
        private String mText = null;
        
        public ResizeFeedbackFigure(Rectangle bounds)
        {
            setXOR(true);
            setFill(true);
            setBackgroundColor(IInstallOptionsConstants.GHOST_FILL_COLOR);
            setForegroundColor(ColorConstants.white);
            setBounds(bounds);
            mInit = true;
        }
        
        
        public void setBounds(Rectangle rect)
        {
            if(mInit) {
                if(rect.width != bounds.width || rect.height != bounds.height) {
                    mText = new StringBuffer().append(rect.width).append("x").append(rect.height).toString(); //$NON-NLS-1$
                }
            }
            super.setBounds(rect);
        }

        public void paintClientArea(Graphics graphics)
        {
            super.paintClientArea(graphics);
            if(mText != null) {
                Dimension dim = FigureUtilities.getTextExtents(mText,getFont());
                int delX = bounds.width-dim.width;
                int delY = bounds.height-dim.height;
                if(delX > 0 && delY > 0) {
                    Color fgColor = getForegroundColor();
                    graphics.setForegroundColor(ColorConstants.listForeground);
                    graphics.drawText(mText,bounds.x+delX/2,bounds.y+delY/2);
                    graphics.setForegroundColor(fgColor);
                }                
            }
        }
    }
}