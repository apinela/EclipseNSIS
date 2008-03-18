/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import org.w3c.dom.*;

public class NodeConvertibleNodeConverter extends AbstractNodeConverter
{
    public Object fromNode(Node node, Class clasz)
    {
        Object obj = Common.createDefaultObject(clasz);
        if(obj instanceof INodeConvertible) {
            INodeConvertible nodeConvertible = (INodeConvertible)obj;
            if(Common.stringsAreEqual(node.getNodeName(), nodeConvertible.getNodeName())) {
                nodeConvertible.fromNode(node);
                return nodeConvertible;
            }
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, Object object)
    {
        if(object instanceof INodeConvertible) {
            return ((INodeConvertible)object).toNode(document);
        }
        throw new IllegalArgumentException(String.valueOf(object));
    }
}
