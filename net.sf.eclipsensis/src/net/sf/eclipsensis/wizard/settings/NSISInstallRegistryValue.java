/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallRegistryValueDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISInstallRegistryValue extends AbstractNSISInstallItem
{
    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.regvalue.type"); //$NON-NLS-1$
    private static final Image cStrImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.regstr.icon")); //$NON-NLS-1$
    private static final Image cDwordImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.regdword.icon")); //$NON-NLS-1$
    
    private int mRootKey = HKLM;
    private String mSubKey = null;
    private String mData = null;
    private String mValue = null;
    private int mValueType = REG_SZ;

    static {
        NSISInstallElementFactory.register(TYPE, NSISInstallRegistryValue.class);
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
        return new StringBuffer(NSISWizardDisplayValues.HKEY_NAMES[mRootKey]).append(
        "\\").append(mSubKey).append("\\").append(mValue).toString();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(Composite composite)
    {
        return new NSISInstallRegistryValueDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        switch(mValueType)
        {
            case REG_DWORD:
                return cDwordImage;
            default:
                return cStrImage;
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
}