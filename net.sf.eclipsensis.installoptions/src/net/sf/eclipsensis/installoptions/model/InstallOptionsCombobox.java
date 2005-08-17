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
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.dialogs.ListItemsDialog;
import net.sf.eclipsensis.installoptions.properties.editors.CustomComboBoxCellEditor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.ListLabelProvider;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsCombobox extends InstallOptionsEditableElement
{
    protected static LabelProvider cListItemsLabelProvider = new ListLabelProvider();
    private List mListItems;
    
    protected InstallOptionsCombobox(INISection section)
    {
        super(section);
    }
    
    public String getType()
    {
        return InstallOptionsModel.TYPE_COMBOBOX;
    }
    
    protected void init()
    {
        super.init();
        mListItems = new ArrayList();
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,99,99);
    }
    
    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            list.add(InstallOptionsModel.PROPERTY_LISTITEMS);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected TypeConverter getTypeConverter(String property)
    {
        if(InstallOptionsModel.PROPERTY_LISTITEMS.equals(property)) {
            return TypeConverter.STRING_LIST_CONVERTER;
        }
        else {
            return super.getTypeConverter(property);
        }
    }
    
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            return new ListItemsPropertyDescriptor();
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            ComboStatePropertyDescriptor descriptor = new ComboStatePropertyDescriptor();
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_LISTITEMS.equals(propName)) {
            return getListItems();
        }
        return super.getPropertyValue(propName);
    }
    
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            setListItems((List)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public List getListItems()
    {
        return (mListItems == null?Collections.EMPTY_LIST:mListItems);
    }

    public void setListItems(List listItems)
    {
        if(!mListItems.equals(listItems)) {
            List oldListItems = mListItems;
            mListItems = listItems;
            firePropertyChange(InstallOptionsModel.PROPERTY_LISTITEMS, oldListItems, mListItems);
            setDirty(true);
        }
    }
    
    protected boolean isStateEditable()
    {
        return true;
    }
    
    public Object clone()
    {
        InstallOptionsCombobox clone = (InstallOptionsCombobox)super.clone();
        clone.setListItems(new ArrayList(mListItems));
        return clone;
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

    protected class ListItemsPropertyDescriptor extends PropertyDescriptor
    {
        public ListItemsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_LISTITEMS, InstallOptionsPlugin.getResourceString("listitems.property.name")); //$NON-NLS-1$
            setLabelProvider(cListItemsLabelProvider);
            setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_LISTITEMS));
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            final ListItemsCellEditor cellEditor = new ListItemsCellEditor(parent);
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                cellEditor.setValidator(validator);
            }
            return cellEditor;
        }
    }
        
    protected class ListItemsCellEditor extends DialogCellEditor
    {
        private ListItemsCellEditor(Composite parent)
        {
            super(parent);
        }

        protected void updateContents(Object value) 
        {
            Label label = getDefaultLabel();
            if (label != null) {
                label.setText(cListItemsLabelProvider.getText(value));
            }
        }

        protected Object openDialogBox(Control cellEditorWindow)
        {
            Object oldValue = getValue();
            ListItemsDialog dialog = new ListItemsDialog(cellEditorWindow.getShell(), (List)oldValue, getType());
            dialog.setValidator(getValidator());
            int result = dialog.open();
            return (result == Window.OK?dialog.getValues():oldValue);
        }
    }
}

