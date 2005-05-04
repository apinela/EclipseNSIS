/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.button;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.ButtonFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.*;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;

public class InstallOptionsButtonEditPart extends InstallOptionsWidgetEditPart
{
    private DirectEditManager mManager;

    protected AccessibleEditPart createAccessible() {
        return new AccessibleGraphicalEditPart(){
            public void getValue(AccessibleControlEvent e) {
                e.result = getInstallOptionsButton().getText();
            }

            public void getName(AccessibleEvent e) {
                e.result = InstallOptionsPlugin.getResourceString("button.template.name"); //$NON-NLS-1$
            }
        };
    }

    protected void createEditPolicies(){
        super.createEditPolicies();
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ButtonDirectEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE,new InstallOptionsButtonEditPolicy()); 
    }

    protected IFigure createFigure() 
    {
        ButtonFigure buttonFigure = new ButtonFigure(this.getRoot());
        buttonFigure.setText(getInstallOptionsButton().getText());
        return buttonFigure;
    }

    private InstallOptionsButton getInstallOptionsButton()
    {
        return (InstallOptionsButton)getModel();
    }

    private void performDirectEdit()
    {
        if(mManager == null) {
            mManager = new InstallOptionsButtonEditManager(this, TextCellEditor.class, 
                    new ButtonCellEditorLocator((ButtonFigure)getFigure())  );
        }
        mManager.show();
    }

    public void performRequest(Request request)
    {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            performDirectEdit();
        }
        super.performRequest(request);
    }

    public void propertyChange(PropertyChangeEvent evt){
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsButton.PROPERTY_TEXT)) {//$NON-NLS-1$
            ((ButtonFigure)getFigure()).setText(getInstallOptionsButton().getText());
            refreshVisuals();
        }
        else {
            super.propertyChange(evt);
        }
    }
}
