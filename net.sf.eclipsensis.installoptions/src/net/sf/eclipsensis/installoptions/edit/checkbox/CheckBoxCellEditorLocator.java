/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.checkbox;

import net.sf.eclipsensis.installoptions.edit.button.ButtonCellEditorLocator;
import net.sf.eclipsensis.installoptions.figures.CheckBoxFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class CheckBoxCellEditorLocator extends ButtonCellEditorLocator
{
    private static int X_OFFSET = -4;
    private static int W_OFFSET = -10;

    public CheckBoxCellEditorLocator(CheckBoxFigure button) 
    {
        super(button);
    }

    public void relocate(CellEditor celleditor) 
    {
        Text text = (Text)celleditor.getControl();

        Rectangle rect = getButton().getClientArea().getCopy();
        getButton().translateToAbsolute(rect);
        Point p = text.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        
        text.setBounds(rect.x + X_OFFSET + (((CheckBoxFigure)getButton()).isLeftText()?0:15), rect.y+(rect.height-p.y)/2, rect.width + W_OFFSET, p.y);    
    }
}
