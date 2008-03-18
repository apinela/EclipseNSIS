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

import java.util.*;

import org.w3c.dom.Node;

public abstract class AbstractNodeConverter implements INodeConverter
{
    private Map mNameClassMap = new HashMap();

    public Map getNameClassMappings()
    {
        return Collections.unmodifiableMap(mNameClassMap);
    }

    public void addNameClassMapping(String name, Class clasz)
    {
        mNameClassMap.put(name, clasz);
    }

    public Object fromNode(Node node)
    {
        Class clasz = (Class)mNameClassMap.get(node.getNodeName());
        if(clasz != null) {
            return fromNode(node, clasz);
        }
        throw new IllegalArgumentException(node.getNodeName());
    }

    public abstract Object fromNode(Node node, Class clasz);
}
