/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.picture;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.PictureFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsPicture;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;

public abstract class InstallOptionsPictureEditPart extends InstallOptionsWidgetEditPart
{
    protected String getAccessibleControlEventResult()
    {
        return ((InstallOptionsPicture)getModel()).getText();
    }

    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        return new PictureFigure();
    }

    public void initFigure(IInstallOptionsFigure figure2)
    {
        super.initFigure(figure2);
        PictureFigure figure3 = (PictureFigure)figure2;
        InstallOptionsPicture picture = (InstallOptionsPicture)getModel();
        figure3.setImage(picture.getImage());
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return null;
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return null;
    }
}
