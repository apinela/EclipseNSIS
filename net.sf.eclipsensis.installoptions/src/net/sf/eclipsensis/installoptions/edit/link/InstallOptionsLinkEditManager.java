/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.link;

import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditManager;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.swt.SWT;

public class InstallOptionsLinkEditManager extends InstallOptionsUneditableElementEditManager
{
    public InstallOptionsLinkEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected int getCellEditorStyle() 
    {
        return SWT.LEFT;
    }
}