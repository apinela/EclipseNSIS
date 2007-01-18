/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.*;
import org.eclipse.gef.internal.ui.rulers.RulerEditPart;
import org.eclipse.gef.internal.ui.rulers.RulerFigure;
import org.eclipse.gef.requests.SelectionRequest;

public class InstallOptionsRulerEditPart extends RulerEditPart
{
    /**
     * @param model
     */
    public InstallOptionsRulerEditPart(Object model)
    {
        super(model);
    }

    protected GraphicalViewer getDiagramViewer()
    {
        return super.getDiagramViewer();
    }

    public DragTracker getDragTracker(Request request) {
        if (request.getType().equals(REQ_SELECTION) && ((SelectionRequest)request).getLastButtonPressed() != 1) {
            return null;
        }
        return new InstallOptionsRulerDragTracker(this);
    }

    protected IFigure createFigure()
    {
        RulerFigure ruler =  new InstallOptionsRulerFigure(this, isHorizontal(), getRulerProvider().getUnit());
        ruler.setInterval(100, 10);
        return ruler;
    }
}
