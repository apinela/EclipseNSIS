/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Node;

public class NSISInstallElementFactory
{
    private static Map cElementMap = new HashMap();
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    static {
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        if(plugin != null) {
            String[] classList = Common.loadArrayProperty(plugin.getResourceBundle(),"preload.nsisinstallelements"); //$NON-NLS-1$
            for (int i = 0; i < classList.length; i++) {
                try {
                    Class.forName(classList[i]);
                }
                catch(Exception e)
                {
                }
            }
        }
    }

    private NSISInstallElementFactory()
    {
    }

    public static void register(String type, String typeName, Image image, Class clasz)
    {
        if(!cElementMap.containsKey(type)) {
            try {
                NSISInstallElementDescriptor descriptor = new NSISInstallElementDescriptor(clasz, typeName, image);
                cElementMap.put(type, descriptor);
            }
            catch(Exception ex) {
            }
        }
    }

    public static void unregister(String type, Class clasz)
    {
        if(!cElementMap.containsKey(type) && ((NSISInstallElementDescriptor)cElementMap.get(type)).getElementClass().equals(clasz)) {
            cElementMap.remove(type);
        }
    }

    public static INSISInstallElement create(String type)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            try {
                return (INSISInstallElement)descriptor.getConstructor().newInstance(EMPTY_OBJECT_ARRAY);
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    public static INSISInstallElement createFromNode(Node node)
    {
        return createFromNode(node,null);
    }

    public static INSISInstallElement createFromNode(Node node, String type)
    {
        if(node.getNodeName().equals(INSISInstallElement.NODE)) {
            String nodeType = node.getAttributes().getNamedItem(INSISInstallElement.TYPE_ATTRIBUTE).getNodeValue();
            if(Common.isEmpty(type) || nodeType.equals(type)) {
                INSISInstallElement element = create(nodeType);
                if(element != null) {
                    element.fromNode(node);
                    return element;
                }
            }
        }
        return null;
    }

    public static Image getImage(String type)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            return descriptor.getImage();
        }
        return null;
    }

    public static String getTypeName(String type)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            return descriptor.getTypeName();
        }
        return null;
    }

    static void setImage(String type, Image image)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            descriptor.setImage(image);
        }
    }

    static void setTypeName(String type, String typeName)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            descriptor.setTypeName(typeName);
        }
    }

    private static class NSISInstallElementDescriptor
    {
        public String mTypeName;
        public Image mImage;
        public Class mElementClass;
        public Constructor mConstructor;

        public NSISInstallElementDescriptor(Class clasz, String typeName, Image image) throws SecurityException, NoSuchMethodException, IllegalArgumentException
        {
            mElementClass = clasz;
            mConstructor = clasz.getConstructor(EMPTY_CLASS_ARRAY);
            mTypeName = typeName;
            mImage = image;
        }

        /**
         * @return Returns the class.
         */
        public Class getElementClass()
        {
            return mElementClass;
        }

        /**
         * @return Returns the constructor.
         */
        public Constructor getConstructor()
        {
            return mConstructor;
        }

        public String getTypeName()
        {
            return mTypeName;
        }

        /**
         * @return Returns the image.
         */
        public Image getImage()
        {
            return mImage;
        }

        public void setImage(Image image)
        {
            mImage = image;
        }

        public void setTypeName(String typeName)
        {
            mTypeName = typeName;
        }
    }
}
