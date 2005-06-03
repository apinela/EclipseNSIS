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
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomColorPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsLink extends InstallOptionsUneditableElement
{
    private static Image LINK_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("link.type.small.icon")); //$NON-NLS-1$
    public static final RGB DEFAULT_TXTCOLOR = new RGB(0,0,255);
    
    private String mState;
    private RGB mTxtColor;
    
    private static ILabelProvider cLabelProvider = new LabelProvider(){
        public String getText(Object element) 
        {
            if(element instanceof RGB) {
                StringBuffer buf = new StringBuffer("0x"); //$NON-NLS-1$
                RGB rgb = (RGB)element;
                buf.append(ColorManager.rgbToHex(rgb));
                if(rgb.equals(DEFAULT_TXTCOLOR)) {
                    buf.append(" ").append(InstallOptionsPlugin.getResourceString("value.default")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return buf.toString();
            }
            else {
                return super.getText(element);
            }
        }
    };
    
    /**
     * 
     */
    public InstallOptionsLink()
    {
        this(InstallOptionsModel.TYPE_LINK);
    }

    /**
     * @param type
     */
    public InstallOptionsLink(String type)
    {
        super(type);
        mState = ""; //$NON-NLS-1$
    }

    public Image getIconImage()
    {
        return LINK_ICON;
    }

    /**
     * @return
     */
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("link.text.default"); //$NON-NLS-1$
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
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$;
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
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
    
    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsLink clone = (InstallOptionsLink)super.clone();
        clone.setState(getState());
        if(mTxtColor != null) {
            clone.setTxtColor(new RGB(clone.mTxtColor.red,clone.mTxtColor.green,clone.mTxtColor.blue));
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
