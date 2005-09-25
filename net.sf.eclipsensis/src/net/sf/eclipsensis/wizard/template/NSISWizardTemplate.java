/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

public class NSISWizardTemplate extends AbstractTemplate
{
    private static final long serialVersionUID = 5904505162934330711L;

    private NSISWizardSettings mSettings = null;
    
    //The following four are dummy declarations for handling serialization issues.
    //XXX Remove in 0.9.4
    private String mName = null;
    private String mDescription = null;
    private boolean mEnabled = true;
    private boolean mDeleted = false;
    private int mType = TYPE_DEFAULT;

    //The following method is a hack for handling serialization issues.
    //XXX Remove in 0.9.4
    boolean syncUp()
    {
        if(mName != null) {
            super.setName(mName);
            mName = null;
            super.setDescription(mDescription);
            mDescription = null;
            super.setEnabled(mEnabled);
            mEnabled = true;
            super.setDeleted(mDeleted);
            mDeleted = false;
            super.setType(mType);
            mType = 0;
            return true;
        }
        return false;
    }

    NSISWizardTemplate()
    {
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String name)
    {
        super(name); //$NON-NLS-1$
    }
    
    /**
     * @param name
     * @param description
     */
    public NSISWizardTemplate(String name, String description)
    {
        super(name, description);
    }
    
    public Object clone()
    {
        NSISWizardTemplate template = (NSISWizardTemplate)super.clone();
        try {
            template.mSettings = (mSettings==null?null:(NSISWizardSettings)mSettings.clone());
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            template.mSettings = new NSISWizardSettings();
        }
        return template;
    }

    /**
     * @return Returns the settings.
     */
    public NSISWizardSettings getSettings()
    {
        if(mSettings == null) {
            synchronized(this) {
                if(mSettings == null) {
                    mSettings = new NSISWizardSettings();
                }
            }
        }
        return mSettings;
    }
    
    /**
     * @param settings The settings to set.
     */
    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }
    
}
