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
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class InstallOptionsCheckBox extends InstallOptionsButton
{
    private static final int DEFAULT_STATE = 0;
    private static final String[] STATE_DATA = {"",InstallOptionsModel.STATE_UNCHECKED, //$NON-NLS-1$
                                                InstallOptionsModel.STATE_CHECKED};
    private static final String[] STATE_DISPLAY = {InstallOptionsPlugin.getResourceString("state.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.unchecked"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.checked")}; //$NON-NLS-1$

    protected InstallOptionsCheckBox(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_CHECKBOX;
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,65,10);
    }

    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("checkbox.text.default"); //$NON-NLS-1$
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            CustomComboBoxPropertyDescriptor descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE,
                    InstallOptionsPlugin.getResourceString("state.property.name"), //$NON-NLS-1$
                    getStateData(), getStateDisplay(), getStateDefault());
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    /**
     * @return
     */
    protected int getStateDefault()
    {
        return DEFAULT_STATE;
    }

    /**
     * @return
     */
    protected String[] getStateDisplay()
    {
        return STATE_DISPLAY;
    }

    /**
     * @return
     */
    protected String[] getStateData()
    {
        return STATE_DATA;
    }
}
