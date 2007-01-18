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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.CaseInsensitiveMap;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsElement implements IPropertySource, Cloneable
{
    private static final IPropertyDescriptor cNullPropertyDescriptor = new IPropertyDescriptor(){
        public CellEditor createPropertyEditor(Composite parent)
        {
            return null;
        }

        public String getCategory()
        {
            return null;
        }

        public String getDescription()
        {
            return null;
        }

        public String getDisplayName()
        {
            return null;
        }

        public String[] getFilterFlags()
        {
            return null;
        }

        public Object getHelpContextIds()
        {
            return null;
        }

        public Object getId()
        {
            return null;
        }

        public ILabelProvider getLabelProvider()
        {
            return null;
        }

        public boolean isCompatibleWith(IPropertyDescriptor anotherProperty)
        {
            return false;
        }
    };

    protected static final String OLD_METADATA_PREFIX = ";InstallOptions Editor Guides (DO NOT EDIT):"; //$NON-NLS-1$
    protected static final String METADATA_PREFIX = ";InstallOptions Editor Metadata (DO NOT EDIT):"; //$NON-NLS-1$

    private INISection mSection = null;
    protected PropertyChangeSupport mListeners = new PropertyChangeSupport(this);
    protected ArrayList mModelCommandListeners = new ArrayList();
    private boolean mDirty = false;
    protected Map mDescriptors = new HashMap();
    private Map mTypeConverters = null;

    private INIComment mMetadataComment;

    public InstallOptionsElement(INISection section)
    {
        init();
        if(section != null) {
            loadSection(section);
        }
        else {
            setDefaults();
        }
    }

    protected void init()
    {
    }

    protected void setDefaults()
    {
    }

    public boolean isDirty()
    {
        return mDirty;
    }

    protected void setDirty(boolean dirty)
    {
        mDirty = dirty;
    }

    public void addModelCommandListener(IModelCommandListener l)
    {
        if(!mModelCommandListeners.contains(l)) {
            mModelCommandListeners.add(l);
        }
    }

    public void removeModelCommandListener(IModelCommandListener l)
    {
        mModelCommandListeners.remove(l);
    }

    protected void fireModelCommand(Command cmd)
    {
        ModelCommandEvent e = new ModelCommandEvent(this,cmd);
        IModelCommandListener[] listeners = (IModelCommandListener[])mModelCommandListeners.toArray(new IModelCommandListener[mModelCommandListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].executeModelCommand(e);
        }
    }

    public final IPropertyDescriptor getPropertyDescriptor(String name)
    {
        if(getPropertyNames().contains(name)) {
            return doGetPropertyDescriptor(name);
        }
        return null;
    }

    /**
     * @param name
     * @return
     */
    private IPropertyDescriptor doGetPropertyDescriptor(String name)
    {
        IPropertyDescriptor descriptor = (IPropertyDescriptor)mDescriptors.get(name);
        if(descriptor == null) {
            descriptor = createPropertyDescriptor(name);
            if(descriptor != null) {
                mDescriptors.put(name,descriptor);
            }
            else {
                mDescriptors.put(name, (descriptor = cNullPropertyDescriptor));
            }
        }
        return descriptor;
    }

    public final IPropertyDescriptor[] getPropertyDescriptors()
    {
        Collection names = getPropertyNames();
        ArrayList list = new ArrayList();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            IPropertyDescriptor descriptor = doGetPropertyDescriptor((String)iter.next());
            if(descriptor != cNullPropertyDescriptor) {
                list.add(descriptor);
            }
        }
        return (IPropertyDescriptor[])list.toArray(new IPropertyDescriptor[list.size()]);
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String prop, Object old, Object newValue)
    {
        mListeners.firePropertyChange(prop, old, newValue);
    }

    public Object getEditableValue()
    {
        return this;
    }

    public boolean isPropertySet(Object id)
    {
        return true;
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.removePropertyChangeListener(l);
    }

    public void resetPropertyValue(Object propName)
    {
    }

    public final Image getIcon()
    {
        return getIconImage();
    }

    public void setPropertyValue(Object id, Object value)
    {
    }

    public String getStringPropertyValue(Object id)
    {
        Object value = getPropertyValue(id);
        if(value != null) {
            return value.toString();
        }
        return null;
    }

    public Object getPropertyValue(Object id)
    {
        if (InstallOptionsModel.PROPERTY_TYPE.equals(id)) {
            return getType();
        }
        return null;
    }

    public Object clone()
    {
        try {
            InstallOptionsElement element = (InstallOptionsElement)super.clone();
            element.mListeners = new PropertyChangeSupport(element);
            element.mModelCommandListeners = new ArrayList();
            element.mDescriptors = new HashMap();
            element.mSection = null;
            element.mDirty = true;
            element.mMetadataComment = null;
            return element;
        }
        catch(CloneNotSupportedException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return null;
        }
    }

    protected SetPropertyValueCommand createSetPropertyCommand(String property, Object value)
    {
        SetPropertyValueCommand command = new SetPropertyValueCommand(property);
        command.setPropertyId(property);
        command.setPropertyValue(value);
        command.setTarget(this);
        return command;
    }

    protected INIComment getMetadataComment()
    {
        return mMetadataComment;
    }

    protected void setMetadataComment(INIComment metadataComment)
    {
        mMetadataComment = metadataComment;
    }

    protected void loadSection(INISection section)
    {
        mSection = section;
        Collection properties = doGetPropertyNames();
        for (Iterator iter=properties.iterator(); iter.hasNext(); ) {
            String property = (String)iter.next();
            INIKeyValue[] keyValues = section.findKeyValues(property);
            if(!Common.isEmptyArray(keyValues)) {
                String value = keyValues[0].getValue();
                TypeConverter converter = getTypeConverter(property, value);
                setPropertyValue(property,(converter != null?converter.asType(value):value));
            }
        }
        mMetadataComment = null;
        for(Iterator iter=mSection.getChildren().iterator(); iter.hasNext(); ) {
            INILine line = (INILine)iter.next();
            if(line instanceof INIComment) {
                String text = line.getText().trim();
                if(text.startsWith(OLD_METADATA_PREFIX) || text.startsWith(METADATA_PREFIX)) {
                    mMetadataComment = (INIComment)line;
                    break;
                }
            }
        }
        setDirty(false);
    }

    public INISection getSection()
    {
        if(mSection == null) {
            mSection = new INISection(); //$NON-NLS-1$
        }
        return mSection;
    }

    public final INISection updateSection()
    {
        INISection section = getSection();
        int n = section.getSize();
        while(n > 0) {
            INILine lastChild = section.getChild(n-1);
            if(lastChild.getClass().equals(INILine.class) && Common.isEmpty(lastChild.getText())) {
                n--;
            }
            else {
                break;
            }
        }
        if(isDirty()) {
            section.setName(getSectionName());
            Collection properties = doGetPropertyNames();
            for (Iterator iter=properties.iterator(); iter.hasNext(); ) {
                String property = (String)iter.next();
                Object propertyValue = getPropertyValue(property);
                TypeConverter converter = getTypeConverter(property, propertyValue);
                String value = (propertyValue != null?(converter != null?converter.asString(propertyValue):propertyValue.toString()):""); //$NON-NLS-1$
                value = (value == null?"":value); //$NON-NLS-1$

                INIKeyValue[] keyValues = section.findKeyValues(property);
                if(!Common.isEmptyArray(keyValues)) {
                    keyValues[0].setValue(value);
                }
                else {
                    if(value.length() > 0) {
                        INIKeyValue keyValue = new INIKeyValue(property);
                        keyValue.setValue(value);
                        section.addChild(n++,keyValue);
                    }
                }
            }

            for(Iterator iter=section.getChildren().iterator(); iter.hasNext(); ) {
                INILine line = (INILine)iter.next();
                if(iter.hasNext() && line.getDelimiter() == null) {
                    line.setDelimiter(INSISConstants.LINE_SEPARATOR);
                }
            }
            setDirty(false);
        }
        return section;
    }

    protected final TypeConverter getTypeConverter(String property, Object value)
    {
        if(mTypeConverters == null) {
            mTypeConverters = new CaseInsensitiveMap();
        }
        TypeConverter typeConverter = (TypeConverter)mTypeConverters.get(property);
        if(typeConverter == null) {
            typeConverter = loadTypeConverter(property, value);
            mTypeConverters.put(property, typeConverter);
        }
        return typeConverter;
    }

    protected TypeConverter loadTypeConverter(String property, Object value)
    {
        return TypeConverter.STRING_CONVERTER;
    }

    public Image getIconImage()
    {
        return null;
    }

    protected Collection getPropertyNames()
    {
        return doGetPropertyNames();
    }

    protected abstract Collection doGetPropertyNames();
    protected abstract IPropertyDescriptor createPropertyDescriptor(String name);
    public abstract String getType();
    protected abstract String getSectionName();
}
