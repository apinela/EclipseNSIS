/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.unknown;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsDirectEditManager;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsUnknown;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class InstallOptionsUnknownEditManager extends InstallOptionsDirectEditManager
{
    public InstallOptionsUnknownEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected void initCellEditor()
    {
        InstallOptionsUnknown control = (InstallOptionsUnknown)getEditPart().getModel();
        String initialText = control.getType();
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
        return InstallOptionsModel.PROPERTY_TYPE;
    }

    protected int getCellEditorStyle()
    {
        return SWT.SINGLE|SWT.CENTER;
    }
}
