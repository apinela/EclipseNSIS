/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public abstract class InstallOptionsDirectEditManager extends DirectEditManager
{
    /**
     * @param source
     * @param editorType
     * @param locator
     */
    public InstallOptionsDirectEditManager(GraphicalEditPart source,
            Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected final CellEditor createCellEditorOn(Composite composite)
    {
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(((InstallOptionsWidget)getEditPart().getModel()).getType());
        if(typeDef == null || !typeDef.getSettings().contains(getDirectEditProperty()) ||
           ((InstallOptionsEditDomain)getEditPart().getViewer().getEditDomain()).isReadOnly()) {
            return null;
        }
        else {
            return createCellEditor(composite);
        }
    }

    protected CellEditor createCellEditor(Composite composite)
    {
        return super.createCellEditorOn(composite);
    }

    protected abstract String getDirectEditProperty();
}
