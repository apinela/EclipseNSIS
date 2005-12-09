/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.editors.CustomComboBoxCellEditor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsCombobox extends InstallOptionsListItems
{
    protected InstallOptionsCombobox(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_COMBOBOX;
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            ComboStatePropertyDescriptor descriptor = new ComboStatePropertyDescriptor();
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }

    protected class ComboStatePropertyDescriptor extends PropertyDescriptor implements PropertyChangeListener
    {
        private CustomComboBoxCellEditor mEditor;
        private DisposeListener mListener = new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mEditor = null;
            }
         };
        private int mStyle = SWT.NONE;

        public ComboStatePropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$
            setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
        }

        public int getStyle()
        {
            return mStyle;
        }

        public void setStyle(int style)
        {
            mStyle = style;
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                setListItems((List)evt.getNewValue());
            }

        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mEditor == null) {
                mEditor = new CustomComboBoxCellEditor(parent, getListItems(), mStyle);
                mEditor.getControl().addDisposeListener(mListener);
            }
            return mEditor;
        }

        public void setListItems(List listItems)
        {
            if(mEditor != null) {
                mEditor.setItems(listItems);
            }
        }
    }
}

