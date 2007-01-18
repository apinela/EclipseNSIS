/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.unknown;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsCellEditorLocator;
import net.sf.eclipsensis.installoptions.edit.unknown.InstallOptionsUnknownEditPart.IUnknownFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

public class UnknownCellEditorLocator  extends InstallOptionsCellEditorLocator
{
    private static final int X_OFFSET = -4;
    private static final int W_OFFSET = 5;

    public UnknownCellEditorLocator(IUnknownFigure label)
    {
        super(label);
    }

    protected Rectangle transformLocation(Rectangle editArea, Point preferredSize)
    {
        return new Rectangle(editArea.x + X_OFFSET, editArea.y+(editArea.height-preferredSize.y)/2,
                             editArea.width + W_OFFSET, preferredSize.y);
    }
}
