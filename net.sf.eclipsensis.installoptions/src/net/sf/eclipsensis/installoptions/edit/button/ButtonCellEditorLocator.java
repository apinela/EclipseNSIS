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

import net.sf.eclipsensis.installoptions.edit.InstallOptionsCellEditorLocator;
import net.sf.eclipsensis.installoptions.figures.ButtonFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

public class ButtonCellEditorLocator extends InstallOptionsCellEditorLocator
{
    private static int X_OFFSET = -4;
    private static int W_OFFSET = 5;

    public ButtonCellEditorLocator(ButtonFigure button) 
    {
        super(button);
    }

    protected Rectangle transformLocation(Rectangle editArea, Point preferredSize)
    {
        return new Rectangle(editArea.x + X_OFFSET, editArea.y+(editArea.height-preferredSize.y)/2, 
                             editArea.width + W_OFFSET, preferredSize.y);
    }
}
