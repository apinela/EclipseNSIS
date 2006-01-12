/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsDirectEditManager;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsUneditableElement;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public abstract class InstallOptionsUneditableElementEditManager extends InstallOptionsDirectEditManager
{
    public InstallOptionsUneditableElementEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected void initCellEditor()
    {
        InstallOptionsUneditableElement control = (InstallOptionsUneditableElement)getEditPart().getModel();
        String initialText = control.getText();
        getCellEditor().setValue(initialText);
        Text text = (Text)getCellEditor().getControl();
        text.selectAll();
    }

    protected CellEditor createCellEditor(Composite composite)
    {
        return new TextCellEditor(composite, getCellEditorStyle());
    }

    protected String getDirectEditProperty()
    {
        return InstallOptionsModel.PROPERTY_TEXT;
    }

    protected abstract int getCellEditorStyle();
}
