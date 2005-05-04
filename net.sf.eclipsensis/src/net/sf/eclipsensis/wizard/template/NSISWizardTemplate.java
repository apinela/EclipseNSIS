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

import java.io.Serializable;

import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

public class NSISWizardTemplate implements Serializable, Cloneable
{
	private static final long serialVersionUID = 5904505162934330711L;
    
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CUSTOM = 1;
    public static final int TYPE_USER = 2;
    
    private String mName = null;
    private String mDescription = null;
    private boolean mEnabled = true;
    private boolean mDeleted = false;
    private int mType = TYPE_DEFAULT;
    private NSISWizardSettings mSettings = null;

    NSISWizardTemplate()
    {
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String name)
    {
        this(name,""); //$NON-NLS-1$
    }
    
    /**
     * @param name
     * @param description
     */
    public NSISWizardTemplate(String name, String description)
    {
        this();
        mName = name;
        mDescription = (description==null?"":description); //$NON-NLS-1$
        mType = TYPE_USER;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        NSISWizardTemplate template = (NSISWizardTemplate)super.clone();
        template.mSettings = (mSettings==null?null:(NSISWizardSettings)mSettings.clone());
        return template;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return mDescription;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @return Returns the available.
     */
    public boolean isEnabled()
    {
        return mEnabled;
    }

    /**
     * @param enabled The available to set.
     */
    public void setEnabled(boolean enabled)
    {
        mEnabled = enabled;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getName();
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if(obj instanceof NSISWizardTemplate) {
            NSISWizardTemplate template = (NSISWizardTemplate)obj;
            return mName.equals(template.mName) && mType == template.mType;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return mName.hashCode()+mType;
    }
    
    /**
     * @return Returns the deleted.
     */
    public boolean isDeleted()
    {
        return mDeleted;
    }
    /**
     * @param deleted The deleted to set.
     */
    void setDeleted(boolean deleted)
    {
        mDeleted = deleted;
    }
    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return mType;
    }
    
    /**
     * @param type The type to set.
     */
    public void setType(int type)
    {
        mType = type;
    }
}
