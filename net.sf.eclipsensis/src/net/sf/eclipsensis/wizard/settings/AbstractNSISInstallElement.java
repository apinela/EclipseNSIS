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

public abstract class AbstractNSISInstallElement implements INSISInstallElement
{
	private static final long serialVersionUID = 742172003526190746L;

    private NSISWizardSettings mSettings = null;

    public boolean isRemovable()
    {
        return true;
    }

    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }

    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }

    private INSISInstallElement mParent = null;

    public void setParent(INSISInstallElement parent)
    {
        mParent = parent;
    }

    public INSISInstallElement getParent()
    {
        return mParent;
    }
}
