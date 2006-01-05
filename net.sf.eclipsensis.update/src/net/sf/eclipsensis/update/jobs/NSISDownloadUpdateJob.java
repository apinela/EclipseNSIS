/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.MessageFormat;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

class NSISDownloadUpdateJob extends NSISHttpUpdateJob
{
    protected static final MessageFormat NSIS_SETUP_EXE = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("nsis.setup.format")); //$NON-NLS-1$
    protected static final String NSIS_DOWNLOAD_URL = EclipseNSISUpdatePlugin.getResourceString("download.url.prefix"); //$NON-NLS-1$
    protected static final File DOWNLOAD_FOLDER = EclipseNSISUpdatePlugin.getPluginStateLocation();
    protected static final MessageFormat INSTALL_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.update.prompt")); //$NON-NLS-1$

    private String mVersion;
    private String mFileName;
    
    NSISDownloadUpdateJob(String version, NSISUpdateJobSettings settings)
    {
        super(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.message")).format(new String[]{version}), settings); //$NON-NLS-1$
        mVersion = version;
        mFileName = NSIS_SETUP_EXE.format(new String[] {version});
    }
    
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated() && !getSettings().isInstall();
    }

    protected URL getURL() throws IOException
    {
        return new URL(NSIS_DOWNLOAD_URL+mFileName);
    }

    protected IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException
    {
        File setupExe = new File(DOWNLOAD_FOLDER,mFileName);
        if(!setupExe.exists()) {
            if(IOUtility.isValidFile(DOWNLOAD_FOLDER)) {
                DOWNLOAD_FOLDER.delete();
            }
            if(!IOUtility.isValidDirectory(DOWNLOAD_FOLDER)) {
                DOWNLOAD_FOLDER.mkdirs();
            }
            
            int length = conn.getContentLength();
            int bufsize = 8192;
            if(length <= 0) {
                monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            }
            else {
                monitor.beginTask(getName(), 101);
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            BufferedInputStream is = null;
            FileOutputStream os = null;
            try {
                is = new BufferedInputStream(conn.getInputStream());
                os = new FileOutputStream(setupExe);
                

                ByteBuffer buf = ByteBuffer.allocateDirect(bufsize);
                ReadableByteChannel channel = Channels.newChannel(is);
                FileChannel fileChannel = os.getChannel();

                int worked = 0;
                int totalread = 0;
                int numread = channel.read(buf);
                int n = 0;
                while(numread > 0) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    n++;
                    buf.flip();
                    fileChannel.write(buf);
                    
                    if(length > 0) {
                        totalread += numread;
                        int newWorked = Math.round(totalread*100/length);
                        monitor.worked(newWorked-worked);
                        worked = newWorked;
                    }

                    buf.rewind();
                    numread = channel.read(buf);
                }
                if(length > 0) {
                    monitor.worked(100-worked);
                }
                fileChannel.close();
                channel.close();
            }
            catch(Exception e) {
                if(setupExe.exists()) {
                    setupExe.delete();
                    IOException ioe;
                    if(e instanceof IOException) {
                        ioe = (IOException)e;
                    }
                    else {
                        ioe = (IOException)new IOException(e.getMessage()).initCause(e);
                    }
                    throw ioe;
                }
            }
            finally {
                IOUtility.closeIO(is);
                IOUtility.closeIO(os);
                if (monitor.isCanceled()) {
                    if(setupExe.exists()) {
                        setupExe.delete();
                    }
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
            }
        }
        
        if(setupExe.exists()) {
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            IStatus status = handleInstall(monitor, setupExe);
            if(!status.isOK()) {
                return status;
            }
        }
        return Status.OK_STATUS;
    }

    protected IStatus handleInstall(IProgressMonitor monitor, final File setupExe)
    {
        if(setupExe.exists()) {
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            displayExec(new Runnable() {
                public void run()
                {
                    NSISUpdateJobSettings settings = getSettings();
                    boolean automated = settings.isAutomated();
                    boolean install = settings.isInstall();
                    if(!install) {
                        automated = false;
                        install = Common.openQuestion(Display.getCurrent().getActiveShell(), 
                                    INSTALL_UPDATE_MESSAGEFORMAT.format(new String[] {mVersion}), 
                                    EclipseNSISUpdatePlugin.getShellImage());
                    }
                    if(install) {
                        settings = new NSISUpdateJobSettings(automated, install);
                        NSISUpdateJob job = new NSISInstallUpdateJob(mVersion, setupExe, settings);
                        job.schedule();
                    }
                }
            });
        }
        return Status.OK_STATUS;
    }

    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.error")).format(new String[] {mVersion,e.getMessage()}); //$NON-NLS-1$
    }
}
