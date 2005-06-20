/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.util.List;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsTreeContainerEditPolicy;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsTreeEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

public class InstallOptionsDialogTreeEditPart extends InstallOptionsTreeEditPart
{
    /**
     * @param model
     */
    public InstallOptionsDialogTreeEditPart(Object model)
    {
        super(model);
    }

    /**
     * Creates and installs pertinent EditPolicies.
     */
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE, new InstallOptionsTreeContainerEditPolicy(this));
        //If this editpart is the contents of the viewer, then it is not deletable!
        if (getParent() instanceof RootEditPart) {
            installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        }
    }

    /**
     * Returns the model of this as a InstallOptionsDialog.
     *
     * @return  Model of this.
     */
    protected InstallOptionsDialog getInstallOptionsDialog() {
        return (InstallOptionsDialog)getModel();
    }

    /**
     * Returns the children of this from the model,
     * as this is capable enough of holding EditParts.
     *
     * @return  List of children.
     */
    protected List getModelChildren() {
        return getInstallOptionsDialog().getChildren();
    }

}
