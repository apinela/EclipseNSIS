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

import java.util.Map;

import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.gef.requests.CreationFactory;

public class InstallOptionsElementFactory implements CreationFactory
{
    private static Map cCachedFactories = new CaseInsensitiveMap();
    static {
        String[] controlTypes = InstallOptionsModel.getInstance().getControlTypes();
        for (int i = 0; i < controlTypes.length; i++) {
            cCachedFactories.put(controlTypes[i], new InstallOptionsElementFactory(controlTypes[i]));
        }
    }
    public static InstallOptionsElementFactory getFactory(String type)
    {
        return (InstallOptionsElementFactory)cCachedFactories.get(type);
    }
    
    private String mType;

    /**
     * 
     */
    private InstallOptionsElementFactory(String type)
    {
        super();
        mType = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject()
    {
        if (InstallOptionsModel.TYPE_LABEL.equalsIgnoreCase(mType)) {
            return new InstallOptionsLabel();
        }
        if (InstallOptionsModel.TYPE_BITMAP.equalsIgnoreCase(mType)) {
            return new InstallOptionsBitmap();
        }
        if (InstallOptionsModel.TYPE_ICON.equalsIgnoreCase(mType)) {
            return new InstallOptionsIcon();
        }
        if (InstallOptionsModel.TYPE_LINK.equalsIgnoreCase(mType)) {
            return new InstallOptionsLink();
        }
        if (InstallOptionsModel.TYPE_BUTTON.equalsIgnoreCase(mType)) {
            return new InstallOptionsButton();
        }
        if (InstallOptionsModel.TYPE_CHECKBOX.equalsIgnoreCase(mType)) {
            return new InstallOptionsCheckBox();
        }
        if (InstallOptionsModel.TYPE_RADIOBUTTON.equalsIgnoreCase(mType)) {
            return new InstallOptionsRadioButton();
        }
        if (InstallOptionsModel.TYPE_FILEREQUEST.equalsIgnoreCase(mType)) {
            return new InstallOptionsFileRequest();
        }
        if (InstallOptionsModel.TYPE_DIRREQUEST.equalsIgnoreCase(mType)) {
            return new InstallOptionsDirRequest();
        }
        if (InstallOptionsModel.TYPE_GROUPBOX.equalsIgnoreCase(mType)) {
            return new InstallOptionsGroupBox();
        }
        if (InstallOptionsModel.TYPE_TEXT.equalsIgnoreCase(mType)) {
            return new InstallOptionsText();
        }
        if (InstallOptionsModel.TYPE_PASSWORD.equalsIgnoreCase(mType)) {
            return new InstallOptionsPassword();
        }
        if (InstallOptionsModel.TYPE_DROPLIST.equalsIgnoreCase(mType)) {
            return new InstallOptionsDropList();
        }
        if (InstallOptionsModel.TYPE_COMBOBOX.equalsIgnoreCase(mType)) {
            return new InstallOptionsCombobox();
        }
        if (InstallOptionsModel.TYPE_LISTBOX.equalsIgnoreCase(mType)) {
            return new InstallOptionsListbox();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return mType;
    }
}
