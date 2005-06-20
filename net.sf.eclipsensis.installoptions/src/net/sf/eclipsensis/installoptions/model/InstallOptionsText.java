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
import net.sf.eclipsensis.installoptions.properties.descriptors.MultiLineTextPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.MultiLineLabelProvider;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class InstallOptionsText extends InstallOptionsEditableElement
{
    public static Image TEXT_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("text.type.small.icon")); //$NON-NLS-1$
    
    public InstallOptionsText()
    {
        this(InstallOptionsModel.TYPE_TEXT);
    }
    
    /**
     * @param type
     */
    public InstallOptionsText(String type)
    {
        super(type);
    }

    public Image getIconImage()
    {
        return TEXT_ICON;
    }
    
    /**
     * @return
     */
    protected String getDefaultState()
    {
        return InstallOptionsPlugin.getResourceString("text.state.default"); //$NON-NLS-1$
    }

    public void setFlags(List flags)
    {
        if(flags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            String oldState = getState();
            if(!Common.isEmpty(oldState)) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = oldState.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if(Character.isDigit(chars[i])) {
                        buf.append(chars[i]);
                    }
                }
                String newState = buf.toString();
                if(!Common.stringsAreEqual(newState,oldState)) {
                    fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, newState));
                }
            }
        }
        super.setFlags(flags);
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,122,13);
    }
    
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            MultiLineTextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$
            descriptor.setLabelProvider(MultiLineLabelProvider.INSTANCE);
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
            descriptor.setMultiLine(getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE));
            descriptor.setOnlyNumbers(getFlags().contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }
}
