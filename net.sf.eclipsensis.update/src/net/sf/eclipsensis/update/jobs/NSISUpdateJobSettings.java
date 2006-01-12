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

public class NSISUpdateJobSettings
{
    private boolean mAutomated = false;
    private boolean mDownload = false;
    private boolean mInstall = false;
    private boolean mIgnorePreview = false;
    public static final Object JOB_FAMILY = new Object();

    public NSISUpdateJobSettings()
    {
        this(false,false,false,false);
    }

    public NSISUpdateJobSettings(boolean automated, boolean download, boolean install, boolean ignorePreview)
    {
        this(automated, download, install);
        mIgnorePreview = ignorePreview;
    }

    public NSISUpdateJobSettings(boolean automated, boolean download, boolean install)
    {
        this(automated, install);
        mDownload = download;
    }

    public NSISUpdateJobSettings(boolean automated, boolean install)
    {
        mAutomated = automated;
        mInstall = install;
    }

    public boolean isAutomated()
    {
        return mAutomated;
    }

    public boolean isDownload()
    {
        return mDownload;
    }
    
    public boolean isInstall()
    {
        return mInstall;
    }

    public boolean isIgnorePreview()
    {
        return mIgnorePreview;
    }
}
