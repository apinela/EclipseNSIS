/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.combobox;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.listitems.InstallOptionsListItemsEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.properties.editors.EditableComboBoxCellEditor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.eclipse.gef.tools.*;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsComboboxEditPart extends InstallOptionsListItemsEditPart
{
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy("ShowDropdown", new SelectionEditPolicy() { //$NON-NLS-1$
            protected void hideSelection()
            {
                setShowDropdown(false);
            }

            protected void showSelection()
            {
                setShowDropdown(false);
            }

            protected void showPrimarySelection()
            {
                setShowDropdown(true);
            }

            private void setShowDropdown(boolean flag)
            {
                IFigure figure = getFigure();
                if(figure instanceof ComboboxFigure) {
                    ((ComboboxFigure)figure).setShowDropdown(flag);
                }
            }
        });
    }

    protected String getDirectEditLabelProperty()
    {
        return "combobox.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return "combobox.extended.edit.label"; //$NON-NLS-1$
    }

    protected IListItemsFigure createListItemsFigure()
    {
        ComboboxFigure comboboxFigure = new ComboboxFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        comboboxFigure.setShowDropdown(getSelected()==SELECTED_PRIMARY);
        return comboboxFigure;
    }

    protected boolean supportsScrolling()
    {
        return false;
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("combobox.type.name"); //$NON-NLS-1$
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsComboboxEditManager(part,clasz,locator);
    }

    protected Class getCellEditorClass()
    {
        return EditableComboBoxCellEditor.class;
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new ComboboxCellEditorLocator((ComboboxFigure)figure);
    }
}
