/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.combobox;

import java.util.List;

import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditManager;
import net.sf.eclipsensis.installoptions.model.InstallOptionsCombobox;
import net.sf.eclipsensis.installoptions.properties.editors.EditableComboBoxCellEditor;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsComboboxEditManager extends InstallOptionsEditableElementEditManager
{
    public InstallOptionsComboboxEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected CellEditor createCellEditor(Composite composite)
    {
        InstallOptionsCombobox combobox = (InstallOptionsCombobox)getEditPart().getModel();
        List items = combobox.getListItems();
        EditableComboBoxCellEditor cellEditor = new EditableComboBoxCellEditor(composite,items,getCellEditorStyle());
        cellEditor.setAutoApplyEditorValue(true);
        cellEditor.setAutoDropDown(true);
        return cellEditor;
    }
    
    protected void selectCellEditorText()
    {
        Combo combo = (Combo)getCellEditor().getControl();
        String text = combo.getText();
        if(!Common.isEmpty(text)) {
            combo.setSelection(new Point(0,text.length()));
        }
    }
    
    protected int getCellEditorStyle() 
    {
        return SWT.DROP_DOWN;
    }
}
