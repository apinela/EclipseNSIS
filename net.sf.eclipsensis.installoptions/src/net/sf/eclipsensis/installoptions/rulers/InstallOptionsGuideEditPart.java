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

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.internal.ui.rulers.GuideEditPart;

public class InstallOptionsGuideEditPart extends GuideEditPart
{
    /**
     * @param model
     */
    public InstallOptionsGuideEditPart(Object model)
    {
        super(model);
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new InstallOptionsDragGuidePolicy());
    }
}
