/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.graphics.Image;

public class NSISInstallElementFactory
{
    private static HashMap cElementMap = new HashMap();
    private static final String TYPE_FIELD_NAME = "TYPE"; //$NON-NLS-1$
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    
    static {
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        if(plugin != null) {
            String[] classList = Common.loadArrayProperty(plugin.getResourceBundle(),"preload.nsisinstallelements"); //$NON-NLS-1$
            for (int i = 0; i < classList.length; i++) {
                try {
                    Class clasz = Class.forName(classList[i]);
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

    public static void register(String type, Class clasz)
    {
        if(!cElementMap.containsKey(type)) {
            try {
                NSISInstallElementDescriptor descriptor = new NSISInstallElementDescriptor(clasz);
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
    
    public static INSISInstallElement create(NSISWizardSettings settings, String type)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            try {
                INSISInstallElement element = (INSISInstallElement)descriptor.getConstructor().newInstance(EMPTY_OBJECT_ARRAY);
                element.setSettings(settings);
                return element;
            }
            catch (Exception e) {
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
    
    private static class NSISInstallElementDescriptor
    {
        public Image mImage;
        public Class mElementClass;
        public String mName;
        public Constructor mConstructor;
        
        public NSISInstallElementDescriptor(Class clasz) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
        {
            mElementClass = clasz;
            mConstructor = clasz.getConstructor(EMPTY_CLASS_ARRAY);
            INSISInstallElement instance = (INSISInstallElement)mConstructor.newInstance(EMPTY_OBJECT_ARRAY);
            mImage = instance.getImage();
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
        
        /**
         * @return Returns the image.
         */
        public Image getImage()
        {
            return mImage;
        }
        
        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }
    }
}
