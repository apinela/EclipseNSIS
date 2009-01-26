/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractNodeConvertible implements INodeConvertible, Serializable, Cloneable
{
    private static final long serialVersionUID = 7565288054528442187L;
    public static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    public static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

    private static char[] XML_ESCAPE_CHARS = { '\r', '\n', '\t', '>', '<', '&', '"', '\'' };

    public static final String NULL_NODE = "null"; //$NON-NLS-1$

    private transient Collection mSkippedProperties = null;

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public synchronized Collection getSkippedProperties()
    {
        if (mSkippedProperties == null)
        {
            mSkippedProperties = new HashSet();
            addSkippedProperties(mSkippedProperties);
        }
        return mSkippedProperties;
    }

    protected void addSkippedProperties(Collection skippedProperties)
    {
        skippedProperties.add("class"); //$NON-NLS-1$
        skippedProperties.add("skippedProperties"); //$NON-NLS-1$
        skippedProperties.add("nodeName"); //$NON-NLS-1$
        skippedProperties.add("childNodeName"); //$NON-NLS-1$
    }

    public void fromNode(Node node)
    {
        if (node.getNodeName().equals(getNodeName()))
        {
            BeanInfo beanInfo;
            try
            {
                beanInfo = Introspector.getBeanInfo(getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                Map propertyMap = new HashMap();
                for (int i = 0; i < propertyDescriptors.length; i++)
                {
                    propertyMap.put(propertyDescriptors[i].getName(), propertyDescriptors[i]);

                }
                Collection skippedProperties = getSkippedProperties();
                NodeList childNodes = node.getChildNodes();
                int n = childNodes.getLength();
                for (int i = 0; i < n; i++)
                {
                    Node childNode = childNodes.item(i);
                    String nodeName = childNode.getNodeName();
                    if (nodeName.equals(getChildNodeName()))
                    {
                        Node nameNode = childNode.getAttributes().getNamedItem(NAME_ATTRIBUTE);
                        if (nameNode != null)
                        {
                            String propertyName = nameNode.getNodeValue();
                            if (!skippedProperties.contains(propertyName))
                            {
                                PropertyDescriptor propertyDescriptor = (PropertyDescriptor) propertyMap
                                        .get(propertyName);
                                if (propertyDescriptor != null)
                                {
                                    propertyFromNode(childNode, propertyDescriptor);
                                }
                            }
                        }
                    }
                }
            }
            catch (IntrospectionException e)
            {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    protected void propertyFromNode(Node childNode, PropertyDescriptor propertyDescriptor)
    {
        Method writeMethod = propertyDescriptor.getWriteMethod();
        Class[] paramTypes;
        if (writeMethod != null && (paramTypes = writeMethod.getParameterTypes()).length > 0)
        {
            try
            {
                writeMethod.invoke(this, new Object[] { getNodeValue(childNode, propertyDescriptor.getName(),
                        paramTypes[0]) });
            }
            catch (Exception e1)
            {
                EclipseNSISPlugin.getDefault().log(e1);
            }
        }
    }

    /**
     * @param attributes
     * @param paramTypes
     * @return
     */
    protected Object getNodeValue(Node node, String name, Class clasz)
    {
        if (clasz.isArray())
        {
            return NodeConversionUtility.readArrayNode(node, clasz);
        }
        else if (Collection.class.isAssignableFrom(clasz))
        {
            return NodeConversionUtility.readCollectionNode(node, clasz);
        }
        else if (String.class.equals(clasz))
        {
            String str = XMLUtil.readTextNode(node);
            if (!Common.isEmpty(str))
            {
                return str;
            }
        }
        INodeConverter nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(clasz);
        if (nodeConverter != null)
        {
            Node childNode = XMLUtil.findFirstChild(node);
            if (childNode != null)
            {
                return nodeConverter.fromNode(childNode);
            }
        }
        Node attr = node.getAttributes().getNamedItem(VALUE_ATTRIBUTE);
        if (attr != null)
        {
            return convertFromString(attr.getNodeValue(), clasz);
        }
        return null;
    }

    public Node toNode(Document document)
    {
        Node node = document.createElement(getNodeName());
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            Collection skippedProperties = getSkippedProperties();
            for (int i = 0; i < propertyDescriptors.length; i++)
            {
                try
                {
                    PropertyDescriptor descriptor = propertyDescriptors[i];
                    String name = descriptor.getName();
                    if (!skippedProperties.contains(name))
                    {
                        Node childNode = propertyToNode(document, descriptor);
                        if (childNode != null)
                        {
                            node.appendChild(childNode);
                        }
                    }
                }
                catch (Exception ex)
                {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        }
        catch (IntrospectionException e)
        {
            EclipseNSISPlugin.getDefault().log(e);
        }
        return node;
    }

    protected Node propertyToNode(Document document, PropertyDescriptor descriptor) throws IllegalAccessException,
            InvocationTargetException
    {
        Method readMethod = descriptor.getReadMethod();
        if (readMethod != null)
        {
            Object obj = readMethod.invoke(this, null);

            if (obj != null)
            {
                return createChildNode(document, descriptor.getName(), obj);
            }
        }
        return null;
    }

    protected String convertToString(String name, Object obj)
    {
        if (obj instanceof RGB)
        {
            return StringConverter.asString((RGB) obj);
        }
        return obj == null ? null : obj.toString();
    }

    protected Object convertFromString(String string, Class clasz)
    {
        if (clasz.equals(String.class))
        {
            return string;
        }
        else if (clasz.equals(Long.class) || clasz.equals(long.class))
        {
            return Long.valueOf(string);
        }
        else if (clasz.equals(Integer.class) || clasz.equals(int.class))
        {
            return Integer.valueOf(string);
        }
        else if (clasz.equals(Short.class) || clasz.equals(short.class))
        {
            return Short.valueOf(string);
        }
        else if (clasz.equals(Float.class) || clasz.equals(float.class))
        {
            return Float.valueOf(string);
        }
        else if (clasz.equals(Double.class) || clasz.equals(double.class))
        {
            return Double.valueOf(string);
        }
        else if (clasz.equals(Byte.class) || clasz.equals(byte.class))
        {
            return Byte.valueOf(string);
        }
        else if (clasz.equals(Boolean.class) || clasz.equals(boolean.class))
        {
            return Boolean.valueOf(string);
        }
        else if (clasz.equals(Character.class) || clasz.equals(char.class))
        {
            return new Character(string.charAt(0));
        }
        else if (clasz.equals(RGB.class))
        {
            return StringConverter.asRGB(string);
        }
        else
        {
            return null;
        }
    }

    protected Node createChildNode(Document document, String name, Object value)
    {
        Node childNode = document.createElement(getChildNodeName());
        XMLUtil.addAttribute(document, childNode, NAME_ATTRIBUTE, name);
        if (value.getClass().isArray())
        {
            NodeConversionUtility.createArrayNode(document, childNode, value);
        }
        else if (value instanceof Collection)
        {
            NodeConversionUtility.createCollectionNode(document, childNode, (Collection) value);
        }
        else if (value instanceof INodeConvertible)
        {
            childNode.appendChild(((INodeConvertible) value).toNode(document));
        }
        else if (value instanceof Node)
        {
            childNode.appendChild((Node) value);
        }
        else if (value instanceof NodeList)
        {
            NodeList nodeList = (NodeList) value;
            int n = nodeList.getLength();
            for (int i = 0; i < n; i++)
            {
                childNode.appendChild(nodeList.item(i));
            }
        }
        else if (value != null)
        {
            INodeConverter nodeConverter = null;
            if (value instanceof String)
            {
                String str = (String) value;
                for (int i = 0; i < XML_ESCAPE_CHARS.length; i++)
                {
                    if (str.indexOf(XML_ESCAPE_CHARS[i]) >= 0)
                    {
                        childNode.appendChild(document.createTextNode(str));
                        return childNode;
                    }
                }
            }
            else if (!Common.isWrappedPrimitive(value))
            {
                nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(value.getClass());
            }
            if (nodeConverter != null)
            {
                childNode.appendChild(nodeConverter.toNode(document, value));
            }
            else
            {
                XMLUtil.addAttribute(document, childNode, VALUE_ATTRIBUTE, convertToString(name, value));
            }
        }
        return childNode;
    }

    protected abstract String getChildNodeName();
}
