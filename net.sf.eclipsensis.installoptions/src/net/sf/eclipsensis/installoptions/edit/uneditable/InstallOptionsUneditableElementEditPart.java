/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.IUneditableElementFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsUneditableElement;

import org.eclipse.gef.EditPolicy;

public abstract class InstallOptionsUneditableElementEditPart extends InstallOptionsWidgetEditPart
{
    protected String getAccessibleControlEventResult()
    {
        return getInstallOptionsUneditableElement().getText();
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new UneditableElementDirectEditPolicy());
    }

    protected InstallOptionsUneditableElement getInstallOptionsUneditableElement()
    {
        return (InstallOptionsUneditableElement)getModel();
    }

    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            IUneditableElementFigure figure2 = (IUneditableElementFigure)getFigure();
            figure2.setText((String)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
}
