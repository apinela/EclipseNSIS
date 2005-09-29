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

import net.sf.eclipsensis.installoptions.figures.FigureUtility;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsSnapToGrid extends SnapToGrid
{
    private double mDpuX;
    private double mDpuY;

    /**
     * @param container
     */
    public InstallOptionsSnapToGrid(GraphicalEditPart container)
    {
        super(container);
        Font f = Display.getDefault().getSystemFont();
        mDpuX = ((double)FigureUtility.dialogUnitsToPixelsX(1000,f))/1000;
        mDpuY = ((double)FigureUtility.dialogUnitsToPixelsY(1000,f))/1000;
    }

    public int snapRectangle(Request request, int snapLocations, PrecisionRectangle rect, PrecisionRectangle result)
    {
        if(!((InstallOptionsEditDomain)container.getViewer().getEditDomain()).isReadOnly()) {
            rect = rect.getPreciseCopy();
            
            makeRelative(container.getContentPane(), rect);
            PrecisionRectangle correction = new PrecisionRectangle();
            makeRelative(container.getContentPane(), correction);
            
            if (gridX > 0 && (snapLocations & EAST) != 0) {
                correction.preciseWidth -= Math.IEEEremainder(rect.preciseRight()
                        - origin.x*mDpuX - 1, gridX*mDpuX);
                snapLocations &= ~EAST;
            }
            
            if ((snapLocations & (WEST | HORIZONTAL)) != 0 && gridX > 0) {
                double leftCorrection = Math.IEEEremainder(rect.preciseX - origin.x*mDpuX,
                        gridX*mDpuX);
                correction.preciseX -= leftCorrection;
                if ((snapLocations & HORIZONTAL) == 0) {
                    correction.preciseWidth += leftCorrection;
                }
                snapLocations &= ~(WEST | HORIZONTAL);
            }
            
            if ((snapLocations & SOUTH) != 0 && gridY > 0) {
                correction.preciseHeight -= Math.IEEEremainder(rect.preciseBottom()
                        - origin.y*mDpuY - 1, gridY*mDpuY);
                snapLocations &= ~SOUTH;
            }
            
            if ((snapLocations & (NORTH | VERTICAL)) != 0 && gridY > 0) {
                double topCorrection = Math.IEEEremainder(
                        rect.preciseY - origin.y*mDpuY, gridY*mDpuY);
                correction.preciseY -= topCorrection;
                if ((snapLocations & VERTICAL) == 0) {
                    correction.preciseHeight += topCorrection;
                }
                snapLocations &= ~(NORTH | VERTICAL);
            }
    
            correction.updateInts();
            makeAbsolute(container.getContentPane(), correction);
            result.preciseX += correction.preciseX;
            result.preciseY += correction.preciseY;
            result.preciseWidth += correction.preciseWidth;
            result.preciseHeight += correction.preciseHeight;
            result.updateInts();
        }
        return snapLocations;
    }
}
