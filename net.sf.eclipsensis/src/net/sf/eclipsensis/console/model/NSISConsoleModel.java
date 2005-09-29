/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console.model;

import java.util.*;

import net.sf.eclipsensis.console.NSISConsoleLine;

public class NSISConsoleModel
{
    private Set mListeners = new HashSet();
    private List mContents = Collections.synchronizedList(new ArrayList());
    private ArrayList mErrors = new ArrayList();
    private ArrayList mWarnings = new ArrayList();

    public boolean supportsStatistics()
    {
        return true;
    }

    public void addModelListener(INSISConsoleModelListener listener)
    {
        mListeners.add(listener);
    }

    public void removeModelListener(INSISConsoleModelListener listener)
    {
        mListeners.remove(listener);
    }
    
    private void notifyListeners(int type, NSISConsoleLine line)
    {
        NSISConsoleModelEvent event = new NSISConsoleModelEvent(type,line);
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            INSISConsoleModelListener listener = (INSISConsoleModelListener)iter.next();
            listener.modelChanged(event);
        }
    }

    public void add(NSISConsoleLine line)
    {
        mContents.add(line);
        if(line.getType() == NSISConsoleLine.TYPE_ERROR) {
            mErrors.add(line);
        }
        else if(line.getType() == NSISConsoleLine.TYPE_WARNING) {
            mWarnings.add(line);
        }
        notifyListeners(NSISConsoleModelEvent.ADD, line);
    }

    public void remove(NSISConsoleLine line)
    {
        mContents.remove(line);
        if(line.getType() == NSISConsoleLine.TYPE_ERROR) {
            mErrors.remove(line);
        }
        else if(line.getType() == NSISConsoleLine.TYPE_WARNING) {
            mWarnings.remove(line);
        }
        notifyListeners(NSISConsoleModelEvent.REMOVE, line);
    }

    public void clear()
    {
        mContents.clear();
        mErrors.clear();
        mWarnings.clear();
        notifyListeners(NSISConsoleModelEvent.CLEAR, null);
    }

    private List getUnmodifiableList(List list)
    {
        return (list==null?null:Collections.unmodifiableList(list));
    }
    
    public List getContents()
    {
        return getUnmodifiableList(mContents);
    }

    /**
     * @return Returns the errors.
     */
    public List getErrors()
    {
        return getUnmodifiableList(mErrors);
    }

    /**
     * @return Returns the warnings.
     */
    public List getWarnings()
    {
        return getUnmodifiableList(mWarnings);
    }
}
