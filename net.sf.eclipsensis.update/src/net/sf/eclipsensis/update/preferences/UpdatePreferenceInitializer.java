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

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

import org.eclipse.core.runtime.preferences.*;

public class UpdatePreferenceInitializer extends AbstractPreferenceInitializer implements IUpdatePreferenceConstants
{
    public void initializeDefaultPreferences()
    {
        IEclipsePreferences prefs = new DefaultScope().getNode(EclipseNSISUpdatePlugin.getDefault().getPluginId());
        prefs.put(AUTO_UPDATE, Boolean.FALSE.toString());
        prefs.put(UPDATE_SCHEDULE, Integer.toString(SchedulerConstants.SCHEDULE_ON_STARTUP));
        prefs.put(UPDATE_ACTION, Integer.toString(SchedulerConstants.UPDATE_NOTIFY));
        prefs.put(IGNORE_PREVIEW, Boolean.FALSE.toString());
        String zero = Integer.toString(0);
        prefs.put(DAILY_TIME, zero);
        prefs.put(DAY_OF_WEEK, zero);
        prefs.put(WEEKLY_TIME, zero);
        prefs.put(DAY_OF_MONTH, zero);
        prefs.put(MONTHLY_TIME, zero);
    }

}
