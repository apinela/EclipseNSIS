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


public abstract class AbstractNSISInstallItem implements INSISInstallElement
{
	private static final long serialVersionUID = 7704420240201206452L;

    private INSISInstallElement mParent = null;
    private NSISWizardSettings mSettings = null;
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isRemovable()
     */
    public boolean isRemovable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#hasChildren()
     */
    public final boolean hasChildren()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildren()
     */
    public final INSISInstallElement[] getChildren()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildTypes()
     */
    public final String[] getChildTypes()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#addChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final void addChild(INSISInstallElement child)
    {
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final void removeChild(INSISInstallElement child)
    {
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeAllChildren()
     */
    public final void removeAllChildren()
    {
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getParent()
     */
    public INSISInstallElement getParent()
    {
        return mParent;
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#setParent(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public void setParent(INSISInstallElement parent)
    {
        mParent = parent;
    }

    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }
    
    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }
}
