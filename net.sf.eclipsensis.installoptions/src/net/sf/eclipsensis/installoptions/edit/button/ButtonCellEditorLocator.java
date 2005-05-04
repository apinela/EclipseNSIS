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

import net.sf.eclipsensis.installoptions.figures.ButtonFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class ButtonCellEditorLocator implements CellEditorLocator
{
    private static int X_OFFSET = -4;
    private static int W_OFFSET = 5;

    private ButtonFigure mButton;

    public ButtonCellEditorLocator(ButtonFigure button) 
    {
        setButton(button);
    }

    public void relocate(CellEditor celleditor) 
    {
        Text text = (Text)celleditor.getControl();

        Rectangle rect = mButton.getClientArea().getCopy();
        mButton.translateToAbsolute(rect);
        Point p = text.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        
        text.setBounds(rect.x + X_OFFSET, rect.y+(rect.height-p.y)/2, rect.width + W_OFFSET, p.y);    
    }

    protected ButtonFigure getButton() {
        return mButton;
    }

    protected void setButton(ButtonFigure button) 
    {
        mButton = button;
    }
}
