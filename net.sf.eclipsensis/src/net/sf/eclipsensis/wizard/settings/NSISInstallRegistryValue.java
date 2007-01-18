/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.CommonImages;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallRegistryValueDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallRegistryValue extends NSISInstallRegistryItem
{
	private static final long serialVersionUID = 4012648943855296196L;

    public static final String TYPE = "Registry Value"; //$NON-NLS-1$
    private int mRootKey = HKLM;
    private String mSubKey = null;
    private String mData = null;
    private String mValue = null;
    private int mValueType = REG_SZ;

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.regvalue.type.name"), CommonImages.REG_SZ_IMAGE, NSISInstallRegistryValue.class); //$NON-NLS-1$
    }

    protected int getRootKeyInternal()
    {
        return mRootKey;
    }

    protected String getSubKeyInternal()
    {
        return mSubKey;
    }

    protected void setRootKeyInternal(int rootKey)
    {
        mRootKey = rootKey;
    }

    protected void setSubKeyInternal(String subKey)
    {
        mSubKey = subKey;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    protected void makeDisplayName(StringBuffer buf)
    {
        super.makeDisplayName(buf);
        buf.append("\\").append(mValue).toString(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallRegistryValueDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        switch(mValueType)
        {
            case REG_BIN:
            case REG_DWORD:
                return CommonImages.REG_DWORD_IMAGE;
            default:
                return CommonImages.REG_SZ_IMAGE;
        }
    }

    /**
     * @return Returns the data.
     */
    public String getData()
    {
        return mData;
    }

    /**
     * @param data The data to set.
     */
    public void setData(String data)
    {
        mData = data;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return mValue;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value)
    {
        mValue = value;
    }

    /**
     * @return Returns the valueType.
     */
    public int getValueType()
    {
        return mValueType;
    }

    /**
     * @param valueType The valueType to set.
     */
    public void setValueType(int valueType)
    {
        mValueType = valueType;
    }

    public String validate(boolean recursive)
    {
        String error = super.validate(recursive);
        if(Common.isEmpty(error)) {
            switch(getValueType()) {
                case INSISWizardConstants.REG_BIN:
                    if(Common.isEmpty(getData()) || (getData().length() % 2) != 0) {
                        error = EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value.error"); //$NON-NLS-1$
                    }
                    break;
                case INSISWizardConstants.REG_DWORD:
                case INSISWizardConstants.REG_EXPAND_SZ:
                    if(Common.isEmpty(getData())) {
                        error = EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value.error"); //$NON-NLS-1$
                    }
            }
        }
        return error;
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((mData == null)?0:mData.hashCode());
        result = PRIME * result + mRootKey;
        result = PRIME * result + ((mSubKey == null)?0:mSubKey.hashCode());
        result = PRIME * result + ((mValue == null)?0:mValue.hashCode());
        result = PRIME * result + mValueType;
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NSISInstallRegistryValue other = (NSISInstallRegistryValue)obj;
        if (mData == null) {
            if (other.mData != null)
                return false;
        }
        else if (!mData.equals(other.mData))
            return false;
        if (mRootKey != other.mRootKey)
            return false;
        if (mSubKey == null) {
            if (other.mSubKey != null)
                return false;
        }
        else if (!mSubKey.equals(other.mSubKey))
            return false;
        if (mValue == null) {
            if (other.mValue != null)
                return false;
        }
        else if (!mValue.equals(other.mValue))
            return false;
        if (mValueType != other.mValueType)
            return false;
        return true;
    }
}
