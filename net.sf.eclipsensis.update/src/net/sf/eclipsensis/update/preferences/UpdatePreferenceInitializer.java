/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

import org.eclipse.core.runtime.preferences.*;

public class UpdatePreferenceInitializer extends AbstractPreferenceInitializer implements IUpdatePreferenceConstants
{
    public void initializeDefaultPreferences()
    {
        IEclipsePreferences prefs = new DefaultScope().getNode(EclipseNSISUpdatePlugin.getDefault().getPluginId());

        prefs.put(USE_HTTP_PROXY, Boolean.FALSE.toString());
        prefs.put(HTTP_PROXY_HOST, ""); //$NON-NLS-1$
        prefs.put(HTTP_PROXY_PORT, ""); //$NON-NLS-1$

        prefs.put(NSIS_UPDATE_SITE, NSISUpdateURLs.getDefaultUpdateSite());
        prefs.put(SOURCEFORGE_MIRROR, NSISUpdateURLs.getDefaultDownloadSite());
        prefs.putBoolean(AUTOSELECT_SOURCEFORGE_MIRROR, true);

        prefs.put(IGNORE_PREVIEW, Boolean.valueOf(SchedulerConstants.DEFAULT_IGNORE_PREVIEW).toString());

        prefs.put(AUTO_UPDATE, Boolean.valueOf(SchedulerConstants.DEFAULT_AUTO_UPDATE).toString());
        prefs.put(UPDATE_SCHEDULE, Integer.toString(SchedulerConstants.DEFAULT_SCHEDULE));
        prefs.put(UPDATE_ACTION, Integer.toString(SchedulerConstants.DEFAULT_ACTION));
        prefs.put(DAILY_TIME, Integer.toString(SchedulerConstants.DEFAULT_TIME_OF_DAY));
        prefs.put(DAY_OF_WEEK, Integer.toString(SchedulerConstants.DEFAULT_DAY_OF_WEEK));
        prefs.put(WEEKLY_TIME, Integer.toString(SchedulerConstants.DEFAULT_TIME_OF_DAY));
        prefs.put(DAY_OF_MONTH, Integer.toString(SchedulerConstants.DEFAULT_DAY_OF_MONTH));
        prefs.put(MONTHLY_TIME, Integer.toString(SchedulerConstants.DEFAULT_TIME_OF_DAY));
    }

}
