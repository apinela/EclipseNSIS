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

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.editors.FileFilterCellEditor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsFileRequest extends InstallOptionsPathRequest
{
    public static Image FILEREQUEST_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("filerequest.type.small.icon")); //$NON-NLS-1$
    public  static final char FILTER_SEPARATOR = ';';
    
    private static final TypeConverter FILTER_LIST_CONVERTER = new TypeConverter(){
        public String asString(Object o)
        {
            return Common.flatten(((List)o).toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object asType(String s)
        {
            List list = new ArrayList();
            String[] tokens = Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR);
            for (int i = 0; i < (tokens.length-1); i+= 2) {
                String description = tokens[i];
                String[] temp = Common.tokenize(tokens[i+1],FILTER_SEPARATOR);
                FilePattern[] patterns = new FilePattern[temp.length];
                for (int j = 0; j < patterns.length; j++) {
                    patterns[j] = new FilePattern(temp[j]);
                }
                list.add(new FileFilter(description, patterns));
            }
            return list;
        }

        public Object makeCopy(Object o)
        {
            List list = new ArrayList();
            for(Iterator iter=((List)o).iterator(); iter.hasNext(); ) {
                list.add(((FileFilter)iter.next()).clone());
            }
            return null;
        }
    };

    public static final LabelProvider FILTER_LABEL_PROVIDER = new LabelProvider(){
        public String getText(Object element) 
        {
            if(element instanceof List) {
                return FILTER_LIST_CONVERTER.asString(element);
            }
            else {
                return super.getText(element);
            }
        }
    };

    private List mFilter = new ArrayList();

    public InstallOptionsFileRequest()
    {
        super(InstallOptionsModel.TYPE_FILEREQUEST);
    }

    public Image getIconImage()
    {
        return FILEREQUEST_ICON;
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsFileRequest clone = (InstallOptionsFileRequest)super.clone();
        List list = new ArrayList();
        for (Iterator iter = mFilter.iterator(); iter.hasNext();) {
            list.add(((FileFilter)iter.next()).clone());
        }
        clone.setFilter(list);
        return clone;
    }
    
    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FILTER)) {
            list.add(InstallOptionsModel.PROPERTY_FILTER);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected TypeConverter getTypeConverter(String property)
    {
        if(property.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            return FILTER_LIST_CONVERTER;
        }
        else {
            return super.getTypeConverter(property);
        }
    }
    
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            PropertyDescriptor descriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_FILTER, InstallOptionsPlugin.getResourceString("filter.property.name")){ //$NON-NLS-1$
                public CellEditor createPropertyEditor(Composite parent) 
                {
                    FileFilterCellEditor editor = new FileFilterCellEditor(InstallOptionsFileRequest.this, parent);
                    editor.setValidator(getValidator());
                    return editor;
                }
            };
            descriptor.setLabelProvider(FILTER_LABEL_PROVIDER);
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_FILTER));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_FILTER.equals(propName)) {
            return getFilter();
        }
        return super.getPropertyValue(propName);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            setFilter((List)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }
    
    public List getFilter()
    {
        return mFilter;
    }

    public void setFilter(List filter)
    {
        if(!mFilter.equals(filter)) {
            List oldFilter = mFilter;
            mFilter = filter;
            firePropertyChange(InstallOptionsModel.PROPERTY_FILTER, oldFilter, mFilter);
            setDirty(true);
        }
    }
}
