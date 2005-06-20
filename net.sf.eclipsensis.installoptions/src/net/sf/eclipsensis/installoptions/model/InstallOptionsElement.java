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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsElement implements IPropertySource, Cloneable
{
    private INISection mSection;
    private String mType=""; //$NON-NLS-1$
    protected PropertyChangeSupport mListeners = new PropertyChangeSupport(this);
    protected ArrayList mModelCommandListeners = new ArrayList();
    private List mPropertyNames;
    private boolean mDirty = false;

    public InstallOptionsElement(String type)
    {
        setType(type);
    }

    protected boolean isDirty()
    {
        return mDirty;
    }
    
    protected void setDirty(boolean dirty)
    {
        mDirty = dirty;
    }
    
    private void setType(String type)
    {
        mType = type;
    }

    public final String getType()
    {
        return mType;
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
        for (Iterator iter = mModelCommandListeners.iterator(); iter.hasNext();) {
            IModelCommandListener element = (IModelCommandListener)iter.next();
            element.executeModelCommand(e);
        }
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

    public Image getIcon()
    {
        return getIconImage();
    }

    public void setPropertyValue(Object id, Object value)
    {
    }

    public Object getPropertyValue(Object id)
    {
        if (InstallOptionsModel.PROPERTY_TYPE.equals(id)) {
            return getType();
        }
        return null;
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsElement element = (InstallOptionsElement)super.clone();
        element.mListeners = new PropertyChangeSupport(element);
        element.mModelCommandListeners = new ArrayList();
        element.setType(getType());
        return element;
    }

    protected SetPropertyValueCommand createSetPropertyCommand(String property, Object value)
    {
        SetPropertyValueCommand command = new SetPropertyValueCommand(property);
        command.setPropertyId(property);
        command.setPropertyValue(value);
        command.setTarget(this);
        return command;
    }

    public List getPropertyNames()
    {
        if(mPropertyNames == null) {
            synchronized(this) {
                if(mPropertyNames == null) {
                    mPropertyNames = doGetPropertyNames();
                }
            }
        }
        return mPropertyNames;
    }
    
    public void loadSection(INISection section)
    {
        mSection = section;
        List properties = getPropertyNames();
        for (Iterator iter=properties.iterator(); iter.hasNext(); ) {
            String property = (String)iter.next();
            INIKeyValue[] keyValues = section.findKeyValues(property);
            if(!Common.isEmptyArray(keyValues)) {
                String value = keyValues[0].getValue();
                TypeConverter converter = getTypeConverter(property);
                setPropertyValue(property,(converter != null?converter.asType(value):value));
            }
        }
        setDirty(false);
    }
    
    public INISection saveSection()
    {
        if(mSection == null) {
            mSection = new INISection();
        }
        int n = mSection.getSize();
        if(n > 0) {
            INILine lastChild = mSection.getChild(n-1);
            if(!lastChild.getClass().equals(INILine.class) || !Common.isEmpty(lastChild.getText())) {
                mSection.addChild(new INILine());
            }
            else {
                n--;
            }
        }
        else {
            mSection.addChild(new INILine());
        }
        if(isDirty()) {
            mSection.setName(getSectionName());
            List properties = getPropertyNames();
            for (Iterator iter=properties.iterator(); iter.hasNext(); ) {
                String property = (String)iter.next();
                TypeConverter converter = getTypeConverter(property);
                Object propertyValue = getPropertyValue(property);
                String value = (propertyValue != null?(converter != null?converter.asString(propertyValue):propertyValue.toString()):""); //$NON-NLS-1$
                value = (value == null?"":value); //$NON-NLS-1$
    
                INIKeyValue[] keyValues = mSection.findKeyValues(property);
                if(!Common.isEmptyArray(keyValues)) {
                    keyValues[0].setValue(value);
                }
                else {
                    if(value.length() > 0) {
                        INIKeyValue keyValue = new INIKeyValue(property);
                        keyValue.setValue(value);
                        mSection.addChild(n++,keyValue);
                    }
                }
            }
            
            setDirty(false);
        }
        return mSection;
    }
    
    protected TypeConverter getTypeConverter(String property)
    {
        return null;
    }
    
    protected List doGetPropertyNames()
    {
        return new ArrayList();
    }
    
    public Image getIconImage()
    {
        return null;
    }
    
    protected abstract String getSectionName();
}
