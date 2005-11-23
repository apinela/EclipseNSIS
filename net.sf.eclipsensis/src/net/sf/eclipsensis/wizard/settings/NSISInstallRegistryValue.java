/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallRegistryValueDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallRegistryValue extends AbstractNSISInstallItem
{
	private static final long serialVersionUID = 4012648943855296196L;

    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.regvalue.type"); //$NON-NLS-1$
    private static final Image STR_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.regstr.icon")); //$NON-NLS-1$
    private static final Image DWORD_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.regdword.icon")); //$NON-NLS-1$

    private int mRootKey = HKLM;
    private String mSubKey = null;
    private String mData = null;
    private String mValue = null;
    private int mValueType = REG_SZ;

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.regvalue.type.name"), STR_IMAGE, NSISInstallRegistryValue.class); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        String[] hkeyNames = NSISWizardDisplayValues.getHKEYNames();
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(mRootKey >= 0 && mRootKey < hkeyNames.length) {
            buf.append(hkeyNames[mRootKey]);
        }
        return buf.append("\\").append(mSubKey).append("\\").append(mValue).toString(); //$NON-NLS-1$ //$NON-NLS-2$
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
            case REG_DWORD:
                return DWORD_IMAGE;
            default:
                return STR_IMAGE;
        }
    }

    /**
     * @return Returns the rootKey.
     */
    public int getRootKey()
    {
        return mRootKey;
    }

    /**
     * @param rootKey The rootKey to set.
     */
    public void setRootKey(int rootKey)
    {
        mRootKey = rootKey;
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
     * @return Returns the subKey.
     */
    public String getSubKey()
    {
        return mSubKey;
    }

    /**
     * @param subKey The subKey to set.
     */
    public void setSubKey(String subKey)
    {
        mSubKey = subKey;
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
        String[] hkeyNames = NSISWizardDisplayValues.getHKEYNames();
        if(mRootKey < 0 || mRootKey >= hkeyNames.length) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.root.key.error"); //$NON-NLS-1$
        }
        else {
            String subKey = Common.trim(getSubKey());
            if(Common.isEmpty(subKey) || subKey.endsWith("\\") || subKey.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.sub.key.error"); //$NON-NLS-1$
            }
            else if(getValueType() != INSISWizardConstants.REG_SZ && Common.isEmpty(getData())) {
                return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value.error"); //$NON-NLS-1$
            }
            else {
                return super.validate(recursive);
            }
        }
    }
}
