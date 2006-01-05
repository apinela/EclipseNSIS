/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import java.text.*;
import java.util.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.Scheduler;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class UpdatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IUpdatePreferenceConstants
{
    private static final String LINK_TEXT;
    
    private static String[] cTimesOfDay;
    private static String[] cDaysOfWeek;
    
    private Button mAutoUpdate;
    private Button mStartup;
    private Button mDaily;
    private Button mWeekly;
    private Button mMonthly;
    private Combo mDailyTime;
    private Combo mDayOfWeek;
    private Combo mWeeklyTime;
    private Combo mDayOfMonth;
    private Combo mMonthlyTime;
    private Button mNotify;
    private Button mDownload;
    private Button mInstall;
    private Button mIgnorePreview;

    private Map mEnablers = new HashMap();

    static {
        String id1 = null;
        String id2 = null;
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.preferencePages"); //$NON-NLS-1$
        if(extensionPoint != null) {
            IExtension[] extensions = extensionPoint.getExtensions();
            if(extensions != null) {
                for (int i = 0; i < extensions.length; i++) {
                    if(id1 == null) {
                        id1 = getPageId(extensions[i], "org.eclipse.update.ui"); //$NON-NLS-1$
                    }
                    if(id2 == null) {
                        id2 = getPageId(extensions[i], "org.eclipse.update.scheduler"); //$NON-NLS-1$
                    }
                    if(id1 != null && id2 != null) {
                        break;
                    }
                }
            }
        }
        LINK_TEXT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("preference.page.header")).format(new String[] {id1,id2}); //$NON-NLS-1$
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        cTimesOfDay = new String[24];
        for (int i = 0; i < SchedulerConstants.TIMES_OF_DAY.length; i++) {
            cal.set(Calendar.HOUR_OF_DAY, SchedulerConstants.TIMES_OF_DAY[i]);
            cTimesOfDay[i] = df.format(cal.getTime());
        }
        
        df = new SimpleDateFormat("EEEE"); //$NON-NLS-1$
        cDaysOfWeek = new String[SchedulerConstants.DAYS_OF_WEEK.length];
        for (int i = 0; i < SchedulerConstants.DAYS_OF_WEEK.length; i++) {
            cal.set(Calendar.DAY_OF_WEEK,SchedulerConstants.DAYS_OF_WEEK[i]);
            cDaysOfWeek[i]= df.format(cal.getTime());
        }
    }

    private static String getPageId(IExtension extension, String name)
    {
        if(name.equals(extension.getNamespace())) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                if(elements[j].getName().equals("page")) { //$NON-NLS-1$
                    return elements[j].getAttribute("id"); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    protected IPreferenceStore doGetPreferenceStore()
    {
        return EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
    }

    protected Control createContents(Composite parent)
    {
        parent = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        Link link= new Link(parent, SWT.NONE);
        link.setText(LINK_TEXT);
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                if(e.text != null) {
                    PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null);
                }
            }
        });
        link.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        Label l = new Label(parent,SWT.SEPARATOR|SWT.HORIZONTAL);
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        mAutoUpdate = new Button(parent,SWT.CHECK);
        mAutoUpdate.setText(EclipseNSISUpdatePlugin.getResourceString("auto.update.label")); //$NON-NLS-1$
        mAutoUpdate.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        createScheduleGroup(parent);
        createActionGroup(parent);
        createOptionsGroup(parent);

        loadPreferences();
        
        new Enabler(mAutoUpdate, new Control[] {mStartup,mDaily,mWeekly,mMonthly,
                    mNotify,mDownload,mInstall,mIgnorePreview}).run();
        return parent;
    }

    private void loadPreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        mAutoUpdate.setSelection(prefs.getBoolean(AUTO_UPDATE));
        
        int updateSchedule = prefs.getInt(UPDATE_SCHEDULE);
        mStartup.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_ON_STARTUP);
        
        mDaily.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_DAILY);
        mDailyTime.select(prefs.getInt(DAILY_TIME));
        
        mWeekly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_WEEKLY);
        mDayOfWeek.select(prefs.getInt(DAY_OF_WEEK));
        mWeeklyTime.select(prefs.getInt(WEEKLY_TIME));
        
        mMonthly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_MONTHLY);
        mDayOfMonth.select(prefs.getInt(DAY_OF_MONTH));
        mMonthlyTime.select(prefs.getInt(MONTHLY_TIME));
        
        int updateAction = prefs.getInt(UPDATE_ACTION);
        mNotify.setSelection(updateAction == SchedulerConstants.UPDATE_NOTIFY);
        mDownload.setSelection(updateAction == SchedulerConstants.UPDATE_DOWNLOAD);
        mInstall.setSelection(updateAction == SchedulerConstants.UPDATE_INSTALL);
        
        mIgnorePreview.setSelection(prefs.getBoolean(IGNORE_PREVIEW));
    }

    private void loadDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();
        mAutoUpdate.setSelection(prefs.getDefaultBoolean(AUTO_UPDATE));
        
        int updateSchedule = prefs.getDefaultInt(UPDATE_SCHEDULE);
        mStartup.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_ON_STARTUP);
        
        mDaily.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_DAILY);
        mDailyTime.select(prefs.getDefaultInt(DAILY_TIME));
        
        mWeekly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_WEEKLY);
        mDayOfWeek.select(prefs.getDefaultInt(DAY_OF_WEEK));
        mWeeklyTime.select(prefs.getDefaultInt(WEEKLY_TIME));
        
        mMonthly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_MONTHLY);
        mDayOfMonth.select(prefs.getDefaultInt(DAY_OF_MONTH));
        mMonthlyTime.select(prefs.getDefaultInt(MONTHLY_TIME));
        
        int updateAction = prefs.getDefaultInt(UPDATE_ACTION);
        mNotify.setSelection(updateAction == SchedulerConstants.UPDATE_NOTIFY);
        mDownload.setSelection(updateAction == SchedulerConstants.UPDATE_DOWNLOAD);
        mInstall.setSelection(updateAction == SchedulerConstants.UPDATE_INSTALL);
        
        mIgnorePreview.setSelection(prefs.getDefaultBoolean(IGNORE_PREVIEW));
    }

    private void savePreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        prefs.setValue(AUTO_UPDATE,mAutoUpdate.getSelection());
        
        int updateSchedule = (mStartup.getSelection()?SchedulerConstants.SCHEDULE_ON_STARTUP:
                              (mDaily.getSelection()?SchedulerConstants.SCHEDULE_DAILY:
                               (mWeekly.getSelection()?SchedulerConstants.SCHEDULE_WEEKLY:
                                SchedulerConstants.SCHEDULE_MONTHLY)));
        prefs.setValue(UPDATE_SCHEDULE, updateSchedule);

        prefs.setValue(DAILY_TIME,mDailyTime.getSelectionIndex());
        
        prefs.setValue(DAY_OF_WEEK,mDayOfWeek.getSelectionIndex());
        prefs.setValue(WEEKLY_TIME,mWeeklyTime.getSelectionIndex());
        
        prefs.setValue(DAY_OF_MONTH,mDayOfMonth.getSelectionIndex());
        prefs.setValue(MONTHLY_TIME,mMonthlyTime.getSelectionIndex());
        
        int updateAction = (mNotify.getSelection()?SchedulerConstants.UPDATE_NOTIFY:
                            (mDownload.getSelection()?SchedulerConstants.UPDATE_DOWNLOAD:
                             SchedulerConstants.UPDATE_INSTALL));
        prefs.setValue(UPDATE_ACTION, updateAction);
        
        prefs.setValue(IGNORE_PREVIEW,mIgnorePreview.getSelection());
    }

    private void createScheduleGroup(Composite parent)
    {
        GridLayout layout;
        Label l;
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.group.label")); //$NON-NLS-1$
        layout = new GridLayout(5,false);
        group.setLayout(layout);
        
        mStartup = new Button(group, SWT.RADIO);
        mStartup.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.startup.label")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,false);
        gridData.horizontalSpan = 5;
        mStartup.setLayoutData(gridData);
        
        mDaily = new Button(group, SWT.RADIO);
        mDaily.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.daily.label")); //$NON-NLS-1$
        mDaily.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        
        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.daily.time.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
        mDailyTime = createTimeCombo(group);
        mDailyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        Label l2 = new Label(group,SWT.NONE);
        l2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        ((GridData)l2.getLayoutData()).horizontalSpan = 2;
        
        new Enabler(mDaily, new Control[] {l, mDailyTime});
        
        mWeekly = new Button(group, SWT.RADIO);
        mWeekly.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.label")); //$NON-NLS-1$
        mWeekly.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        
        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.day.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
        mDayOfWeek = new Combo(group, SWT.DROP_DOWN|SWT.READ_ONLY);
        mDayOfWeek.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        mDayOfWeek.setItems(cDaysOfWeek);
        mDayOfWeek.select(0);
        
        l2 = new Label(group,SWT.NONE);
        l2.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.time.label")); //$NON-NLS-1$
        l2.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
        mWeeklyTime = createTimeCombo(group);
        mWeeklyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        
        new Enabler(mWeekly, new Control[] {l, mDayOfWeek, l2, mWeeklyTime});

        mMonthly = new Button(group, SWT.RADIO);
        mMonthly.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.label")); //$NON-NLS-1$
        mMonthly.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        
        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.day.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
        mDayOfMonth = new Combo(group, SWT.DROP_DOWN|SWT.READ_ONLY);
        mDayOfMonth.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        for(int i=0; i<SchedulerConstants.DAYS_OF_MONTH.length; i++) {
            mDayOfMonth.add(Integer.toString(SchedulerConstants.DAYS_OF_MONTH[i]));
        }
        mDayOfMonth.select(0);
        
        l2 = new Label(group,SWT.NONE);
        l2.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.time.label")); //$NON-NLS-1$
        l2.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
        mMonthlyTime = createTimeCombo(group);
        mMonthlyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));

        new Enabler(mMonthly, new Control[] {l, mDayOfMonth, l2, mMonthlyTime});
    }

    private void createActionGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));
        
        mNotify = new Button(group, SWT.RADIO);
        mNotify.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.notify.label")); //$NON-NLS-1$
        mNotify.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        mDownload = new Button(group, SWT.RADIO);
        mDownload.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.download.label")); //$NON-NLS-1$
        mDownload.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        mInstall = new Button(group, SWT.RADIO);
        mInstall.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.install.label")); //$NON-NLS-1$
        mInstall.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
    }

    private void createOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.options.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));
        
        mIgnorePreview = new Button(group, SWT.CHECK);
        mIgnorePreview.setText(EclipseNSISUpdatePlugin.getResourceString("ignore.preview.label")); //$NON-NLS-1$
        mIgnorePreview.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
    }

    private Combo createTimeCombo(Composite parent)
    {
        Combo c = new Combo(parent,SWT.DROP_DOWN|SWT.READ_ONLY);
        c.setItems(cTimesOfDay);
        c.select(0);
        return c;
    }

    public boolean performOk()
    {
        // TODO Auto-generated method stub
        boolean ok = super.performOk();
        if(ok) {
            savePreferences();
            Scheduler scheduler = Scheduler.getInstance();
            if(scheduler != null) {
                scheduler.scheduleUpdateJob();
            }
        }
        return ok;
    }

    protected void performDefaults()
    {
        loadDefaults();
        ((Enabler)mEnablers.get(mAutoUpdate)).run();
        super.performDefaults();
    }

    public void init(IWorkbench workbench)
    {
    }
    
    private class Enabler
    {
        private Button mButton;
        private Control[] mDependents;
        
        public Enabler(Button button, Control[] dependents)
        {
            super();
            mButton = button;
            mDependents = dependents;
            mButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    run();
                }
            });
            
            mEnablers.put(mButton, this);
        }
        
        public void run()
        {
            boolean selected = mButton.getSelection();
            for (int i = 0; i < mDependents.length; i++) {
                mDependents[i].setEnabled(selected);
                if(mDependents[i] instanceof Button) {
                    Enabler enabler = (Enabler)mEnablers.get(mDependents[i]);
                    if(enabler != null) {
                        enabler.run();
                    }
                }
            }
        }
    }
}
