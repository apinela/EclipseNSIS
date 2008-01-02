/*******************************************************************************
 * Copyright (c) 2005-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class NetworkUtil
{
    private static final String PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP = "imageCacheRefreshTimestamp"; //$NON-NLS-1$
    private static final int DOWNLOAD_BUFFER_SIZE = 32768;
    private static MessageFormat cConnectionFormat = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("http.connect.message")); //$NON-NLS-1$
    private static Map cImageCache = new HashMap();
    private static long cImageCacheRefreshTimestamp = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().getLong(PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP);
    private static final File IMAGE_CACHE_FOLDER = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),"imageCache"); //$NON-NLS-1$

    private NetworkUtil()
    {
    }

    public static HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        try {
            monitor.beginTask(cConnectionFormat.format(new String[] {url.getHost()}),100);
            HttpURLConnection conn = null;
            int responseCode;
            try {
                conn = (HttpURLConnection)url.openConnection();
                responseCode = conn.getResponseCode();
            }
            catch (IOException e) {
                if(defaultURL != null) {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
                else {
                    throw e;
                }
            }
            if(responseCode >= 400) {
                if(defaultURL != null) {
                    monitor.worked(50);
                    url = defaultURL;
                    monitor.setTaskName(cConnectionFormat.format(new String[] {url.getHost()}));
                    conn = (HttpURLConnection)url.openConnection();
                    responseCode = conn.getResponseCode();
                }
                if(responseCode >= 400) {
                    throw new IOException(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("http.error")).format(new Object[] {new Integer(responseCode)})); //$NON-NLS-1$
                }
            }
            return conn;
        }
        finally {
            monitor.done();
        }
    }

    public static IStatus download(HttpURLConnection conn, IProgressMonitor monitor, String name, OutputStream os) throws IOException
    {
        int length = 0;
        String[] args = null;

        MessageFormat mf = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.progress.format")); //$NON-NLS-1$
        if(monitor != null) {
            length = conn.getContentLength();
            if(length <= 0) {
                monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
            }
            else {
                args = new String[] {"0"}; //$NON-NLS-1$
                monitor.beginTask(mf.format(args), 101);
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
        }
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(conn.getInputStream());

            ByteBuffer buf = ByteBuffer.allocateDirect(DOWNLOAD_BUFFER_SIZE);
            ReadableByteChannel channel = Channels.newChannel(is);
            WritableByteChannel fileChannel = Channels.newChannel(os);
            int worked = 0;
            int totalread = 0;
            int numread = channel.read(buf);
            while(numread >= 0) {
                if (monitor != null && monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                totalread += numread;
                if(buf.position() >= buf.limit()) {
                    buf.flip();
                    fileChannel.write(buf);

                    if(monitor != null && length > 0) {
                        int newWorked = Math.round(totalread*100/length);
                        args[0]=Integer.toString(newWorked);
                        monitor.setTaskName(mf.format(args));
                        monitor.worked(newWorked-worked);
                        worked = newWorked;
                    }

                    buf.rewind();
                }
                numread = channel.read(buf);
            }
            if(buf.position() > 0) {
                buf.flip();
                fileChannel.write(buf);
            }
            if(monitor != null && length > 0) {
                args[0]="100"; //$NON-NLS-1$
                monitor.setTaskName(mf.format(args));
                monitor.worked(100-worked);
            }
            fileChannel.close();
            channel.close();
        }
        finally {
            IOUtility.closeIO(is);
            IOUtility.closeIO(os);
            if (monitor != null) {
                if(monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
            }
        }

        return Status.OK_STATUS;
    }

    public static String getContent(HttpURLConnection conn) throws IOException
    {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(DOWNLOAD_BUFFER_SIZE);
            NetworkUtil.download(conn, null, null, os);
        }
        catch(Exception e) {
            IOException ioe;
            if(e instanceof IOException) {
                ioe = (IOException)e;
            }
            else {
                ioe = (IOException)new IOException(e.getMessage()).initCause(e);
            }
            throw ioe;
        }
        finally {
            IOUtility.closeIO(os);
        }

        String content;
        try {
            content = os.toString(conn.getContentEncoding());
        }
        catch(Exception ex) {
            content = os.toString();
        }
        return content;
    }

    public static boolean downloadLatest(URL url, File targetFile)
    {
        boolean ok = true;
        boolean downloaded = false;
        FileOutputStream fos = null;
        HttpURLConnection conn2 = null;
        long timestamp = 0;
        try {
            conn2 = makeConnection(new NullProgressMonitor(), url, null);
            timestamp = conn2.getLastModified();
            if (timestamp > (targetFile.exists()?targetFile.lastModified():0)) {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                ok = false;
                fos = new FileOutputStream(targetFile);
                NetworkUtil.download(conn2, null, null, fos);
                ok = true;
                downloaded = true;
            }
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            ok = false;
        }
        finally {
            IOUtility.closeIO(fos);
            if (downloaded) {
                targetFile.setLastModified(timestamp);
            }
            if (conn2 != null) {
                conn2.disconnect();
            }
        }
        return ok;
    }

    public static List getDownloadSites(IProgressMonitor monitor, String taskName, String parentTaskName)
    {
        try {
            monitor.beginTask(taskName, 100);
            List downloadSites = new ArrayList();
            HttpURLConnection conn2 = null;
            String content = null;
            try {
                conn2 = makeConnection(new NestedProgressMonitor(monitor,taskName,parentTaskName,25), NSISUpdateURLs.getSelectDownloadURL(), null);
                content = getContent(conn2);
                monitor.worked(25);
            }
            catch(IOException ioe) {
                return null;
            }
            finally {
                if (conn2 != null) {
                    conn2.disconnect();
                }
            }
            if(content != null) {
                //BIG HACK - Replace with proper lenient XHTML parser if available
                //HTML Parser does not like XHTML
                int n = content.indexOf("<html"); //$NON-NLS-1$
                if(n >= 0) {
                    content = content.substring(n);
                }
                content = content.replaceAll("\\s*/>",">"); //$NON-NLS-1$ //$NON-NLS-2$

                ParserDelegator parserDelegator = new ParserDelegator();
                DownloadURLsParserCallback callback = new DownloadURLsParserCallback();
                try {
                    parserDelegator.parse(new StringReader(content), callback, true);
                }
                catch (IOException e1) {
                    return null;
                }
                List sites = callback.getSites();
                if(sites.size() > 0) {
                    long now = System.currentTimeMillis();
                    boolean refreshImageCache = false;
                    if(now - cImageCacheRefreshTimestamp > 86400000) {
                        //Refresh once a day
                        refreshImageCache = true;
                        cImageCacheRefreshTimestamp = now;
                        EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP, cImageCacheRefreshTimestamp);
                    }
                    if(!IOUtility.isValidDirectory(IMAGE_CACHE_FOLDER)) {
                        IMAGE_CACHE_FOLDER.mkdirs();
                    }
                    int count=0;
                    for (Iterator iter = sites.iterator(); iter.hasNext();) {
                        String[] element = (String[])iter.next();
                        if(element != null && element.length == 4) {
                            Image image = null;
                            try {
                                if (!Common.isEmpty(element[0])) {
                                    URL imageURL = new URL(element[0]);
                                    String path = imageURL.getPath();
                                    n = path.lastIndexOf("/"); //$NON-NLS-1$
                                    if (n >= 0) {
                                        path = path.substring(n + 1);
                                    }
                                    if (!Common.isEmpty(path)) {
                                        File imageFile = new File(IMAGE_CACHE_FOLDER, path);
                                        if (!imageFile.exists() || refreshImageCache) {
                                            NetworkUtil.downloadLatest(imageURL, imageFile);
                                            if (!imageFile.exists()) {
                                                cImageCache.remove(imageFile);
                                            }
                                        }
                                        image = (Image)cImageCache.get(imageFile);
                                        if (image == null) {
                                            if (imageFile.exists()) {
                                                final ImageData imageData = new ImageData(imageFile.getAbsolutePath());
                                                final Image[] imageArray = new Image[1];
                                                Display.getDefault().syncExec(new Runnable() {
                                                    public void run()
                                                    {
                                                        try {
                                                            imageArray[0] = new Image(Display.getDefault(), imageData);
                                                        }
                                                        catch (Exception ex) {
                                                            imageArray[0] = null;
                                                        }
                                                    }
                                                });
                                                image = imageArray[0];
                                                if (image != null) {
                                                    cImageCache.put(imageFile, image);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                EclipseNSISUpdatePlugin.getDefault().log(IStatus.WARNING,e);
                            }
                            downloadSites.add(new DownloadSite(image, element[1], element[2], element[3]));
                        }
                        monitor.worked(50*(++count/sites.size()));
                    }
                }
            }

            return downloadSites;
        }
        finally {
            monitor.done();
        }
    }
}
