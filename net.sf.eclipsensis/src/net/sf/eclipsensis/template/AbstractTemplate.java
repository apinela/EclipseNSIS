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
import java.util.Collection;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public abstract class AbstractTemplate extends AbstractNodeConvertible implements Serializable, Cloneable
{
    private static final long serialVersionUID = -6593538372175606301L;

    public static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    protected static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
    protected static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    protected static final String DESCRIPTION_NODE= "description"; //$NON-NLS-1$

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
        this(null, null);
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
        mId = id;
        mName = name;
        mDescription = (description==null?"":description); //$NON-NLS-1$
        mType = TYPE_USER;
    }

    public void fromNode(Node node)
    {
        super.fromNode(node);
        NamedNodeMap attributes= node.getAttributes();

        if (attributes != null) {
            String id= XMLUtil.getStringValue(attributes, ID_ATTRIBUTE);
            setId(id);

            String name= XMLUtil.getStringValue(attributes, NAME_ATTRIBUTE);
            if (name == null) {
                throw new InvalidTemplateException(EclipseNSISPlugin.getFormattedString("template.error.missing.attribute", //$NON-NLS-1$
                        new Object[]{NAME_ATTRIBUTE}));
            }
            setName(name);
        }
        Node[] descriptionNode = XMLUtil.findChildren(node,DESCRIPTION_NODE);
        if(!Common.isEmptyArray(descriptionNode)) {
            setDescription(XMLUtil.readTextNode(descriptionNode[0]));
        }

        setDeleted(false);
        setEnabled(true);
    }

    protected String getChildNodeName()
    {
        return "attribute"; //$NON-NLS-1$
    }

    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        if (!Common.isEmpty(getId())) {
            XMLUtil.addAttribute(document, node, ID_ATTRIBUTE, getId());
        }
        XMLUtil.addAttribute(document, node, NAME_ATTRIBUTE, getName());
        Element description = document.createElement(DESCRIPTION_NODE);
        Text data= document.createTextNode(getDescription());
        description.appendChild(data);
        node.appendChild(description);
        return node;
    }

    protected void addSkippedProperties(Collection skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("id"); //$NON-NLS-1$
        skippedProperties.add("name"); //$NON-NLS-1$
        skippedProperties.add("type"); //$NON-NLS-1$
        skippedProperties.add("available"); //$NON-NLS-1$
        skippedProperties.add("enabled"); //$NON-NLS-1$
        skippedProperties.add("deleted"); //$NON-NLS-1$
        skippedProperties.add("description"); //$NON-NLS-1$
        skippedProperties.add("settings"); //$NON-NLS-1$
    }

    public final String getNodeName()
    {
        return TEMPLATE_ELEMENT;
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

    public boolean isAvailable()
    {
        return true;
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

    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
