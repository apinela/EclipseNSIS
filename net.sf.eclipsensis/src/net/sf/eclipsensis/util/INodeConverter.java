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

public interface INodeConverter
{
    public Class[] getSupportedClasses();
    public Node toNode(Document document, Object object);
    public Object fromNode(Node node, Class clasz);
}
