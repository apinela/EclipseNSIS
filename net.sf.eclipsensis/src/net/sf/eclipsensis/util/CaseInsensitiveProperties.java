/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class CaseInsensitiveProperties extends Properties
{
    public CaseInsensitiveProperties()
    {
        super();
    }

    /**
     * @param defaults
     */
    public CaseInsensitiveProperties(Properties defaults)
    {
        this();
        CaseInsensitiveProperties newDefaults = new CaseInsensitiveProperties();
        newDefaults.putAll(defaults);
        this.defaults = newDefaults;
    }
    
    private String fixKey(Object key)
    {
        return (key !=null && key instanceof String?((String)key).toLowerCase():null);
    }
    
    /* (non-Javadoc)
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String key, String defaultValue)
    {
        return super.getProperty(fixKey(key), defaultValue);
    }
    
    /* (non-Javadoc)
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    public String getProperty(String key)
    {
        return super.getProperty(fixKey(key));
    }
    
    /* (non-Javadoc)
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    public synchronized Object setProperty(String key, String value)
    {
        return super.setProperty(fixKey(key), value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public synchronized boolean containsKey(Object key)
    {
        return super.containsKey(fixKey(key));
    }

    /* (non-Javadoc)
     * @see java.util.Dictionary#get(java.lang.Object)
     */
    public synchronized Object get(Object key)
    {
        return super.get(fixKey(key));
    }

    /* (non-Javadoc)
     * @see java.util.Dictionary#put(java.lang.Object, java.lang.Object)
     */
    public synchronized Object put(Object key, Object value)
    {
        return super.put(fixKey(key), value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public synchronized void putAll(Map t)
    {
        for(Iterator iter=t.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iter.next();
            put(fixKey(entry.getKey()),entry.getValue());
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Dictionary#remove(java.lang.Object)
     */
    public synchronized Object remove(Object key)
    {
        return super.remove(fixKey(key));
    }
}
