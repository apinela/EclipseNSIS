/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini.validators;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

public class INIKeyValueValidatorRegistry
{
    private static Map mRegistry = new CaseInsensitiveMap();
    
    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(INIKeyValueValidatorRegistry.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }

        if(bundle != null) {
            HashMap map = new HashMap();
            for(Enumeration enum=bundle.getKeys(); enum.hasMoreElements();) {
                try {
                    IINIKeyValueValidator validator;

                    String key = (String)enum.nextElement();
                    String className = (String)bundle.getString(key);
                    Class clasz = Class.forName(className);
                    if(map.containsKey(clasz)) {
                        validator = (IINIKeyValueValidator)map.get(clasz);
                    }
                    else {
                        Constructor c = clasz.getConstructor(null);
                        validator = (IINIKeyValueValidator)c.newInstance(null);
                        map.put(clasz,validator);
                    }
                    mRegistry.put(key,validator);
                }
                catch(Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }            
        }
    }
    
    private INIKeyValueValidatorRegistry()
    {
    }
    
    public static IINIKeyValueValidator getKeyValueValidator(String name)
    {
        return (IINIKeyValueValidator)mRegistry.get(name);
    }
}
