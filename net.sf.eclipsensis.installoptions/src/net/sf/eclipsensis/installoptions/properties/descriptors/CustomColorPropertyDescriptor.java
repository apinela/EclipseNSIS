/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.descriptors;

import net.sf.eclipsensis.installoptions.properties.editors.CustomColorCellEditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CustomColorPropertyDescriptor extends PropertyDescriptor
{
    private RGB mDefaultColor = null;

    /**
     * @param id
     * @param displayName
     */
    public CustomColorPropertyDescriptor(Object id, String displayName)
    {
        super(id, displayName);
    }

    public RGB getDefaultColor()
    {
        return mDefaultColor;
    }

    public void setDefaultColor(RGB defaultColor)
    {
        mDefaultColor = defaultColor;
    }

    public CellEditor createPropertyEditor(Composite parent)
    {
        CustomColorCellEditor editor = new CustomColorCellEditor(parent);
        if(mDefaultColor != null) {
            editor.setDefaultColor(mDefaultColor);
        }
        if(getLabelProvider() != null) {
            editor.setLabelProvider(getLabelProvider());
        }
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
