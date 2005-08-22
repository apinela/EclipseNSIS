/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listbox;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.combobox.InstallOptionsComboboxEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.ListFigure;
import net.sf.eclipsensis.installoptions.properties.editors.ListCellEditor;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsListboxEditPart extends InstallOptionsComboboxEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "listbox.direct.edit.label"; //$NON-NLS-1$
    }
    
    protected String getExtendedEditLabelProperty()
    {
        return "listbox.extended.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        return new ListFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }
    
    protected boolean supportsScrolling()
    {
        return true;
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("listbox.type.name"); //$NON-NLS-1$
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsListboxEditManager(part,clasz,locator);
    }

    protected Class getCellEditorClass()
    {
        return ListCellEditor.class;
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new ListboxCellEditorLocator((ListFigure)figure);
    }
}
