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
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener;
import net.sf.eclipsensis.installoptions.model.commands.ModelCommandEvent;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsElement implements IPropertySource, Cloneable
{
    private String mType=""; //$NON-NLS-1$
    protected PropertyChangeSupport mListeners = new PropertyChangeSupport(this);
    protected ArrayList mModelCommandListeners = new ArrayList();

    public InstallOptionsElement(String type)
    {
        setType(type);
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

    public void update()
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
            return mType;
        }
        return null;
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsElement element = (InstallOptionsElement)super.clone();
        element.mListeners = new PropertyChangeSupport(element);
        element.setType(getType());

        return element;
    }

    public abstract Image getIconImage();

    protected SetPropertyValueCommand createSetPropertyCommand(String property, Object value)
    {
        SetPropertyValueCommand command = new SetPropertyValueCommand(property);
        command.setPropertyId(property);
        command.setPropertyValue(value);
        command.setTarget(this);
        return command;
    }
}
