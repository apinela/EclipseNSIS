/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
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
}
