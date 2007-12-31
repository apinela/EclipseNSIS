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

public final class MRUMap extends LinkedHashMap
{
    private static final long serialVersionUID = -4303663274693162132L;

    private final int mMaxSize;

    public MRUMap(int maxSize)
    {
        super(15,0.75f,true);
        mMaxSize= maxSize;
    }

    public MRUMap(int maxSize, Map map)
    {
        this(maxSize);
        putAll(map);
    }

    public Object put(Object key, Object value)
    {
        Object object= remove(key);
        super.put(key, value);
        return object;
    }

    protected boolean removeEldestEntry(Map.Entry eldest)
    {
        return (mMaxSize > 0 && size() > mMaxSize);
    }
}