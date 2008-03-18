/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.util.AbstractNodeConverter;

import org.w3c.dom.*;

public class NSISInstallElementNodeConverter extends AbstractNodeConverter
{
    public Object fromNode(Node node, Class clasz)
    {
        if(INSISInstallElement.class.isAssignableFrom(clasz)) {
            if(node.getNodeName().equals(INSISInstallElement.NODE)) {
                return NSISInstallElementFactory.createFromNode(node);
            }
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, Object object)
    {
        if(object instanceof INSISInstallElement) {
            return ((INSISInstallElement)object).toNode(document);
        }
        throw new IllegalArgumentException(String.valueOf(object));
    }
}
