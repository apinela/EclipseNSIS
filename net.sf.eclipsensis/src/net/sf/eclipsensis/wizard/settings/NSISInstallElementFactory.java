/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Node;

public class NSISInstallElementFactory
{
    private static final String TYPE_ALIASES = "type.aliases"; //$NON-NLS-1$
    private static final String PRELOAD_INSTALLELEMENTS = "preload.installelements"; //$NON-NLS-1$
    private static final String VALID_TYPES = "valid.types"; //$NON-NLS-1$
    
    private static final ResourceBundle cBundle;
    private static final Map cTypeAliases = new HashMap();
    private static final Set cValidTypes = new HashSet();
    private static final Map cElementMap = new HashMap();
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static INSISHomeListener cNSISHomeListener  = new INSISHomeListener() {
        public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
        {
            loadTypes(monitor);
        }
    };

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISInstallElementFactory.class.getName());
        } 
        catch (MissingResourceException x) {
            bundle = null;
        }
        cBundle = bundle;
        if(cBundle != null) {
            String typeAliasesList;
            try {
                typeAliasesList = cBundle.getString(TYPE_ALIASES);
            }
            catch(MissingResourceException mre) {
                typeAliasesList = null;
            }
            String[] typeAliases = Common.tokenize(typeAliasesList, ',');
            for (int i = 0; i < typeAliases.length; i++) {
                int n = typeAliases[i].indexOf('=');
                if(n > 0 && n < typeAliases[i].length()-1) {
                    String type = typeAliases[i].substring(0,n);
                    String alias = typeAliases[i].substring(n+1);
                    cTypeAliases.put(type, alias);
                    cTypeAliases.put(alias, type);
                }
            }
        }
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;
            
            public void start(IProgressMonitor monitor)
            {
                loadTypes(monitor);
                NSISPreferences.INSTANCE.addListener(cNSISHomeListener);
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                mStarted = false;
                NSISPreferences.INSTANCE.removeListener(cNSISHomeListener);
            }

            public boolean isStarted()
            {
                return mStarted;
            }
        });
        if(cBundle != null) {
            String classList;
            try {
                classList = cBundle.getString(PRELOAD_INSTALLELEMENTS);
            }
            catch(MissingResourceException mre) {
                classList = null;
            }
            String[] classes = Common.tokenize(classList, ',');
            if(!Common.isEmptyArray(classes)) {
                for (int i = 0; i < classes.length; i++) {
                    try {
                        Class.forName(classes[i]);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static String getAlias(String type)
    {
        return (String)cTypeAliases.get(type);
    }
    
    private static void loadTypes(IProgressMonitor monitor)
    {
        Version nsisVersion = NSISPreferences.INSTANCE.getNSISVersion();
        cValidTypes.clear();
        if(cBundle != null) {
            Version maxVersion = null;
            String validTypes = null;
            for(Enumeration e=cBundle.getKeys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                if(key.startsWith(VALID_TYPES)) {
                    int n = key.indexOf('#');
                    Version version = (n >= 0?new Version(key.substring(n+1)):NSISValidator.MINIMUM_NSIS_VERSION);
                    if(nsisVersion.compareTo(version) >= 0) {
                        if(maxVersion == null || version.compareTo(maxVersion) > 0) {
                            maxVersion = version;
                            validTypes = cBundle.getString(key);
                        }
                    }
                }
            }
            if(validTypes != null) {
                cValidTypes.addAll(Common.tokenizeToList(validTypes, ','));
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
        if(cElementMap.containsKey(type) && ((NSISInstallElementDescriptor)cElementMap.get(type)).getElementClass().equals(clasz)) {
            cElementMap.remove(type);
        }
    }

    private static NSISInstallElementDescriptor getDescriptor(String type)
    {
        if(!cValidTypes.contains(type)) {
            type = (String)cTypeAliases.get(type);
            if(type == null || !cValidTypes.contains(type)) {
                return null;
            }
        }
        return (NSISInstallElementDescriptor)cElementMap.get(type);
    }

    public static INSISInstallElement create(String type)
    {
        NSISInstallElementDescriptor descriptor = getDescriptor(type);
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
        NSISInstallElementDescriptor descriptor = getDescriptor(type);
        if(descriptor != null) {
            return descriptor.getImage();
        }
        return null;
    }

    public static String getTypeName(String type)
    {
        NSISInstallElementDescriptor descriptor = getDescriptor(type);
        if(descriptor != null) {
            return descriptor.getTypeName();
        }
        return null;
    }

    public static boolean isValidType(String type)
    {
        return cValidTypes.contains(type) || (!cValidTypes.contains(null) && cValidTypes.contains(cTypeAliases.get(type)));
    }

    static void setImage(String type, Image image)
    {
        NSISInstallElementDescriptor descriptor = getDescriptor(type);
        if(descriptor != null) {
            descriptor.setImage(image);
        }
    }

    static void setTypeName(String type, String typeName)
    {
        NSISInstallElementDescriptor descriptor = getDescriptor(type);
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
