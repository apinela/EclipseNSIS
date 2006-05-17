/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listbox;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.listitems.InstallOptionsListItemsEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;
import net.sf.eclipsensis.installoptions.properties.editors.ListCellEditor;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsListboxEditPart extends InstallOptionsListItemsEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "listbox.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return "listbox.extended.edit.label"; //$NON-NLS-1$
    }

    protected IListItemsFigure createListItemsFigure()
    {
        return new ListFigure((Composite)getViewer().getControl(), new PropertySourceWrapper(getInstallOptionsWidget()) {
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
                    List list = (List)getDelegate().getPropertyValue(id);
                    if(!list.contains(InstallOptionsModel.FLAGS_VSCROLL)) {
                        list = new ArrayList(list);
                        list.add(InstallOptionsModel.FLAGS_VSCROLL);
                    }
                    return list;
                }
                return super.getPropertyValue(id);
            }
        });
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
