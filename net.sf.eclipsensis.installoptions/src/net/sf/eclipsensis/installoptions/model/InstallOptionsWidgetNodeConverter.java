/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.util.AbstractNodeConverter;

import org.w3c.dom.*;

public class InstallOptionsWidgetNodeConverter extends AbstractNodeConverter<InstallOptionsWidget>
{
    @Override
	public InstallOptionsWidget fromNode(Node node, Class<?> clasz)
    {
        if(InstallOptionsWidget.class.isAssignableFrom(clasz)) {
            if(node.getNodeName().equals(InstallOptionsWidget.NODE_NAME)) {
                return (InstallOptionsWidget) InstallOptionsElementFactory.createFromNode(node);
            }
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, InstallOptionsWidget object)
    {
        if(object != null) {
            return object.toNode(document);
        }
        throw new IllegalArgumentException(String.valueOf(object));
    }
}
