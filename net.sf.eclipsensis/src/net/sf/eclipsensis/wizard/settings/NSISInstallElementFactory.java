/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
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
        String[] classList = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"preload.nsisinstallelements"); //$NON-NLS-1$
        for (int i = 0; i < classList.length; i++) {
            try {
                Class clasz = Class.forName(classList[i]);
            }
            catch(Exception e)
            {
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
        if(!cElementMap.containsKey(type) && ((NSISInstallElementDescriptor)cElementMap.get(type)).clasz.equals(clasz)) {
            cElementMap.remove(type);
        }
    }
    
    public static INSISInstallElement create(NSISWizardSettings settings, String type)
    {
        NSISInstallElementDescriptor descriptor = (NSISInstallElementDescriptor)cElementMap.get(type);
        if(descriptor != null) {
            try {
                INSISInstallElement element = (INSISInstallElement)descriptor.constructor.newInstance(EMPTY_OBJECT_ARRAY);
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
            return descriptor.image;
        }
        return null;
    }
    
    private static class NSISInstallElementDescriptor
    {
        public Image image;
        public Class clasz;
        public String name;
        public Constructor constructor;
        
        public NSISInstallElementDescriptor(Class clasz) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
        {
            this.clasz = clasz;
            constructor = clasz.getConstructor(EMPTY_CLASS_ARRAY);
            INSISInstallElement instance = (INSISInstallElement)constructor.newInstance(EMPTY_OBJECT_ARRAY);
            image = instance.getImage();
        }
    }
}
