/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import java.util.Iterator;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.internal.ui.rulers.DragGuidePolicy;

public class InstallOptionsDragGuidePolicy extends DragGuidePolicy
{
    /**
     * 
     */
    public InstallOptionsDragGuidePolicy()
    {
        super();
    }

    protected boolean isMoveValid(int zoomedPosition)
    {
        if(super.isMoveValid(zoomedPosition)) {
            if(zoomedPosition >= 0) {
                Iterator i = getGuideEditPart().getRulerProvider().getAttachedEditParts(getHost().getModel(), 
                        ((InstallOptionsRulerEditPart)getHost().getParent()).getDiagramViewer()).iterator();
                
                int delta = zoomedPosition - getGuideEditPart().getZoomedPosition();
                while (i.hasNext()) {
                    InstallOptionsWidgetEditPart part = (InstallOptionsWidgetEditPart)i.next();
                    IFigure fig = part.getFigure();
                    Rectangle bounds = fig.getBounds();
                    if(getGuideEditPart().isHorizontal()) {
                        if(bounds.y+delta < 0) {
                            return false;
                        }
                    }
                    else {
                        if(bounds.x+delta < 0) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
