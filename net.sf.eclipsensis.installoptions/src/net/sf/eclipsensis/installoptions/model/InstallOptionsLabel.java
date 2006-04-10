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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.descriptors.MultiLineTextPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.MultiLineLabelProvider;
import net.sf.eclipsensis.installoptions.properties.validators.NSISEscapedStringLengthValidator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsLabel extends InstallOptionsUneditableElement
{
    protected InstallOptionsLabel(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_LABEL;
    }

    protected ILabelProvider getDisplayLabelProvider()
    {
        return MultiLineLabelProvider.INSTANCE;
    }

    /**
     * @return
     */
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("label.text.default"); //$NON-NLS-1$
    }

    /**
     * @return
     */
    protected Position getDefaultPosition()
    {
        return new Position(0,0,19,9);
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("text.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(InstallOptionsModel.PROPERTY_TEXT, propertyName);
            descriptor.setValidator(new NSISEscapedStringLengthValidator(propertyName));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }
}
