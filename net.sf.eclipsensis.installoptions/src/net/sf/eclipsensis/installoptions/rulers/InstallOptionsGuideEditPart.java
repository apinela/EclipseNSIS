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

import net.sf.eclipsensis.installoptions.figures.FigureUtility;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.internal.ui.rulers.GuideEditPart;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

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
    
    public void updateLocationOfFigures(int position) 
    {
        Font f = Display.getDefault().getSystemFont();
        position = (isHorizontal()?FigureUtility.pixelsToDialogUnitsY(position,f):FigureUtility.pixelsToDialogUnitsX(position,f));
        position = (isHorizontal()?FigureUtility.dialogUnitsToPixelsY(position,f):FigureUtility.dialogUnitsToPixelsX(position,f));
        super.updateLocationOfFigures(position);
    }
}
