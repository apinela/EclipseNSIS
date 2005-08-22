/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.label;

import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart.ILabelFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

public class LabelCellEditorLocator implements CellEditorLocator
{
    private static int X_OFFSET = -4;
    private static int W_OFFSET = 5;

    private ILabelFigure mLabel;

    public LabelCellEditorLocator(ILabelFigure label) 
    {
        setLabel(label);
    }

    public void relocate(CellEditor celleditor) 
    {
        Text text = (Text)celleditor.getControl();

        Rectangle rect = mLabel.getClientArea().getCopy();
        mLabel.translateToAbsolute(rect);
        
        text.setBounds(rect.x + X_OFFSET, rect.y, rect.width + W_OFFSET, rect.height);    
    }

    protected ILabelFigure getLabel() {
        return mLabel;
    }

    protected void setLabel(ILabelFigure label) 
    {
        mLabel = label;
    }
}
