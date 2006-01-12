/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.util.Collection;

import net.sf.eclipsensis.util.AbstractNodeConvertible;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractNSISInstallElement extends AbstractNodeConvertible implements INSISInstallElement
{
    private static final long serialVersionUID = 742172003526190746L;

    private NSISWizardSettings mSettings = null;
    private INSISInstallElement mParent = null;

    public Object clone() throws CloneNotSupportedException
    {
        AbstractNSISInstallElement element = (AbstractNSISInstallElement) super.clone();
        element.mSettings = null;
        element.mParent = null;
        return element;
    }

    protected void addSkippedProperties(Collection skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("removable"); //$NON-NLS-1$
        skippedProperties.add("editable"); //$NON-NLS-1$
        skippedProperties.add("settings"); //$NON-NLS-1$
        skippedProperties.add("parent"); //$NON-NLS-1$
        skippedProperties.add("childTypes"); //$NON-NLS-1$
        skippedProperties.add("displayName"); //$NON-NLS-1$
        skippedProperties.add("image"); //$NON-NLS-1$
        skippedProperties.add("type"); //$NON-NLS-1$
    }

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

    public void setParent(INSISInstallElement parent)
    {
        mParent = parent;
    }

    public INSISInstallElement getParent()
    {
        return mParent;
    }

    public void fromNode(Node node)
    {
        if(node.getAttributes().getNamedItem(TYPE_ATTRIBUTE).getNodeValue().equals(getType())) {
            super.fromNode(node);
        }
    }

    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        XMLUtil.addAttribute(document,node,TYPE_ATTRIBUTE,getType());
        return node;
    }

    protected String getChildNodeName()
    {
        return CHILD_NODE;
    }

    protected String getNodeName()
    {
        return NODE;
    }

    public final String validate()
    {
        return validate(true);
    }

    public String validate(boolean recursive)
    {
        return null;
    }
}
