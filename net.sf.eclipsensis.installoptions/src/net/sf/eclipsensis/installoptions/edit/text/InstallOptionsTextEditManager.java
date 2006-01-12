/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.text;

import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditManager;
import net.sf.eclipsensis.installoptions.figures.TextFigure;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class InstallOptionsTextEditManager extends InstallOptionsEditableElementEditManager
{
    public InstallOptionsTextEditManager(GraphicalEditPart source, Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected CellEditor createCellEditor(Composite composite)
    {
        TextCellEditor cellEditor = (TextCellEditor)super.createCellEditor(composite);
        TextFigure figure = (TextFigure)getEditPart().getFigure();
        if(figure.isOnlyNumbers()) {
            ((Text)cellEditor.getControl()).addVerifyListener(new VerifyListener(){
                public void verifyText(VerifyEvent e)
                {
                    char[] chars = e.text.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if(!Character.isDigit(chars[i])) {
                            e.doit = false;
                            return;
                        }
                    }
                }
            });
        }
        return cellEditor;
    }

    protected int getCellEditorStyle()
    {
        TextFigure figure = (TextFigure)getEditPart().getFigure();
        int style = SWT.LEFT;
        if(figure.isMultiLine()) {
            style |= SWT.MULTI;
            if(figure.isNoWordWrap() && !figure.isHScroll()) {
                style |= SWT.WRAP;
            }
        }
        if(figure.isHScroll()) {
            style |= SWT.H_SCROLL;
        }
        if(figure.isVScroll()) {
            style |= SWT.V_SCROLL;
        }
        return style;
    }
}
