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

import java.beans.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.w3c.dom.*;

public abstract class AbstractNodeConvertible implements INodeConvertible, Serializable, Cloneable
{
	private static final long serialVersionUID = 7565288054528442187L;
    public static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    public static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

    private transient Collection mSkippedProperties = null;

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public synchronized Collection getSkippedProperties()
    {
        if(mSkippedProperties == null) {
            mSkippedProperties = new HashSet();
            addSkippedProperties(mSkippedProperties);
        }
        return mSkippedProperties;
    }

    protected void addSkippedProperties(Collection skippedProperties)
    {
        skippedProperties.add("class"); //$NON-NLS-1$
        skippedProperties.add("skippedProperties"); //$NON-NLS-1$
    }

    public void fromNode(Node node)
    {
        if(node.getNodeName().equals(getNodeName())) {
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                Map propertyMap = new HashMap();
                for (int i = 0; i < propertyDescriptors.length; i++) {
                    propertyMap.put(propertyDescriptors[i].getName(),propertyDescriptors[i]);

                }
                NodeList childNodes = node.getChildNodes();
                int n = childNodes.getLength();
                for(int i=0; i<n; i++) {
                    Node childNode = childNodes.item(i);
                    String nodeName = childNode.getNodeName();
                    if(nodeName.equals(getChildNodeName())) {
                        Node nameNode = childNode.getAttributes().getNamedItem(NAME_ATTRIBUTE);
                        Collection skippedProperties = getSkippedProperties();
                        String propertyName = nameNode.getNodeValue();
                        if(nameNode != null && !skippedProperties.contains(propertyName)) {
                            PropertyDescriptor propertyDescriptor = (PropertyDescriptor)propertyMap.get(propertyName);
                            if(propertyDescriptor != null) {
                                Method writeMethod = propertyDescriptor.getWriteMethod();
                                Class[] paramTypes;
                                if(writeMethod != null && (paramTypes = writeMethod.getParameterTypes()).length > 0) {
                                    try {
                                        writeMethod.invoke(this,new Object[]{getNodeValue(childNode, propertyName, paramTypes[0])});
                                    }
                                    catch (Exception e1) {
                                        EclipseNSISPlugin.getDefault().log(e1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (IntrospectionException e) {
                EclipseNSISPlugin.getDefault().log(e);
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
        return convertFromString(name,node.getAttributes().getNamedItem(VALUE_ATTRIBUTE).getNodeValue(), clasz);
    }

    public Node toNode(Document document)
    {
        Node node = document.createElement(getNodeName());
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            Collection skippedProperties = getSkippedProperties();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                try {
                    PropertyDescriptor descriptor = propertyDescriptors[i];
                    String name = descriptor.getName();
                    if(!skippedProperties.contains(name)) {
                        Method readMethod = descriptor.getReadMethod();
                        if(readMethod != null) {
                            Object obj = readMethod.invoke(this,null);

                            if(obj != null) {
                                node.appendChild(createChildNode(document, name, obj));
                            }
                        }
                    }
                }
                catch(Exception ex) {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        }
        catch (IntrospectionException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        return node;
    }

    protected String convertToString(String name, Object obj)
    {
        return (obj==null?null:obj.toString());
    }

    protected Object convertFromString(String name, String string, Class clasz)
    {
        if(clasz.equals(String.class)) {
            return string;
        }
        else if(clasz.equals(Long.class) || clasz.equals(long.class)) {
            return Long.valueOf(string);
        }
        else if(clasz.equals(Integer.class) || clasz.equals(int.class)) {
            return Integer.valueOf(string);
        }
        else if(clasz.equals(Short.class) || clasz.equals(short.class)) {
            return Short.valueOf(string);
        }
        else if(clasz.equals(Float.class) || clasz.equals(float.class)) {
            return Float.valueOf(string);
        }
        else if(clasz.equals(Double.class) || clasz.equals(double.class)) {
            return Double.valueOf(string);
        }
        else if(clasz.equals(Byte.class) || clasz.equals(byte.class)) {
            return Byte.valueOf(string);
        }
        else if(clasz.equals(Boolean.class) || clasz.equals(boolean.class)) {
            return Boolean.valueOf(string);
        }
        else if(clasz.equals(Character.class) || clasz.equals(char.class)) {
            return new Character(string.charAt(0));
        }
        else {
            return null;
        }
    }

    protected Node createChildNode(Document document, String name, Object value)
    {
        Node childNode = document.createElement(getChildNodeName());
        XMLUtil.addAttribute(document, childNode, NAME_ATTRIBUTE, name);
        if(value instanceof INodeConvertible) {
            childNode.appendChild(((INodeConvertible)value).toNode(document));
        }
        else if(value instanceof Node) {
            childNode.appendChild((Node)value);
        }
        else if(value instanceof NodeList) {
            NodeList nodeList = (NodeList)value;
            int n = nodeList.getLength();
            for(int i=0; i<n; i++) {
                childNode.appendChild(nodeList.item(i));
            }
        }
        else {
            XMLUtil.addAttribute(document, childNode, VALUE_ATTRIBUTE, convertToString(name, value));
        }
        return childNode;
    }

    protected abstract String getChildNodeName();
    protected abstract String getNodeName();
}
