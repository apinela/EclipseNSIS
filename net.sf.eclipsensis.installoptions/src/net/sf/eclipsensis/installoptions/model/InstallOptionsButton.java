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

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsButton extends InstallOptionsUneditableElement
{
    private static Image BUTTON_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("button.type.small.icon")); //$NON-NLS-1$

    private String mState;

    public InstallOptionsButton()
    {
        this(InstallOptionsModel.TYPE_BUTTON);
    }

    protected InstallOptionsButton(String type)
    {
        super(type);
        mState = ""; //$NON-NLS-1$
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsButton clone = (InstallOptionsButton)super.clone();
        clone.setState(getState());
        return clone;
    }

    /**
     * @return
     */
    protected Position getDefaultPosition()
    {
        return new Position(0,0,50,15);
    }

    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("button.text.default"); //$NON-NLS-1$
    }

    protected void addPropertyName(List list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            list.add(InstallOptionsModel.PROPERTY_STATE);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$;
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }
    
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_STATE.equals(propName)) {
            return getState();
        }
        return super.getPropertyValue(propName);
    }
    
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_STATE)) {
            setState((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public Image getIconImage()
    {
        return BUTTON_ICON;
    }
    
    public String getState()
    {
        return mState;
    }
    
    public void setState(String state)
    {
        if(!mState.equals(state)) {
            String oldState = mState;
            mState = state;
            firePropertyChange(InstallOptionsModel.PROPERTY_STATE, oldState, mState);
        }
    }
}
