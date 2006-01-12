/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public abstract class InstallOptionsUneditableElement extends InstallOptionsWidget
{
    private String mText;

    protected InstallOptionsUneditableElement(INISection section)
    {
        super(section);
    }

    protected void init()
    {
        super.init();
        mText = ""; //$NON-NLS-1$
    }

    /**
     * @param type
     */
    protected void setDefaults()
    {
        super.setDefaults();
        mText = getDefaultText();
    }

    public Object clone()
    {
        InstallOptionsUneditableElement clone = (InstallOptionsUneditableElement)super.clone();
        clone.setText(getText());
        return clone;
    }

    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            list.add(InstallOptionsModel.PROPERTY_TEXT);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TEXT, InstallOptionsPlugin.getResourceString("text.property.name")); //$NON-NLS-1$;
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_TEXT));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_TEXT.equals(propName)) {
            return getText();
        }
        return super.getPropertyValue(propName);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            setText((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String s)
    {
        if(!mText.equals(s)) {
            String oldText = mText;
            mText = s;
            firePropertyChange(InstallOptionsModel.PROPERTY_TEXT, oldText, mText);
            setDirty(true);
        }
    }

    protected String getDefaultText()
    {
        return ""; //$NON-NLS-1$
    }
}