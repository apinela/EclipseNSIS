/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.Serializable;

public abstract class AbstractTemplate implements Serializable, Cloneable
{
    private static final long serialVersionUID = -6593538372175606301L;

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CUSTOM = 1;
    public static final int TYPE_USER = 2;

    private String mId = null;
    private String mName = null;
    private String mDescription = null;
    private boolean mEnabled = true;
    private boolean mDeleted = false;
    private int mType = TYPE_DEFAULT;

    protected AbstractTemplate()
    {
    }

    /**
     * @param name
     */
    public AbstractTemplate(String id, String name)
    {
        this(id, name,""); //$NON-NLS-1$
    }

    /**
     * @param name
     * @param description
     */
    public AbstractTemplate(String id, String name, String description)
    {
        this();
        mId = id;
        mName = name;
        mDescription = (description==null?"":description); //$NON-NLS-1$
        mType = TYPE_USER;
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
     * @return Returns the deleted.
     */
    public boolean isDeleted()
    {
        return mDeleted;
    }
    /**
     * @param deleted The deleted to set.
     */
    protected void setDeleted(boolean deleted)
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

    public String getId()
    {
        return mId;
    }

    public void setId(String id)
    {
        mId = id;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int type)
    {
        mType = type;
    }

    protected Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    protected void beforeExport()
    {
    }

    protected void afterImport() throws InvalidTemplateException
    {
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (mDeleted?1231:1237);
        result = PRIME * result + ((mDescription == null)?0:mDescription.hashCode());
        result = PRIME * result + (mEnabled?1231:1237);
        result = PRIME * result + ((mId == null)?0:mId.hashCode());
        result = PRIME * result + ((mName == null)?0:mName.hashCode());
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
        final AbstractTemplate other = (AbstractTemplate)obj;
        if (mDeleted != other.mDeleted)
            return false;
        if (mDescription == null) {
            if (other.mDescription != null)
                return false;
        }
        else if (!mDescription.equals(other.mDescription))
            return false;
        if (mEnabled != other.mEnabled)
            return false;
        if (mId == null) {
            if (other.mId != null)
                return false;
        }
        else if (!mId.equals(other.mId))
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        }
        else if (!mName.equals(other.mName))
            return false;
        return true;
    }
}
