/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.droplist;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.combobox.InstallOptionsComboboxEditPart;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditManager;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;

import org.eclipse.gef.tools.CellEditorLocator;

public class InstallOptionsDropListEditPart extends InstallOptionsComboboxEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "droplist.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return "droplist.extended.edit.label"; //$NON-NLS-1$
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("droplist.type.name"); //$NON-NLS-1$
    }

    protected InstallOptionsEditableElementEditManager creatDirectEditManager(InstallOptionsEditableElementEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsDropListEditManager(part,clasz,locator);
    }
}
