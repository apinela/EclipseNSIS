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

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.w3c.dom.Node;

public class NodeClassMap
{
    private static Map cNodeClassMap = new HashMap();

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NodeClassMap.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        Map map = Common.loadMapProperty(bundle,"node.class.map"); //$NON-NLS-1$
        for(Iterator iter=map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iter.next();
            String nodeName = (String)entry.getKey();
            String className = (String)entry.getValue();
            try {
                Class clasz = Class.forName(className);
                cNodeClassMap.put(nodeName, clasz);
            }
            catch(Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    private NodeClassMap()
    {
    }

    public static Class getClassForNode(Node node)
    {
        return (Class)cNodeClassMap.get(node.getNodeName());
    }
}
