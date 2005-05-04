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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsElement implements IPropertySource, Cloneable
{
    protected transient PropertyChangeSupport mListeners = new PropertyChangeSupport(this);

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

    final Object getPropertyValue(String propName)
    {
        return null;
    }

    public boolean isPropertySet(Object propName)
    {
        return isPropertySet((String)propName);
    }

    final boolean isPropertySet(String propName)
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

    final void resetPropertyValue(String propName)
    {
    }

    final void setPropertyValue(String propName, Object val)
    {
    }

    public void update()
    {
    }

    public Image getIcon()
    {
        return getIconImage();
    }

    abstract public Image getIconImage();

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return null;
    }

    public Object getPropertyValue(Object propName)
    {
        return null;
    }

    /**
     *  
     */
    public boolean isPropertySet()
    {
        return true;
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsElement element = (InstallOptionsElement)super.clone();
        element.mListeners = new PropertyChangeSupport(element);
        return element;
    }
}
