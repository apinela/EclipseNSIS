/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NodeConversionUtility
{
    private NodeConversionUtility()
    {
    }

    public static final Object readArrayNode(Node node, Class clasz)
    {
        if (clasz.isArray())
        {
            Class clasz2 = clasz.getComponentType();
            INodeConverter nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(clasz2);
            Node[] children = XMLUtil.findChildren(node);
            Object array = Array.newInstance(clasz2, children.length);
            for (int i = 0; i < children.length; i++)
            {
                Array.set(array, i, readComponentNode(children[i], nodeConverter, clasz2));
            }
            return array;
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    private static Object readComponentNode(Node node, INodeConverter nodeConverter, Class clasz)
    {
        if (!AbstractNodeConvertible.NULL_NODE.equals(node.getNodeName()))
        {
            if (nodeConverter != null)
            {
                return nodeConverter.fromNode(node);
            }
            else
            {
                INodeConverter nodeConverter2 = NodeConverterFactory.INSTANCE.getNodeConverter(node.getNodeName());
                if (nodeConverter2 != null)
                {
                    return nodeConverter2.fromNode(node);
                }
                throw new IllegalArgumentException(node.getNodeName());
            }
        }
        return null;
    }

    public static final void createArrayNode(Document document, Node parent, Object value)
    {
        if (value.getClass().isArray())
        {
            Class clasz = value.getClass().getComponentType();
            INodeConverter nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(clasz);
            if (!Common.isEmptyArray(value))
            {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++)
                {
                    Object obj = Array.get(value, i);
                    createComponentNode(document, parent, nodeConverter, obj);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException(value.getClass().getName());
        }
    }

    public static final Collection readCollectionNode(Node node, Class clasz)
    {
        Collection collection = null;
        if (!Modifier.isAbstract(clasz.getModifiers()))
        {
            collection = (Collection) Common.createDefaultObject(clasz);
        }
        else if (List.class.equals(clasz))
        {
            collection = new ArrayList();
        }
        else if (Set.class.equals(clasz))
        {
            collection = new HashSet();
        }
        if (collection != null)
        {
            Node[] children = XMLUtil.findChildren(node);
            for (int i = 0; i < children.length; i++)
            {
                collection.add(readComponentNode(children[i], null, Object.class));
            }
        }
        return collection;
    }

    public static final void createCollectionNode(Document document, Node parent, Collection collection)
    {
        if (!Common.isEmptyCollection(collection))
        {
            for (Iterator iterator = collection.iterator(); iterator.hasNext();)
            {
                createComponentNode(document, parent, null, iterator.next());
            }
        }
    }

    private static void createComponentNode(Document document, Node parent, INodeConverter nodeConverter, Object obj)
    {
        if (obj != null)
        {
            if (nodeConverter != null)
            {
                parent.appendChild(nodeConverter.toNode(document, obj));
            }
            else
            {
                INodeConverter nodeConverter2 = NodeConverterFactory.INSTANCE.getNodeConverter(obj.getClass());
                if (nodeConverter2 != null)
                {
                    parent.appendChild(nodeConverter2.toNode(document, obj));
                }
                else
                {
                    throw new IllegalArgumentException(obj.getClass().getName());
                }
            }
        }
        else
        {
            parent.appendChild(document.createElement(AbstractNodeConvertible.NULL_NODE));
        }
    }
}
