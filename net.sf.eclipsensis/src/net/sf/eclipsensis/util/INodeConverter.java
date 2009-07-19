/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.Map;

import org.w3c.dom.*;

public interface INodeConverter<T>
{
    public Map<String,Class<?>> getNameClassMappings();
    public void addNameClassMapping(String name, Class<?> clasz);
    public Node toNode(Document document, T object);
    public T fromNode(Node node);
}
