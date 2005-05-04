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

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.Position;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;

public abstract class InstallOptionsWidgetEditPart extends InstallOptionsEditPart
{

    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsWidget.PROPERTY_POSITION.equals(prop)) {
            refreshVisuals();
        }
    }

    /**
     * Updates the visual aspect of this.
     */
    protected void refreshVisuals()
    {
        InstallOptionsWidget widget = (InstallOptionsWidget)getInstallOptionsElement();
        Position pos = widget.getPosition();
        pos = widget.toGraphical(pos); 
        Rectangle r = new Rectangle(pos.left,pos.top,(pos.right-pos.left)+1,(pos.bottom-pos.top)+1);

        ((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), r);
    }
}
