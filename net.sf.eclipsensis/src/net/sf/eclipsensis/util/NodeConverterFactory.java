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

public class NodeConverterFactory
{
    private static Map cNodeConvertersClassMap = new HashMap();

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NodeConverterFactory.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        List list = Common.loadListProperty(bundle,"node.converters"); //$NON-NLS-1$
        for(Iterator iter=list.iterator(); iter.hasNext(); ) {
            String className = (String)iter.next();
            try {
                Object object = Common.createDefaultObject(className);
                if(object instanceof INodeConverter)  {
                    INodeConverter nodeConverter = (INodeConverter)object;
                    Class[] supportedClasses = nodeConverter.getSupportedClasses();
                    for (int i = 0; i < supportedClasses.length; i++) {
                        cNodeConvertersClassMap.put(supportedClasses[i], nodeConverter);
                    }
                }
            }
            catch(Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    private NodeConverterFactory()
    {
    }

    public static INodeConverter getNodeConverter(Class clasz)
    {
        INodeConverter nodeConverter = (INodeConverter)cNodeConvertersClassMap.get(clasz);
        if(nodeConverter == null) {
            Class parent = clasz.getSuperclass();
            while(parent != null) {
                nodeConverter = (INodeConverter)cNodeConvertersClassMap.get(parent);
                if(nodeConverter != null) {
                    return nodeConverter;
                }
                parent = parent.getSuperclass();
            }

            if(nodeConverter == null ) {
                nodeConverter = getNodeConverter2(clasz);
                if(nodeConverter == null) {
                    parent = clasz.getSuperclass();
                    while(parent != null) {
                        nodeConverter = getNodeConverter2(parent);
                        if(nodeConverter != null) {
                            return nodeConverter;
                        }
                        parent = parent.getSuperclass();
                    }
                }
            }
        }
        return nodeConverter;
    }

    private static INodeConverter getNodeConverter2(Class clasz)
    {
        Class[] interfaces = clasz.getInterfaces();
        if(!Common.isEmptyArray(interfaces)) {
            for (int i = 0; i < interfaces.length; i++) {
                INodeConverter nodeConverter = getNodeConverter(interfaces[i]);
                if(nodeConverter != null) {
                    return nodeConverter;
                }
            }
        }

        return null;
    }
}
