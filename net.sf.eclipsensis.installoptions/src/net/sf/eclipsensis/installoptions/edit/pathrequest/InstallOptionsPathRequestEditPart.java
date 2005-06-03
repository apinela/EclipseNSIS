/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.pathrequest;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.PathRequestFigure;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;

public abstract class InstallOptionsPathRequestEditPart extends InstallOptionsEditableElementEditPart
{
    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        return new PathRequestFigure(this);
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsPathRequestEditManager(part, clasz, locator);
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new PathRequestCellEditorLocator((PathRequestFigure)getFigure());
    }
}
