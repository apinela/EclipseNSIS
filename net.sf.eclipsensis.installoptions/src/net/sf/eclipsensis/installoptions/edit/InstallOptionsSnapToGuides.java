/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.*;

public class InstallOptionsSnapToGuides extends SnapToGuides
{
    /**
     * @param container
     */
    public InstallOptionsSnapToGuides(GraphicalEditPart container)
    {
        super(container);
    }

    public int snapRectangle(Request request, int snapLocations, PrecisionRectangle rect, PrecisionRectangle result)
    {
        if(!((InstallOptionsEditDomain)container.getViewer().getEditDomain()).isReadOnly()) {
            return super.snapRectangle(request, snapLocations, rect, result);
        }
        else {
            return snapLocations;
        }
    }
}
