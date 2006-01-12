/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.w3c.dom.*;

public class XMLUtil
{
    private XMLUtil()
    {
    }

    public static void addAttribute(Document document, Node node, String name, String value)
    {
        if(value != null) {
            Attr attribute = document.createAttribute(name);
            attribute.setValue(value);
            node.getAttributes().setNamedItem(attribute);
        }
    }

    public static String getStringValue(NamedNodeMap values, String name)
    {
        return getStringValue(values, name, null);
    }

    public static String getStringValue(NamedNodeMap values, String name, String defaultValue)
    {
        Node node= values.getNamedItem(name);
        return node == null ? defaultValue : node.getNodeValue();
    }

    public static boolean getBooleanValue(NamedNodeMap values, String name)
    {
        return getBooleanValue(values, name, false);
    }

    public static boolean getBooleanValue(NamedNodeMap values, String name, boolean defaultValue)
    {
        Node node= values.getNamedItem(name);
        return node == null ? defaultValue : Boolean.valueOf(node.getNodeValue()).booleanValue();
    }

    public static String readTextNode(Node item)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        NodeList children2 = item.getChildNodes();
        if(children2 != null) {
            for (int k = 0; k < children2.getLength(); k++) {
               Node item2 = children2.item(k);
               if(item2 != null) {
                   buf.append(item2.getNodeValue());
               }
            }
        }
        return buf.toString();
    }

    public static int getIntValue(NamedNodeMap values, String name)
    {
        return getIntValue(values, name, 0);
    }

    public static int getIntValue(NamedNodeMap values, String name, int defaultValue)
    {
        Node node= values.getNamedItem(name);
        try {
            return node == null ? defaultValue : Integer.parseInt(node.getNodeValue());
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return defaultValue;
        }
    }
}
