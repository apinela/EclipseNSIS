/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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

public class CaseInsensitiveSet implements Set, Serializable
{
	private static final long serialVersionUID = -3353276139904582714L;

    private Map mValueMap = new HashMap();
    private Set mSet = new LinkedHashSet();

    public CaseInsensitiveSet()
    {
    }

    public CaseInsensitiveSet(Collection coll)
    {
        for(Iterator iter=coll.iterator(); iter.hasNext(); ) {
            Object value = iter.next();
            add(value);
        }
    }

    private Object fixValue(Object value)
    {
        return (value !=null && value instanceof String?((String)value).toUpperCase():value);
    }

    /* (non-Javadoc)
     * @see java.util.Set#size()
     */
    public int size()
    {
        return mSet.size();
    }

    /* (non-Javadoc)
     * @see java.util.Set#clear()
     */
    public void clear()
    {
        mValueMap.clear();
        mSet.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty()
    {
        return mSet.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object value)
    {
        return mValueMap.containsKey(fixValue(value));
    }

    /* (non-Javadoc)
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection coll)
    {
        boolean rv = false;
        for(Iterator iter=coll.iterator(); iter.hasNext(); ) {
            if(add(iter.next())) {
                rv = true;
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object value)
    {
        Object fixedVal = fixValue(value);
        if(mValueMap.containsKey(fixedVal)) {
            value = mValueMap.remove(fixedVal);
            return mSet.remove(value);
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(Object value)
    {
        Object fixedVal = fixValue(value);
        if(!mValueMap.containsKey(fixedVal)) {
            mValueMap.put(fixedVal,value);
            return mSet.add(value);
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray()
     */
    public Object[] toArray()
    {
        return mSet.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c)
    {
        if(!Common.isEmptyCollection(c)) {
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                if(!mValueMap.containsKey(fixValue(iter.next()))) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c)
    {
        boolean rv = false;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object element = fixValue(iter.next());
            if(mValueMap.containsKey(element)) {
                element = mValueMap.remove(element);
                mSet.remove(element);
                rv = true;
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c)
    {
        HashSet set = new HashSet();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object element = fixValue(iter.next());
            if(mValueMap.containsKey(element)) {
                set.add(mValueMap.get(element));
            }
        }
        if(set.size() != mSet.size()) {
            clear();
            addAll(set);
            return true;
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#iterator()
     */
    public Iterator iterator()
    {
        return mSet.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a)
    {
        return mSet.toArray(a);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return mSet.toString();
    }
}
