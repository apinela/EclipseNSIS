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

public class InstallOptionsDirRequest extends InstallOptionsPathRequest
{
    public static Image DIRREQUEST_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("dirrequest.type.small.icon")); //$NON-NLS-1$

    private String mRoot = ""; //$NON-NLS-1$

    public InstallOptionsDirRequest()
    {
        super(InstallOptionsModel.TYPE_DIRREQUEST);
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsDirRequest clone = (InstallOptionsDirRequest)super.clone();
        clone.setRoot(getRoot());
        return clone;
    }
    
    public Image getIconImage()
    {
        return DIRREQUEST_ICON;
    }

    protected void addPropertyName(List list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_ROOT)) {
            list.add(InstallOptionsModel.PROPERTY_ROOT);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_ROOT)) {
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_ROOT, InstallOptionsPlugin.getResourceString("root.property.name")); //$NON-NLS-1$
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_ROOT));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_ROOT.equals(propName)) {
            return getRoot();
        }
        return super.getPropertyValue(propName);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_ROOT)) {
            setRoot((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }
    
    public String getRoot()
    {
        return mRoot;
    }

    public void setRoot(String root)
    {
        if(!mRoot.equals(root)) {
            String oldRoot = mRoot;
            mRoot = root;
            firePropertyChange(InstallOptionsModel.PROPERTY_ROOT, oldRoot, mRoot);
            setDirty(true);
        }
    }
}
