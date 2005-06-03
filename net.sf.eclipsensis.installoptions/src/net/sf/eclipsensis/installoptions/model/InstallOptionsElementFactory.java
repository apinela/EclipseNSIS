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

import org.eclipse.gef.requests.CreationFactory;

public class InstallOptionsElementFactory implements CreationFactory
{
    private String mType;
    /**
     * 
     */
    public InstallOptionsElementFactory(String type)
    {
        super();
        mType = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject()
    {
        if (InstallOptionsModel.TYPE_LABEL.equals(mType)) {
            return new InstallOptionsLabel();
        }
        if (InstallOptionsModel.TYPE_BITMAP.equals(mType)) {
            return new InstallOptionsBitmap();
        }
        if (InstallOptionsModel.TYPE_ICON.equals(mType)) {
            return new InstallOptionsIcon();
        }
        if (InstallOptionsModel.TYPE_LINK.equals(mType)) {
            return new InstallOptionsLink();
        }
        if (InstallOptionsModel.TYPE_BUTTON.equals(mType)) {
            return new InstallOptionsButton();
        }
        if (InstallOptionsModel.TYPE_CHECKBOX.equals(mType)) {
            return new InstallOptionsCheckBox();
        }
        if (InstallOptionsModel.TYPE_RADIOBUTTON.equals(mType)) {
            return new InstallOptionsRadioButton();
        }
        if (InstallOptionsModel.TYPE_FILEREQUEST.equals(mType)) {
            return new InstallOptionsFileRequest();
        }
        if (InstallOptionsModel.TYPE_DIRREQUEST.equals(mType)) {
            return new InstallOptionsDirRequest();
        }
        if (InstallOptionsModel.TYPE_GROUPBOX.equals(mType)) {
            return new InstallOptionsGroupBox();
        }
        if (InstallOptionsModel.TYPE_TEXT.equals(mType)) {
            return new InstallOptionsText();
        }
        if (InstallOptionsModel.TYPE_PASSWORD.equals(mType)) {
            return new InstallOptionsPassword();
        }
        if (InstallOptionsModel.TYPE_DROPLIST.equals(mType)) {
            return new InstallOptionsDropList();
        }
        if (InstallOptionsModel.TYPE_COMBOBOX.equals(mType)) {
            return new InstallOptionsCombobox();
        }
        if (InstallOptionsModel.TYPE_LISTBOX.equals(mType)) {
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
