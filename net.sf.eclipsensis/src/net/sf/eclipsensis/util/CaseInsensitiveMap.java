/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
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

public class CaseInsensitiveMap<T> extends AbstractMap<String, T> implements Serializable
{
	private static final long serialVersionUID = 7710930539504135243L;

    private Map<String, T> mValueMap = new LinkedHashMap<String, T>();
    private Map<String, String> mKeyMap = new HashMap<String, String>();

    public CaseInsensitiveMap()
    {
    }

    public CaseInsensitiveMap(Map<String, T> map)
    {
        for(Iterator<String> iter=map.keySet().iterator(); iter.hasNext(); ) {
        	String key = iter.next();
            put(key,  map.get(key));
        }
    }

    private String toUpperCase(String key)
    {
        return (key !=null?key.toUpperCase():key);
    }

    @Override
	public void clear()
    {
        mValueMap.clear();
        mKeyMap.clear();
    }

    @Override
	public boolean containsKey(Object key)
    {
        return key == null || key instanceof String?mKeyMap.containsKey(toUpperCase((String)key)):false;
    }

    @Override
	public Set<Map.Entry<String, T>> entrySet()
    {
        return mValueMap.entrySet();
    }

    @Override
	public T get(Object key)
    {
    	if (key == null || key instanceof String) {
			String uppercaseKey = toUpperCase((String) key);
			if (mKeyMap.containsKey(uppercaseKey)) {
				key = mKeyMap.get(uppercaseKey);
				return mValueMap.get(key);
			}
		}
		return null;
    }

    @Override
	public T remove(Object key)
    {
    	if (key == null || key instanceof String) {
			String fixedKey = toUpperCase((String) key);
			if (mKeyMap.containsKey(fixedKey)) {
				key = mKeyMap.remove(fixedKey);
				return mValueMap.remove(key);
			}
		}
		return null;
    }

    @Override
	public T put(String key, T value)
    {
    	String oldKey = mKeyMap.put(toUpperCase(key),key);
        if(oldKey != null) {
            mValueMap.remove(oldKey);
        }
        return mValueMap.put(key,value);
    }

    @Override
	public String toString()
    {
        return mValueMap.toString();
    }
}
