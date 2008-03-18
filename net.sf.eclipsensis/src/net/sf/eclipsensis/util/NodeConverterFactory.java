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

import net.sf.eclipsensis.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.*;

public class NodeConverterFactory implements IExtensionChangeHandler
{
    private static final String EXTENSION_POINT = "nodeConverters"; //$NON-NLS-1$
    private static final String NODE_CONVERTER = "nodeConverter"; //$NON-NLS-1$
    private static final String NODE_CONVERTER_CLASS = "class"; //$NON-NLS-1$
    private static final String NAME_CLASS_MAPPING = "nameClassMapping"; //$NON-NLS-1$
    private static final String NAME_CLASS_MAPPING_NAME = "name"; //$NON-NLS-1$
    private static final String NAME_CLASS_MAPPING_CLASS = "class"; //$NON-NLS-1$

    public static final NodeConverterFactory INSTANCE = new NodeConverterFactory();

    private Map mExtensions = new HashMap();
    private Map mNameNodeConverterMap = new HashMap();
    private Map mClassNodeConverterMap = new HashMap();

    private Object mLock = new Object();

    private NodeConverterFactory()
    {
        final IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        loadExtensions(tracker);
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
        final BundleContext bundleContext = EclipseNSISPlugin.getDefault().getBundleContext();
        bundleContext.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event)
            {
                if(event.getType() == BundleEvent.STOPPED ) {
                    bundleContext.removeBundleListener(this);
                }
                tracker.unregisterHandler(NodeConverterFactory.this);
            }
        });
    }

    public INodeConverter getNodeConverter(String name)
    {
        synchronized (mLock) {
            return (INodeConverter)mNameNodeConverterMap.get(name);
        }
    }

    public INodeConverter getNodeConverter(Class clasz)
    {
        synchronized (mLock) {
            INodeConverter nodeConverter = (INodeConverter)mClassNodeConverterMap.get(clasz);
            if(nodeConverter == null) {
                Class parent = clasz.getSuperclass();
                while(parent != null) {
                    nodeConverter = (INodeConverter)mClassNodeConverterMap.get(parent);
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
    }

    private INodeConverter getNodeConverter2(Class clasz)
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


    private IExtensionPoint getExtensionPointFilter()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(INSISConstants.PLUGIN_ID,EXTENSION_POINT);
    }

    private void loadExtensions(IExtensionTracker tracker)
    {
        IExtensionPoint point = getExtensionPointFilter();
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                addExtension(tracker, extensions[i]);
            }
        }
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        synchronized (mLock) {
            if (!mExtensions.containsKey(extension.getUniqueIdentifier())) {
                IConfigurationElement[] elements = extension.getConfigurationElements();
                List nodeConverters = new ArrayList();
                for (int i = 0; i < elements.length; i++) {
                    if (NODE_CONVERTER.equals(elements[i].getName())) {
                        try {
                            String className = elements[i].getAttribute(NODE_CONVERTER_CLASS);
                            Class clasz = Class.forName(className);
                            if(INodeConverter.class.isAssignableFrom(clasz)) {
                                INodeConverter nodeConverter = (INodeConverter)clasz.getConstructor(null).newInstance(null);

                                IConfigurationElement[] elements2 = elements[i].getChildren();
                                for (int j = 0; j < elements2.length; j++) {
                                    if(NAME_CLASS_MAPPING.equals(elements2[j].getName())) {
                                        String name = elements2[j].getAttribute(NAME_CLASS_MAPPING_NAME);
                                        className = elements2[j].getAttribute(NAME_CLASS_MAPPING_CLASS);
                                        clasz = Class.forName(className);
                                        nodeConverter.addNameClassMapping(name, clasz);
                                    }
                                }
                                for (Iterator iterator = nodeConverter.getNameClassMappings().entrySet().iterator(); iterator.hasNext();) {
                                    Map.Entry entry = (Map.Entry)iterator.next();
                                    mNameNodeConverterMap.put(entry.getKey(),nodeConverter);
                                    mClassNodeConverterMap.put(entry.getValue(),nodeConverter);
                                }
                                tracker.registerObject(extension, nodeConverter, IExtensionTracker.REF_WEAK);
                                nodeConverters.add(nodeConverter);
                            }
                        }
                        catch (Exception e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
                mExtensions.put(extension.getUniqueIdentifier(), nodeConverters);
            }
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        synchronized (mLock) {
            if (mExtensions.containsKey(extension.getUniqueIdentifier())) {
                List nodeConverters = (List)mExtensions.remove(extension.getUniqueIdentifier());
                for (Iterator iterator = nodeConverters.iterator(); iterator.hasNext();) {
                    INodeConverter nodeConverter = (INodeConverter)iterator.next();
                    Map nameClassMapping = nodeConverter.getNameClassMappings();
                    mNameNodeConverterMap.keySet().removeAll(nameClassMapping.keySet());
                    mClassNodeConverterMap.keySet().removeAll(nameClassMapping.values());
                }
            }
        }
    }
}
