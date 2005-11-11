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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INIKeyValue;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.Common;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsUnknown extends InstallOptionsWidget
{
    private String mType;

    public InstallOptionsUnknown(INISection section)
    {
        super(section);
    }

    protected void loadSection(INISection section)
    {
        String type = null;
        if(section != null) {
            INIKeyValue[] keyValues = section.findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
            if(!Common.isEmptyArray(keyValues)) {
                type = keyValues[0].getValue();
            }
        }
        mType = (type == null?"":type); //$NON-NLS-1$
        super.loadSection(section);
    }

    protected void setDefaults()
    {
        super.setDefaults();
        mType = InstallOptionsPlugin.getResourceString("unknown.type.default"); //$NON-NLS-1$
    }

    public String getType()
    {
        return mType;
    }

    public void setType(String type)
    {
        String oldType = mType;
        mType = type;
        if(!Common.stringsAreEqual(oldType, mType)) {
            firePropertyChange(InstallOptionsModel.PROPERTY_TYPE, oldType, mType);
        }
    }

    public void setPropertyValue(Object id, Object value)
    {
        if (InstallOptionsModel.PROPERTY_TYPE.equals(id)) {
            setType((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,50,50);
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TYPE)) {
            return new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TYPE, InstallOptionsPlugin.getResourceString("type.property.name")); //$NON-NLS-1$
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }
}
