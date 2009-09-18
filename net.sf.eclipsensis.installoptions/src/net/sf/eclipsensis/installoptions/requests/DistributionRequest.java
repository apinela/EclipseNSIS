/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.requests;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.requests.ChangeBoundsRequest;

public class DistributionRequest extends ChangeBoundsRequest
{
    private int alignment;
    private Rectangle alignmentRect;

    public DistributionRequest()
    {
        super();
    }

    public DistributionRequest(Object type)
    {
        super(type);
    }

    private void doNormalAlignment(Rectangle result, Rectangle reference) {
        switch (alignment) {
            case PositionConstants.LEFT:
                result.x = reference.x;
                break;
            case PositionConstants.RIGHT:
                result.x = reference.x + reference.width - result.width;
                break;
            case PositionConstants.TOP:
                result.y = reference.y;
                break;
            case PositionConstants.BOTTOM:
                result.y = reference.y + reference.height - result.height;
                break;
            case PositionConstants.CENTER:
                result.x = reference.x + (reference.width / 2) - (result.width / 2);
                break;
            case PositionConstants.MIDDLE:
                result.y = reference.y + (reference.height / 2) - (result.height / 2);
                break;
        }
    }

    private void doPrecisionAlignment(
        PrecisionRectangle result,
        PrecisionRectangle reference) {
        switch (alignment) {
            case PositionConstants.LEFT:
                result.setX(reference.preciseX);
                break;
            case PositionConstants.RIGHT:
                result.setX(
                    reference.preciseX + reference.preciseWidth - result.preciseWidth);
                break;
            case PositionConstants.TOP:
                result.setY(reference.preciseY);
                break;
            case PositionConstants.BOTTOM:
                result.setY(
                    reference.preciseY + reference.preciseHeight - result.preciseHeight);
                break;
            case PositionConstants.CENTER:
                result.setX(
                    reference.preciseX
                        + (reference.preciseWidth / 2)
                        - (result.preciseWidth / 2));
                break;
            case PositionConstants.MIDDLE:
                result.setY(
                    reference.preciseY
                        + (reference.preciseHeight / 2)
                        - (result.preciseHeight / 2));
                break;
        }


    }

    /**
     * Returns the alignment.  Possible values are {@link PositionConstants#LEFT},
     * {@link PositionConstants#RIGHT}, {@link PositionConstants#TOP} and
     * {@link PositionConstants#BOTTOM}.
     * @return the alignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Returns the rectangle used to align the edit part with.
     * @return the alignment rectangle
     */
    public Rectangle getAlignmentRectangle() {
        return alignmentRect;
    }

    /**
     * @see ChangeBoundsRequest#getTransformedRectangle(Rectangle)
     */
    @Override
    public Rectangle getTransformedRectangle(Rectangle rect) {
        Rectangle result = rect.getCopy();
        Rectangle reference = getAlignmentRectangle();

        if (result instanceof PrecisionRectangle) {
            if (reference instanceof PrecisionRectangle) {
                doPrecisionAlignment(
                    (PrecisionRectangle)result,
                    (PrecisionRectangle)reference);
            }
            else {
                doPrecisionAlignment(
                    (PrecisionRectangle)result,
                    new PrecisionRectangle(reference));
            }
        }
        else {
            doNormalAlignment(result, reference);
        }
        return result;
    }

    /**
     * Sets the alignment.
     * @param align the alignment
     * @see #getAlignment()
     */
    public void setAlignment(int align) {
        alignment = align;
    }

    /**
     * Sets the alignment rectangle.
     * @param rect the alignment rectangle
     * @see #getAlignmentRectangle()
     */
    public void setAlignmentRectangle(Rectangle rect) {
        alignmentRect = rect;
    }

}
