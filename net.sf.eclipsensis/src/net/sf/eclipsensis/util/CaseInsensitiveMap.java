/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.Serializable;
import java.util.*;

public class CaseInsensitiveMap implements Map, Serializable
{
	private static final long serialVersionUID = 7710930539504135243L;

    private Map mValueMap = new LinkedHashMap();
    private Map mKeyMap = new HashMap();

    public CaseInsensitiveMap()
    {
    }

    public CaseInsensitiveMap(Map map)
    {
        for(Iterator iter=map.keySet().iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            put(key,  map.get(key));
        }
    }

    private Object fixKey(Object key)
    {
        return (key !=null && key instanceof String?((String)key).toUpperCase():key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size()
    {
        return mValueMap.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        mValueMap.clear();
        mKeyMap.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return mValueMap.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return mKeyMap.containsKey(fixKey(key));
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {
        return mValueMap.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values()
    {
        return mValueMap.values();
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t)
    {
        for(Iterator iter=t.keySet().iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            put(key,t.get(key));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        return mValueMap.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet()
    {
        return mValueMap.keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        Object fixedKey = fixKey(key);
        if(mKeyMap.containsKey(fixedKey)) {
            key = mKeyMap.get(fixedKey);
            return mValueMap.get(key);
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        Object fixedKey = fixKey(key);
        if(mKeyMap.containsKey(fixedKey)) {
            key = mKeyMap.remove(fixedKey);
            return mValueMap.remove(key);
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {
        Object oldKey = mKeyMap.put(fixKey(key),key);
        if(oldKey != null) {
            mValueMap.remove(oldKey);
        }
        return mValueMap.put(key,value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return mValueMap.toString();
    }
}
