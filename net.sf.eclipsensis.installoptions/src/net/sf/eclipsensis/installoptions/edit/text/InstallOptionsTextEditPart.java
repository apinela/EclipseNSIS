/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.text;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.TextFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsTextEditPart extends InstallOptionsEditableElementEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "text.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new TextFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    protected void handleFlagAdded(String flag)
    {
        TextFigure figure = (TextFigure)getFigure();
        if(flag.equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            figure.setOnlyNumbers(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_MULTILINE)) {
            figure.setMultiLine(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_NOWORDWRAP)) {
            figure.setNoWordWrap(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_READONLY)) {
            figure.setReadOnly(true);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagAdded(flag);
        }
    }
    protected void handleFlagRemoved(String flag)
    {
        TextFigure figure = (TextFigure)getFigure();
        if(flag.equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            figure.setOnlyNumbers(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_MULTILINE)) {
            figure.setMultiLine(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_NOWORDWRAP)) {
            figure.setNoWordWrap(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_READONLY)) {
            figure.setReadOnly(false);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagRemoved(flag);
        }
    }
    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("text.type.name"); //$NON-NLS-1$
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsTextEditManager(part, clasz, locator);
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new TextCellEditorLocator((TextFigure)getFigure());
    }
}
