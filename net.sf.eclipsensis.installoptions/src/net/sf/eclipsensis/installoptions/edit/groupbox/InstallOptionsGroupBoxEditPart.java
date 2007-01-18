/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.groupbox;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.GroupBoxFigure;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsGroupBoxEditPart extends InstallOptionsUneditableElementEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "groupbox.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new GroupBoxFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("groupbox.type.name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#creatDirectEditManager(net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart, java.lang.Class, org.eclipse.gef.tools.CellEditorLocator)
     */
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsGroupBoxEditManager(part, clasz, locator);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#createCellEditorLocator(net.sf.eclipsensis.installoptions.figures.UneditableElementFigure)
     */
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new GroupBoxCellEditorLocator((GroupBoxFigure)getFigure());
    }
}
