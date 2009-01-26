/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.editable;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.IEditableElementFigure;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.EditPolicy;

public abstract class InstallOptionsEditableElementEditPart extends InstallOptionsWidgetEditPart
{
    protected String getAccessibleControlEventResult()
    {
        return getInstallOptionsEditableElement().getState();
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, createDirectEditPolicy());
    }

    /**
     * @return
     */
    protected EditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new EditableElementDirectEditPolicy();
    }

    protected InstallOptionsEditableElement getInstallOptionsEditableElement()
    {
        return (InstallOptionsEditableElement)getModel();
    }

    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            IEditableElementFigure figure2 = (IEditableElementFigure)getFigure();
            figure2.setState((String)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
}
