/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.job;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.ui.progress.UIJob;

public class JobScheduler
{
    private boolean mRunning = false;
    private Set mJobFamilies = new HashSet();

    public void start()
    {
        if(!mRunning) {
            mRunning = true;
        }
    }

    public void stop()
    {
        if(mRunning) {
            mRunning = false;
            IJobManager manager = Platform.getJobManager();
            if(manager != null) {
                for(Iterator iter = mJobFamilies.iterator(); iter.hasNext(); ) {
                    manager.cancel(iter.next());
                    iter.remove();
                }
            }
        }
    }

    public boolean isScheduled(Object family)
    {
        return !Common.isEmptyArray(Platform.getJobManager().find(family));
    }

    public void scheduleUIJob(String name, IJobStatusRunnable runnable)
    {
        scheduleUIJob(null, name, runnable);
    }

    public void scheduleUIJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        final Object jobFamily = (family == null?this:family);

        new UIJob(name) {
            public IStatus runInUIThread(IProgressMonitor monitor)
            {
                return runnable.run(monitor);
            }

            public boolean belongsTo(Object family)
            {
                return jobFamily.equals(family);
            }

        }.schedule();
    }

    public void scheduleJob(String name, IJobStatusRunnable runnable)
    {
        scheduleJob(null, name, runnable);
    }

    public void scheduleJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        final Object jobFamily = (family == null?this:family);

        new Job(name) {
            public IStatus run(IProgressMonitor monitor)
            {
                return runnable.run(monitor);
            }

            public boolean belongsTo(Object family)
            {
                return jobFamily.equals(family);
            }

        }.schedule();
    }

    public void cancelJobs(Object family)
    {
        if(family != null && mRunning && mJobFamilies.contains(family)) {
            mJobFamilies.remove(family);
            Platform.getJobManager().cancel(family);
            try {
                Platform.getJobManager().join(family, new NullProgressMonitor());
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }
}
