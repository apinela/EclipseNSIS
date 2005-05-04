/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.button;

import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class InstallOptionsButtonEditManager extends DirectEditManager
{
    public InstallOptionsButtonEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected void initCellEditor() 
    {
        InstallOptionsButton button = (InstallOptionsButton)getEditPart().getModel();
        String initialLabelText = button.getText();
        getCellEditor().setValue(initialLabelText);
        Text text = (Text)getCellEditor().getControl();
        text.selectAll();
    }

    protected CellEditor createCellEditorOn(Composite composite) 
    {
        return new TextCellEditor(composite, SWT.SINGLE|SWT.CENTER);
    }
}