/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

public class NSISCheckUpdateJob extends NSISHttpUpdateJob
{
    protected static final String NO_UPDATE = "0"; //$NON-NLS-1$
    protected static final String RELEASE_UPDATE = "1"; //$NON-NLS-1$
    protected static final String PREVIEW_UPDATE = "2"; //$NON-NLS-1$

    protected static final MessageFormat RELEASE_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("release.update.message")); //$NON-NLS-1$
    protected static final MessageFormat PREVIEW_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("preview.update.message")); //$NON-NLS-1$
    
    public NSISCheckUpdateJob()
    {
        this(new NSISUpdateJobSettings());
    }

    public NSISCheckUpdateJob(NSISUpdateJobSettings settings)
    {
        this(settings, null); 
    }

    public NSISCheckUpdateJob(NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(EclipseNSISUpdatePlugin.getResourceString("check.update.message"), settings, jobRunner); //$NON-NLS-1$
    }

    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated() && ((getSettings().getAction() & SchedulerConstants.UPDATE_DOWNLOAD) == 0);
    }

    protected URL getURL() throws IOException
    {
        Version version = NSISPreferences.INSTANCE.getNSISVersion();
        if(version != null) {
            if(!NSISValidator.isCVSVersion(version)) {
                String site = cPreferenceStore.getString(IUpdatePreferenceConstants.NSIS_UPDATE_SITE);
                if(!Common.isEmpty(site)) {
                    return NSISUpdateURLs.getUpdateURL(site, version.toString());
                }
            }
            else if(!getSettings().isAutomated()) {
                displayExec(new Runnable() {
                    public void run()
                    {
                        Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"),  //$NON-NLS-1$
                                EclipseNSISUpdatePlugin.getResourceString("update.cvs.version.message"), EclipseNSISUpdatePlugin.getShellImage()); //$NON-NLS-1$
                    }
                });
            }
        }
        return null;
    }

    protected URL getAlternateURL() throws IOException
    {
        Version version = NSISPreferences.INSTANCE.getNSISVersion();
        if(version != null) {
            if(!NSISValidator.isCVSVersion(version)) {
                return NSISUpdateURLs.getUpdateURL(version.toString());
            }
        }
        return null;
    }

    protected IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException
    {
        monitor.beginTask(getName(), 2);
        InputStream is = null;
        String type = null;
        String version = ""; //$NON-NLS-1$
        try {
            is = conn.getInputStream();
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            if(line != null) {
                String[] tokens = Common.tokenize(line, '|');
                if(tokens.length > 0) {
                    type = tokens[0];
                    if(tokens.length > 1) {
                        version = tokens[1];
                    }
                }
            }
        }
        finally {
            IOUtility.closeIO(is);
        }
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);
        IStatus status = handleDownload(monitor, type, version);
        if(status.isOK()) {
            monitor.worked(1);
            monitor.done();
            return Status.OK_STATUS;
        }
        else {
            return status;
        }
    }
    
    protected IStatus handleDownload(IProgressMonitor monitor, final String type, final String version)
    {
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        if((RELEASE_UPDATE.equals(type) || (PREVIEW_UPDATE.equals(type) && !getSettings().isIgnorePreview()))) {
            displayExec(new Runnable() {
                public void run()
                {
                    MessageFormat mf;
                    if(RELEASE_UPDATE.equals(type)) {
                        mf = RELEASE_UPDATE_MESSAGEFORMAT;
                    }
                    else if (PREVIEW_UPDATE.equals(type)) {
                        mf = PREVIEW_UPDATE_MESSAGEFORMAT;
                    }
                    else {
                        return;
                    }
                    NSISUpdateJobSettings settings = getSettings();
                    boolean automated = settings.isAutomated();
                    boolean download = ((settings.getAction() & SchedulerConstants.UPDATE_DOWNLOAD) == SchedulerConstants.UPDATE_DOWNLOAD);
                    if(!download) {
                        automated = false;
                        download = Common.openQuestion(Display.getCurrent().getActiveShell(), 
                                            mf.format(new String[] {version}), 
                                            EclipseNSISUpdatePlugin.getShellImage());
                    }
                    if(download) {
                        settings = new NSISUpdateJobSettings(automated,settings.getAction());
                        INSISUpdateJobRunner jobRunner = getJobRunner();
                        NSISUpdateJob job = new NSISDownloadUpdateJob(version, settings, jobRunner);
                        if(jobRunner == null) {
                            job.schedule();
                        }
                        else {
                            jobRunner.run(job);
                        }
                    }
                }
            });
        }
        else {
            if(!getSettings().isAutomated()) {
                displayExec(new Runnable() {
                    public void run()
                    {
                        Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"),  //$NON-NLS-1$
                                EclipseNSISUpdatePlugin.getResourceString("no.update.message"), EclipseNSISUpdatePlugin.getShellImage()); //$NON-NLS-1$
                    }
                });
            }
        }

        return Status.OK_STATUS;
    }

    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("check.update.error")).format(new String[]{e.getMessage()}); //$NON-NLS-1$
    }
}
