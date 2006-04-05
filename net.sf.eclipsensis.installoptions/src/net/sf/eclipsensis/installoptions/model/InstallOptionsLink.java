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
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomColorPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsLink extends InstallOptionsUneditableElement
{
    public static final RGB DEFAULT_TXTCOLOR = new RGB(0,0,255);
    private static ILabelProvider cLabelProvider = new LabelProvider(){
        public String getText(Object element)
        {
            if(element instanceof RGB) {
                String s = TypeConverter.RGB_CONVERTER.asString(element);
                if(((RGB)element).equals(DEFAULT_TXTCOLOR)) {
                    s = new StringBuffer(s).append(" ").append(InstallOptionsPlugin.getResourceString("value.default")).toString(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return s;
            }
            else {
                return super.getText(element);
            }
        }
    };

    private String mState;
    private RGB mTxtColor;

    protected InstallOptionsLink(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_LINK;
    }

    protected void init()
    {
        super.init();
        mState = ""; //$NON-NLS-1$
    }

    /**
     * @return
     */
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("link.text.default"); //$NON-NLS-1$
    }

    public void setText(String s)
    {
    	//Convert special characters back to escaped form.
        s = (String)TypeConverter.INI_STRING_CONVERTER.asType(s,""); //$NON-NLS-1$
        super.setText(s);
    }

    /**
     * @return
     */
    protected Position getDefaultPosition()
    {
        return new Position(0,0,15,9);
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
            setDirty(true);
        }
    }

    public RGB getTxtColor()
    {
        return mTxtColor;
    }

    public void setTxtColor(RGB txtColor)
    {
        if((mTxtColor == null && txtColor != null) || (mTxtColor != null && !mTxtColor.equals(txtColor))) {
            RGB oldTxtColor = mTxtColor;
            mTxtColor = (DEFAULT_TXTCOLOR.equals(txtColor)?null:txtColor);
            firePropertyChange(InstallOptionsModel.PROPERTY_TXTCOLOR, oldTxtColor, txtColor);
            setDirty(true);
        }
    }

    protected TypeConverter getTypeConverter(String property)
    {
        if(property.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            return TypeConverter.RGB_CONVERTER;
        }
        else {
            return super.getTypeConverter(property);
        }
    }

    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            list.add(InstallOptionsModel.PROPERTY_STATE);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            list.add(InstallOptionsModel.PROPERTY_TXTCOLOR);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, propertyName);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            CustomColorPropertyDescriptor descriptor = new CustomColorPropertyDescriptor(InstallOptionsModel.PROPERTY_TXTCOLOR, InstallOptionsPlugin.getResourceString("txtcolor.property.name")); //$NON-NLS-1$;
            descriptor.setDefaultColor(DEFAULT_TXTCOLOR);
            descriptor.setLabelProvider(cLabelProvider);
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Object clone()
    {
        InstallOptionsLink clone = (InstallOptionsLink)super.clone();
        clone.setState(getState());
        if(mTxtColor != null) {
            clone.setTxtColor(new RGB(mTxtColor.red,mTxtColor.green,mTxtColor.blue));
        }
        return clone;
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_STATE.equals(propName)) {
            return getState();
        }
        if (InstallOptionsModel.PROPERTY_TXTCOLOR.equals(propName)) {
            return getTxtColor();
        }
        return super.getPropertyValue(propName);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_STATE)) {
            setState((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            setTxtColor((RGB)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }
}
