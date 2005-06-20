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

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsSelectionEditPolicy extends ComponentEditPolicy
{
    private Label mToolTip;
    private EditPart mEditPart;
    
    public InstallOptionsSelectionEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }
    
    private Label getToolTip()
    {
        if(mToolTip == null) {
            InstallOptionsWidgetEditPart part = (InstallOptionsWidgetEditPart)getHost();
            mToolTip = new Label(part.getTypeName());
            mToolTip.setBorder(new LineBorder());
            mToolTip.setOpaque(true);
            mToolTip.setBackgroundColor(ColorConstants.tooltipBackground);
            mToolTip.setForegroundColor(ColorConstants.tooltipForeground);
            Dimension dim = FigureUtilities.getStringExtents(mToolTip.getText(),Display.getDefault().getSystemFont());
            dim.expand(8,6);
            mToolTip.setSize(dim);
        }
        return mToolTip;
    }
    
    
    public Command getCommand(Request request)
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            return super.getCommand(request);
        }
    }

    private Point computeTipLocation(IFigure tip, Point p) {
        Rectangle clientArea = Display.getDefault().getClientArea();
        Point preferredLocation = new Point(p.x, p.y + 26);
        
        Dimension tipSize = ((GraphicalEditPart)getHost().getRoot()).getFigure().getPreferredSize();
        
        // Adjust location if tip is going to fall outside display
        if (preferredLocation.y + tipSize.height > clientArea.height) {
            preferredLocation.y = p.y - tipSize.height;
        }
        
        if (preferredLocation.x + tipSize.width > clientArea.width) {
            preferredLocation.x -= (preferredLocation.x + tipSize.width) - clientArea.width;
        }
        
        return preferredLocation; 
    }

    public void eraseTargetFeedback(Request request)
    {
        if(request.getType().equals(RequestConstants.REQ_SELECTION_HOVER)) {
            Label toolTip = getToolTip();
            if(toolTip != null) {
                IFigure figure = ((GraphicalEditPart)getHost().getRoot()).getFigure();
                if(figure.getChildren().contains(toolTip)) {
                    figure.remove(toolTip);
                    return;
                }
            }
        }
        super.eraseTargetFeedback(request);
    }
    
    public void showTargetFeedback(Request request)
    {
        if(request.getType().equals(RequestConstants.REQ_SELECTION_HOVER)) {
            Label toolTip = getToolTip();
            if(toolTip != null) {
                LocationRequest req = (LocationRequest)request;
                toolTip.setLocation(computeTipLocation(toolTip,req.getLocation()));
                IFigure figure = ((GraphicalEditPart)getHost().getRoot()).getFigure();
                if(!figure.getChildren().contains(toolTip)) {
                    figure.add(toolTip);
                }
                return;
            }
        }
        super.showTargetFeedback(request);
    }
}