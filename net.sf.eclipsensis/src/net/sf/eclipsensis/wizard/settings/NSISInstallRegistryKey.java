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
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallRegistryKeyDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISInstallRegistryKey extends AbstractNSISInstallItem
{
	private static final long serialVersionUID = 1525071202238497310L;

    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.regkey.type"); //$NON-NLS-1$
    private static final Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.regkey.icon")); //$NON-NLS-1$
    
    private int mRootKey = HKLM;
    private String mSubKey = null;

    static {
        NSISInstallElementFactory.register(TYPE, NSISInstallRegistryKey.class);
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
                "\\").append(mSubKey).toString(); //$NON-NLS-1$
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
        return new NSISInstallRegistryKeyDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
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
     * @return Returns the subKey.
     */
    public String getSubKey()
    {
        return mSubKey;
    }

    /**
     * @param name The subKey to set.
     */
    public void setSubKey(String subKey)
    {
        mSubKey = subKey;
    }
}
