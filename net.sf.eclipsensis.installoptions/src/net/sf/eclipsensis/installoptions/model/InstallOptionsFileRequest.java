/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.editors.FileFilterCellEditor;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsFileRequest extends InstallOptionsPathRequest
{
    public  static final char FILTER_SEPARATOR = ';';

    public static final TypeConverter FILEFILTER_LIST_CONVERTER = new TypeConverter(){
        public String asString(Object o)
        {
            return Common.flatten(((List)o).toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object asType(String s)
        {
            List list = new ArrayList();
            String[] tokens = Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR,false);
            for (int i = 0; i < (tokens.length-1); i+= 2) {
                String description = tokens[i];
                String[] temp = Common.tokenize(tokens[i+1],FILTER_SEPARATOR,false);
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
            return list;
        }
    };

    public static final LabelProvider FILTER_LABEL_PROVIDER = new LabelProvider(){
        public String getText(Object element)
        {
            if(element instanceof List) {
                return FILEFILTER_LIST_CONVERTER.asString(element);
            }
            else {
                return super.getText(element);
            }
        }
    };

    private List mFilter;

    public boolean usesOtherTab()
    {
        return true;
    }

    protected InstallOptionsFileRequest(INISection section)
    {
        super(section);
    }

    protected void init()
    {
        super.init();
        mFilter = new ArrayList();
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_FILEREQUEST;
    }

    public Object clone()
    {
        InstallOptionsFileRequest clone = (InstallOptionsFileRequest)super.clone();
        clone.mFilter = new ArrayList();
        clone.setFilter(mFilter);
        return clone;
    }

    protected void addPropertyName(List list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FILTER)) {
            list.add(InstallOptionsModel.PROPERTY_FILTER);
        }
        else if(!setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            super.addPropertyName(list, setting);
        }
    }

    protected TypeConverter loadTypeConverter(String property, Object value)
    {
        if(property.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            return FILEFILTER_LIST_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new PathRequestPropertySectionCreator(this);
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            String propertyName = InstallOptionsPlugin.getResourceString("filter.property.name"); //$NON-NLS-1$
            PropertyDescriptor descriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_FILTER, propertyName){
                public CellEditor createPropertyEditor(Composite parent)
                {
                    final FileFilterCellEditor editor = new FileFilterCellEditor(InstallOptionsFileRequest.this, parent);
                    editor.setValidator(getValidator());
                    final PropertyChangeListener listener = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            if(evt.getPropertyName().equals(getId())) {
                                editor.setValue(evt.getNewValue());
                            }
                        }
                    };
                    InstallOptionsFileRequest.this.addPropertyChangeListener(listener);
                    editor.getControl().addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e)
                        {
                            InstallOptionsFileRequest.this.removePropertyChangeListener(listener);
                        }
                    });
                    return editor;
                }
            };
            descriptor.setLabelProvider(FILTER_LABEL_PROVIDER);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
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
            mFilter = (List)FILEFILTER_LIST_CONVERTER.makeCopy(filter);
            firePropertyChange(InstallOptionsModel.PROPERTY_FILTER, oldFilter, mFilter);
            setDirty(true);
        }
    }
}
