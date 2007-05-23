/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.properties.tabbed.section.IPropertySectionCreator;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.LabelPropertySectionCreator;
import net.sf.eclipsensis.installoptions.properties.validators.NSISEscapedStringLengthValidator;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

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
        return (isMultiLine()?MultiLineLabelProvider.INSTANCE:super.getDisplayLabelProvider());
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

    public Object getPropertyValue(Object propName)
    {
        if(InstallOptionsModel.PROPERTY_MULTILINE.equals(propName)) {
            return (isMultiLine()?Boolean.TRUE:Boolean.FALSE);
        }
        return super.getPropertyValue(propName);
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("text.property.name"); //$NON-NLS-1$;
            MultiLineTextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(this, InstallOptionsModel.PROPERTY_TEXT, propertyName);
            descriptor.setValidator(new ICellEditorValidator() {
                ICellEditorValidator mSingleLineValidator = new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE);
                ICellEditorValidator mMultiLineValidator = new NSISEscapedStringLengthValidator(InstallOptionsModel.PROPERTY_STATE);
                public String isValid(Object value)
                {
                    if(InstallOptionsLabel.this.isMultiLine()) {
                        return mMultiLineValidator.isValid(value);
                    }
                    else {
                        return mSingleLineValidator.isValid(value);
                    }
                }

            });
            descriptor.setMultiLine(isMultiLine());
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new LabelPropertySectionCreator(this);
    }

    public boolean isMultiLine()
    {
        return true;
    }
}
