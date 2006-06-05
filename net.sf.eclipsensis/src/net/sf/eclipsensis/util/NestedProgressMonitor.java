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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class NestedProgressMonitor extends ProgressMonitorWrapper
{
    private int mTicks;
    private double mScale;
    private double mUsed = 0.0;
    private boolean mCompleted = false;
    private boolean mHasSubTask = false;
    private String mParentTaskName;
    
    public NestedProgressMonitor(IProgressMonitor monitor, String parentTaskName, int ticks)
    {
        super(monitor);
        mParentTaskName = parentTaskName;
        mTicks = ticks;
    }

    public void beginTask(String name, int totalWork) 
    {
        mScale = totalWork <= 0 ? 0 : (double) mTicks / (double) totalWork;
        setTaskName(name);
    }

    public void done() 
    {
        double remaining = mTicks - mUsed;
        if (remaining > 0) {
            super.internalWorked(remaining);
        }
        if (mHasSubTask) {
            subTask(""); //$NON-NLS-1$
        }
        mUsed = 0;
        setTaskName(mParentTaskName);
    }

    public void internalWorked(double work) 
    {
        if (mCompleted) {
            return;
        }

        double realWork = mScale * work;
        super.internalWorked(realWork);
        mUsed += realWork;
        if (mUsed >= mTicks) {
            mCompleted = true;
        }
    }

    public void subTask(String name) 
    {
        mHasSubTask = true;
        super.subTask(name);
    }

    public void worked(int work) {
        internalWorked(work);
    }
}
