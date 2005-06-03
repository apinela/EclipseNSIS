/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.text;

import net.sf.eclipsensis.installoptions.figures.TextFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

public class TextCellEditorLocator implements CellEditorLocator
{
    private static int X_OFFSET = -4;
    private static int W_OFFSET = 5;

    private TextFigure mText;

    public TextCellEditorLocator(TextFigure text) 
    {
        setText(text);
    }

    public void relocate(CellEditor celleditor) 
    {
        Text text = (Text)celleditor.getControl();

        Rectangle rect = mText.getClientArea().getCopy();
        mText.translateToAbsolute(rect);
        
        text.setBounds(rect.x + X_OFFSET, rect.y, rect.width + W_OFFSET, rect.height);    
    }

    protected TextFigure getText() {
        return mText;
    }

    protected void setText(TextFigure text) 
    {
        mText = text;
    }
}
