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

public class StringNodeConverter extends AbstractNodeConverter
{
    private static final String STRING_NODE = "string"; //$NON-NLS-1$

    public Object fromNode(Node node, Class clasz)
    {
        if(String.class.equals(clasz) && STRING_NODE.equals(node.getNodeName())) {
            return XMLUtil.readTextNode(node);
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, Object object)
    {
        if(object instanceof String) {
            Node node = document.createElement(STRING_NODE);
            node.appendChild(document.createTextNode((String)object));
            return node;
        }
        else {
            throw new IllegalArgumentException(object.getClass().getName());
        }
    }

}
