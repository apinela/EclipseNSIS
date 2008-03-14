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

import org.w3c.dom.*;

public class PrimitivesNodeConverter implements INodeConverter
{
    private static final String CHARACTER_NODE = "character"; //$NON-NLS-1$

    private static final String BOOLEAN_NODE = "boolean"; //$NON-NLS-1$

    private static final String FLOAT_NODE = "float"; //$NON-NLS-1$

    private static final String DOUBLE_NODE = "double"; //$NON-NLS-1$

    private static final String BYTE_NODE = "byte"; //$NON-NLS-1$

    private static final String SHORT_NODE = "short"; //$NON-NLS-1$

    private static final String LONG_NODE = "long"; //$NON-NLS-1$

    private static final String INTEGER_NODE = "integer"; //$NON-NLS-1$

    private static final String VALUE_ATTR = "value"; //$NON-NLS-1$

    private static Map cClassNameMap = new HashMap();

    static {
        cClassNameMap.put(Integer.class, INTEGER_NODE);
        cClassNameMap.put(Integer.TYPE, INTEGER_NODE);
        cClassNameMap.put(Long.class, LONG_NODE);
        cClassNameMap.put(Long.TYPE, LONG_NODE);
        cClassNameMap.put(Short.class, SHORT_NODE);
        cClassNameMap.put(Short.TYPE, SHORT_NODE);
        cClassNameMap.put(Byte.class, BYTE_NODE);
        cClassNameMap.put(Byte.TYPE, BYTE_NODE);
        cClassNameMap.put(Double.class, DOUBLE_NODE);
        cClassNameMap.put(Double.TYPE, DOUBLE_NODE);
        cClassNameMap.put(Float.class, FLOAT_NODE);
        cClassNameMap.put(Float.TYPE, FLOAT_NODE);
        cClassNameMap.put(Boolean.class, BOOLEAN_NODE);
        cClassNameMap.put(Boolean.TYPE, BOOLEAN_NODE);
        cClassNameMap.put(Character.class, CHARACTER_NODE);
        cClassNameMap.put(Character.TYPE, CHARACTER_NODE);
    }

    public Object fromNode(Node node, Class clasz)
    {
        String nodeName = node.getNodeName();
        NamedNodeMap attr = node.getAttributes();
        if(nodeName.equals(INTEGER_NODE) && (Integer.class.equals(clasz) || Integer.TYPE.equals(clasz))) {
            return Integer.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(LONG_NODE) && (Long.class.equals(clasz) || Long.TYPE.equals(clasz))) {
            return Long.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(SHORT_NODE) && (Short.class.equals(clasz) || Short.TYPE.equals(clasz))) {
            return Short.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(BYTE_NODE) && (Byte.class.equals(clasz) || Byte.TYPE.equals(clasz))) {
            return Byte.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(DOUBLE_NODE) && (Double.class.equals(clasz) || Double.TYPE.equals(clasz))) {
            return Double.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(FLOAT_NODE) && (Float.class.equals(clasz) || Float.TYPE.equals(clasz))) {
            return Float.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(BOOLEAN_NODE) && (Boolean.class.equals(clasz) || Boolean.TYPE.equals(clasz))) {
            return Boolean.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(nodeName.equals(CHARACTER_NODE) && (Character.class.equals(clasz) || Character.TYPE.equals(clasz))) {
            return new Character(XMLUtil.getStringValue(attr, VALUE_ATTR).charAt(0));
        }
        throw new IllegalArgumentException(nodeName);
    }

    public Class[] getSupportedClasses()
    {
        return (Class[])cClassNameMap.keySet().toArray(new Class[cClassNameMap.size()]);
    }

    public Node toNode(Document document, Object object)
    {
        String nodeName = (String)cClassNameMap.get(object.getClass());
        if(nodeName != null) {
            Node node = document.createElement(nodeName);
            XMLUtil.addAttribute(document, node, VALUE_ATTR, String.valueOf(object));
            return node;
        }
        throw new IllegalArgumentException(object.getClass().getName());
    }
}
