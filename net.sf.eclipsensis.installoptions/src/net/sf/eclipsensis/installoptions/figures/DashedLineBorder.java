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
import org.eclipse.draw2d.geometry.Insets;

public class DashedLineBorder extends LineBorder
{
    private static final int[] DASHES = {16,8};
    
    public DashedLineBorder()
    {
        super(ColorManager.getColor(ColorManager.BLACK),1);
    }

    public void paint(IFigure figure, Graphics graphics, Insets insets) 
    {
        //Hack until setLineDashes is fixed.
        //XXX Remove when it is fixed.
        graphics.pushState();
        tempRect.setBounds(getPaintRectangle(figure, insets));
        graphics.setLineWidth(getWidth());
        if (getColor() != null) {
            graphics.setForegroundColor(getColor());
        }
        
        int[] dashInfo = {0, DASHES[0]};
        drawLine(graphics, tempRect.x, tempRect.y, tempRect.x+tempRect.width-1, tempRect.y, dashInfo);
        drawLine(graphics, tempRect.x+tempRect.width-1, tempRect.y, tempRect.x+tempRect.width-1, tempRect.y+tempRect.height-1, dashInfo);
        drawLine(graphics, tempRect.x+tempRect.width-1, tempRect.y+tempRect.height-1, tempRect.x, tempRect.y+tempRect.height-1, dashInfo);
        drawLine(graphics, tempRect.x, tempRect.y+tempRect.height-1, tempRect.x, tempRect.y, dashInfo);
        
        graphics.popState();
    }
    
    private void drawLine(Graphics graphics, int x1, int y1, int x2, int y2, int[] dashInfo)
    {
        boolean horizontal;
        int length;
        int factor;
        if(x1 == x2 && y1 == y2) {
            return;
        }
        else if(x1 == x2) {
            length = Math.abs(y2-y1)+1;
            horizontal = false;
            factor = (y2-y1)/Math.abs(y2-y1);
        }
        else if(y1 == y2) {
            length = Math.abs(x2-x1)+1;
            horizontal = true;
            factor = (x2-x1)/Math.abs(x2-x1);
        }
        else {
            return;
        }
        int dashIndex = dashInfo[0];
        int dashRemaining = dashInfo[1];
        int x = x1;
        int y = y1;
        while(length > 0) {
            if(dashRemaining == 0) {
                dashIndex++;
                if(dashIndex >= DASHES.length) {
                    dashIndex = 0;
                }
                dashRemaining = DASHES[dashIndex];
            }
            if(length >= dashRemaining) {
                if(dashIndex % 2 == 0) {
                    if(horizontal) {
                        graphics.drawLine(x,y,x+factor*(dashRemaining-1),y);
                    }
                    else {
                        graphics.drawLine(x,y,x,y+factor*(dashRemaining-1));
                    }
                }
                if(horizontal) {
                    x += factor*dashRemaining;
                }
                else {
                    y += factor*dashRemaining;
                }
                length -= dashRemaining;
                dashRemaining = 0;
            }
            else {
                if(dashIndex % 2 == 0) {
                    if(horizontal) {
                        graphics.drawLine(x,y,x+factor*(length-1),y);
                    }
                    else {
                        graphics.drawLine(x,y,x,y+factor*(length-1));
                    }
                    graphics.drawLine(x,tempRect.y,x+length-1,tempRect.y);
                }
                dashRemaining -= length;
                length = 0;
            }
        }
        dashInfo[0] = dashIndex;
        dashInfo[1] = dashRemaining;
    }
}