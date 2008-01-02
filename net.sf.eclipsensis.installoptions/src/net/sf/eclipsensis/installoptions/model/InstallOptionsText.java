/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.properties.descriptors.MultiLineTextPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.labelproviders.MultiLineLabelProvider;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class InstallOptionsText extends InstallOptionsEditableElement
{
    protected InstallOptionsText(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_TEXT;
    }

    protected ILabelProvider getDisplayLabelProvider()
    {
        if(getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE) &&
           getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE)) {
            return MultiLineLabelProvider.INSTANCE;
        }
        return super.getDisplayLabelProvider();
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
        String oldState = getState();
        String newState = oldState;
        if(flags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            if(!Common.isEmpty(newState)) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = newState.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if(Character.isDigit(chars[i])) {
                        buf.append(chars[i]);
                    }
                }
                newState = buf.toString();
            }
        }
        if(flags.contains(InstallOptionsModel.FLAGS_MULTILINE)) {
            if(!Common.isEmpty(newState)) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = TypeConverter.ESCAPED_STRING_CONVERTER.asString(newState).toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    switch(chars[i])
                    {
                        case SWT.CR:
                            if(i < chars.length - 1) {
                                if(chars[i+1] == SWT.LF) {
                                    i++;
                                }
                            }
                        case SWT.LF:
                            buf.append(SWT.CR).append(SWT.LF);
                            break;
                        default:
                            buf.append(chars[i]);
                            break;
                    }
                }
                newState = (String)TypeConverter.ESCAPED_STRING_CONVERTER.asType(buf.toString());
            }
        }
        if(!Common.stringsAreEqual(newState,oldState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, newState));
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
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$
            MultiLineTextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(this, InstallOptionsModel.PROPERTY_STATE, propertyName);
            descriptor.setValidator(new ICellEditorValidator() {
                ICellEditorValidator mSingleLineValidator = new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE);
                ICellEditorValidator mMultiLineValidator = new NSISEscapedStringLengthValidator(InstallOptionsModel.PROPERTY_STATE);
                public String isValid(Object value)
                {
                    if(getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE) &&
                       getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE)) {
                        return mMultiLineValidator.isValid(value);
                    }
                    else {
                        return mSingleLineValidator.isValid(value);
                    }
                }

            });
            if(getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE)) {
                descriptor.setMultiLine(getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE));
            }
            else {
                descriptor.setMultiLine(false);
            }
            if(getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                descriptor.setOnlyNumbers(getFlags().contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
            }
            else {
                descriptor.setOnlyNumbers(false);
            }
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new TextPropertySectionCreator(this);
    }
}
