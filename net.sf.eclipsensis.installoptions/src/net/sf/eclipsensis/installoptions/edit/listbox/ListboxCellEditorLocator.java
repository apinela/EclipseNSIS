/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listbox;

import net.sf.eclipsensis.installoptions.figures.ListFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.List;

public class ListboxCellEditorLocator implements CellEditorLocator
{
    private ListFigure mListbox;

    public ListboxCellEditorLocator(ListFigure listbox) 
    {
        setListbox(listbox);
    }

    public void relocate(CellEditor celleditor) 
    {
        List list = (List)celleditor.getControl();
        Rectangle rect = mListbox.getClientArea().getCopy();
        mListbox.translateToAbsolute(rect);
        list.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    protected ListFigure getListbox() {
        return mListbox;
    }

    protected void setListbox(ListFigure listbox) 
    {
        mListbox = listbox;
    }
}
