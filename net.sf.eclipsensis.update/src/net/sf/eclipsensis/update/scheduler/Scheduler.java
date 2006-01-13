/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.scheduler;

import java.lang.reflect.Constructor;
import java.util.Calendar;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateJobSettings;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

public class Scheduler implements IStartup, IUpdatePreferenceConstants
{
    private static Scheduler INSTANCE = null;

    private Job mScheduledJob = null;
    private boolean mStartedUp = false;
    private IPreferenceStore mPreferences;

    public static Scheduler getInstance()
    {
        return INSTANCE;
    }

    public Scheduler()
    {
        if(INSTANCE == null) {
            INSTANCE = this;
            mPreferences = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
        }
    }

    public void earlyStartup()
    {
        scheduleUpdateJob();
    }

    public void shutDown()
    {
        Platform.getJobManager().cancel(NSISUpdateJobSettings.JOB_FAMILY);
        mScheduledJob = null;
    }

    public synchronized void scheduleUpdateJob() 
    {
        if (INSTANCE == this) {
            long delay = -1;
            if (mPreferences.getBoolean(IUpdatePreferenceConstants.AUTO_UPDATE)) {
                int schedule = mPreferences.getInt(IUpdatePreferenceConstants.UPDATE_SCHEDULE);
                switch (schedule)
                {
                    case SchedulerConstants.SCHEDULE_ON_STARTUP:
                    {
                        if (!mStartedUp) {
                            delay = 0;
                        }
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_DAILY:
                    {
                        delay = computeDelayForDailySchedule();
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_WEEKLY:
                    {
                        delay = computeDelayForWeeklySchedule();
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_MONTHLY:
                    {
                        delay = computeDelayForMonthlySchedule();
                        break;
                    }
                    default:
                        return;
                }
            }
            schedule(delay);
            if(delay >=0 && !mStartedUp) {
                mStartedUp = true;
            }
        }
    }
    
    private synchronized void schedule(long delay)
    {
        if(mScheduledJob != null) {
            Platform.getJobManager().cancel(mScheduledJob);
            mScheduledJob = null;
        }
        if (delay >= 0) {
            int updateAction = mPreferences.getInt(IUpdatePreferenceConstants.UPDATE_ACTION);
            if(updateAction < SchedulerConstants.UPDATE_NOTIFY || updateAction > SchedulerConstants.UPDATE_INSTALL) {
                updateAction = SchedulerConstants.DEFAULT_ACTION;
            }
            boolean ignorePreview = mPreferences.getBoolean(IUpdatePreferenceConstants.IGNORE_PREVIEW);
            NSISUpdateJobSettings settings = new NSISUpdateJobSettings(true, updateAction, ignorePreview);
            mScheduledJob = createUpdateJob(settings);
            if (mScheduledJob != null) {
                mScheduledJob.schedule(delay);
            }
        }
    }
    
    /*
     * Loads the update job using reflection to avoid premature startup of the
     * EclipseNSIS plug-in.
     */
    private Job createUpdateJob(NSISUpdateJobSettings settings) 
    {
        try {
            Class theClass = Class.forName("net.sf.eclipsensis.update.jobs.NSISCheckUpdateJob"); //$NON-NLS-1$
            Constructor constructor = theClass.getConstructor(new Class[] { NSISUpdateJobSettings.class });
            return (Job) constructor.newInstance(new Object[] { settings });
        } 
        catch (Exception e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
            return null;
        }
    }

    private long computeDelayForDailySchedule()
    {
        int n = mPreferences.getInt(IUpdatePreferenceConstants.DAILY_TIME);
        if(n >= 0 && n < SchedulerConstants.TIMES_OF_DAY.length) {
            Calendar cal = Calendar.getInstance();
            int targetHour = SchedulerConstants.TIMES_OF_DAY[n];
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentMin = cal.get(Calendar.MINUTE);
            int currentSec = cal.get(Calendar.SECOND);
            int currentMilliSec = cal.get(Calendar.MILLISECOND);
            
            if(currentHour == targetHour && currentMin == 0 && currentSec == 0) {
                return 0;
            }
            
            int hourDiff = targetHour-currentHour + (currentHour >= targetHour?24:0);
            return ((hourDiff * 60 - currentMin) * 60 - currentSec) * 1000 - currentMilliSec;
        }
        return -1;
    }
    
    private long computeDelayForWeeklySchedule()
    {
        int m = mPreferences.getInt(IUpdatePreferenceConstants.DAY_OF_WEEK);
        int n = mPreferences.getInt(IUpdatePreferenceConstants.WEEKLY_TIME);
        if((m >= 0 && m < SchedulerConstants.DAYS_OF_WEEK.length) &&
           (n >= 0 && n < SchedulerConstants.TIMES_OF_DAY.length)) {
            int targetDay = SchedulerConstants.DAYS_OF_WEEK[m];
            int targetHour = SchedulerConstants.TIMES_OF_DAY[n];

            Calendar cal = Calendar.getInstance();
            int currentDay = cal.get(Calendar.DAY_OF_WEEK);
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentMin = cal.get(Calendar.MINUTE);
            int currentSec = cal.get(Calendar.SECOND);
            int currentMilliSec = cal.get(Calendar.MILLISECOND);

            if(currentDay == targetDay && currentHour == targetHour && 
                    currentMin == 0 && currentSec == 0) {
                return 0;
            }
            int dayDiff = targetDay - currentDay;
            if (targetDay < currentDay || 
                    (targetDay == currentDay && 
                            (targetHour < currentHour || 
                                    (targetHour == currentHour && currentMin > 0)))) {
                dayDiff += 7;
            }
            return (((dayDiff*24 + targetHour - currentHour)*60 - currentMin)*60 - 
                        currentSec)*1000 - currentMilliSec;
        }
        return -1;
    }
    
    private long computeDelayForMonthlySchedule()
    {
        int m = mPreferences.getInt(IUpdatePreferenceConstants.DAY_OF_MONTH);
        int n = mPreferences.getInt(IUpdatePreferenceConstants.MONTHLY_TIME);
        if((m >= 0 && m < SchedulerConstants.DAYS_OF_MONTH.length) &&
           (n >= 0 && n < SchedulerConstants.TIMES_OF_DAY.length)) {
            int targetDay = SchedulerConstants.DAYS_OF_MONTH[m];
            int targetHour = SchedulerConstants.TIMES_OF_DAY[n];

            Calendar cal = Calendar.getInstance();
            
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH);
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentMin = cal.get(Calendar.MINUTE);
            int currentSec = cal.get(Calendar.SECOND);

            long now = cal.getTimeInMillis();

            if(currentDay == (targetDay>maxDay?maxDay:targetDay) && currentHour == targetHour && 
                    currentMin == 0 && currentSec == 0) {
                return 0;
            }
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            cal.set(Calendar.HOUR_OF_DAY,targetHour);
            
            cal.set(Calendar.DAY_OF_MONTH, (targetDay>maxDay?maxDay:targetDay));
            long targetTime = cal.getTimeInMillis();
            
            if (targetTime < now) {
                int targetYear = currentYear;
                int targetMonth = currentMonth + 1;
                if (targetMonth > cal.getMaximum(Calendar.MONTH)) {
                    targetYear++;
                    targetMonth = cal.getMinimum(Calendar.MONTH);
                }
                cal.set(Calendar.YEAR, targetYear);
                cal.set(Calendar.MONTH, targetMonth);
                maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, (targetDay > maxDay?maxDay:targetDay));
                targetTime = cal.getTimeInMillis();
            }            
            if(targetTime >= now) {
                return targetTime-now;
            }
        }
        return -1;
    }
}
