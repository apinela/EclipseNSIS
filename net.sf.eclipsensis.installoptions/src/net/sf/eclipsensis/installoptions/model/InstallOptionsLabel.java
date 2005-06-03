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
import net.sf.eclipsensis.installoptions.properties.descriptors.MultiLineTextPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.MultiLineLabelProvider;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsLabel extends InstallOptionsUneditableElement
{
    private static Image LABEL_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("label.type.small.icon")); //$NON-NLS-1$

    public InstallOptionsLabel()
    {
        this(InstallOptionsModel.TYPE_LABEL);
    }

    protected InstallOptionsLabel(String type)
    {
        super(type);
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
            TextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(InstallOptionsModel.PROPERTY_TEXT, InstallOptionsPlugin.getResourceString("text.property.name")); //$NON-NLS-1$;
            descriptor.setLabelProvider(MultiLineLabelProvider.INSTANCE);
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_TEXT));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Image getIconImage()
    {
        return LABEL_ICON;
    }
}
