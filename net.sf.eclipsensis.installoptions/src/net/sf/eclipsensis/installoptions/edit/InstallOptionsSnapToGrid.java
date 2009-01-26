/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.*;
import org.eclipse.swt.graphics.Font;

public class InstallOptionsSnapToGrid extends SnapToGrid
{
    /**
     * @param container
     */
    public InstallOptionsSnapToGrid(GraphicalEditPart container)
    {
        super(container);
    }

    public int snapRectangle(Request request, int snapLocations, PrecisionRectangle rect, PrecisionRectangle result)
    {
        Font f = FontUtility.getInstallOptionsFont();
        double dpuX = ((double)FigureUtility.dialogUnitsToPixelsX(1000,f))/1000;
        double dpuY = ((double)FigureUtility.dialogUnitsToPixelsY(1000,f))/1000;
        rect = rect.getPreciseCopy();

        makeRelative(container.getContentPane(), rect);
        PrecisionRectangle correction = new PrecisionRectangle();
        makeRelative(container.getContentPane(), correction);

        if (gridX > 0 && (snapLocations & EAST) != 0) {
            correction.preciseWidth -= Math.IEEEremainder(rect.preciseRight()
                    - origin.x*dpuX - 1, gridX*dpuX);
            snapLocations &= ~EAST;
        }

        if ((snapLocations & (WEST | HORIZONTAL)) != 0 && gridX > 0) {
            double leftCorrection = Math.IEEEremainder(rect.preciseX - origin.x*dpuX,
                    gridX*dpuX);
            correction.preciseX -= leftCorrection;
            if ((snapLocations & HORIZONTAL) == 0) {
                correction.preciseWidth += leftCorrection;
            }
            snapLocations &= ~(WEST | HORIZONTAL);
        }

        if ((snapLocations & SOUTH) != 0 && gridY > 0) {
            correction.preciseHeight -= Math.IEEEremainder(rect.preciseBottom()
                    - origin.y*dpuY - 1, gridY*dpuY);
            snapLocations &= ~SOUTH;
        }

        if ((snapLocations & (NORTH | VERTICAL)) != 0 && gridY > 0) {
            double topCorrection = Math.IEEEremainder(
                    rect.preciseY - origin.y*dpuY, gridY*dpuY);
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
        return snapLocations;
    }
}
