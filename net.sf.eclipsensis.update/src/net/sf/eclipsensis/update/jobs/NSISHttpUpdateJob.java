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

import java.io.IOException;
import java.net.*;
import java.text.MessageFormat;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.proxy.ProxyAuthenticator;
import net.sf.eclipsensis.util.NestedProgressMonitor;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;

public abstract class NSISHttpUpdateJob extends NSISUpdateJob
{
    protected static final String HTTP_PROXY_PORT = "http.proxyPort"; //$NON-NLS-1$
    protected static final String HTTP_PROXY_HOST = "http.proxyHost"; //$NON-NLS-1$
    protected static final String HTTP_PROXY_SET = "http.proxySet"; //$NON-NLS-1$

    protected static final IPreferenceStore cPreferenceStore = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
    
    private MessageFormat mConnectionFormat = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("http.connect.message"));
    private INSISUpdateJobRunner mJobRunner = null;
    
    protected NSISHttpUpdateJob(String name, NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(name, settings);
        mJobRunner = jobRunner;
    }

    public INSISUpdateJobRunner getJobRunner()
    {
        return mJobRunner;
    }

    protected final IStatus doRun(IProgressMonitor monitor)
    {
        monitor.beginTask(getName(), 120);
        try {
            URL url = null;
            URL defaultUrl = null;
            try {
                url = getURL();
            }
            catch (IOException e) {
                handleException(e);
                return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
            }
            try {
                defaultUrl = getDefaultURL();
            }
            catch (IOException e) {
                handleException(e);
                return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
            }
            if(url == null) {
                url = defaultUrl;
                defaultUrl = null;
            }
            else if(defaultUrl != null && url.toString().equals(defaultUrl.toString())) {
                defaultUrl = null;
            }
            
            if (url != null || defaultUrl != null) {
                String oldProxySet = System.getProperty(HTTP_PROXY_SET);
                String oldProxyHost = System.getProperty(HTTP_PROXY_HOST);
                String oldProxyPort = System.getProperty(HTTP_PROXY_PORT);
                boolean isUsingProxy = cPreferenceStore.getBoolean(IUpdatePreferenceConstants.USE_HTTP_PROXY);
                String proxyHost = cPreferenceStore.getString(IUpdatePreferenceConstants.HTTP_PROXY_HOST);
                int proxyPort = cPreferenceStore.getInt(IUpdatePreferenceConstants.HTTP_PROXY_PORT);
                if (proxyPort < 1 || proxyPort > 0xFFFF) {
                    proxyPort = 80; //Default HTTP port
                }
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                if (isUsingProxy) {
                    System.setProperty(HTTP_PROXY_SET, Boolean.TRUE.toString());
                    System.setProperty(HTTP_PROXY_HOST, proxyHost);
                    System.setProperty(HTTP_PROXY_PORT, Integer.toString(proxyPort));
                }
                try {
                    ProxyAuthenticator auth = null;
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    Authenticator defaultAuthenticator = null;
                    if (isUsingProxy) {
                        defaultAuthenticator = WinAPI.getDefaultAuthenticator();

                        auth = new ProxyAuthenticator(proxyHost, proxyPort);
                        Authenticator.setDefault(auth);
                    }
                    try {
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        HttpURLConnection conn = null;
                        try {
                            IProgressMonitor subMonitor = new NestedProgressMonitor(monitor,getName(),5);
                            conn = makeConnection(subMonitor, url, defaultUrl);
                            if(monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }
                            subMonitor = new NestedProgressMonitor(monitor,getName(),100);
                            IStatus status = handleConnection(conn, subMonitor);
                            if(!status.isOK()) {
                                return status;
                            }
                        }
                        finally {
                            if (conn != null) {
                                conn.disconnect();
                            }
                            monitor.worked(5);
                        }
                        if (isUsingProxy) {
                            auth.success(); // This means success. So save the authentication.
                        }
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                    }
                    finally {
                        if (isUsingProxy) {
                            Authenticator.setDefault(defaultAuthenticator);
                        }
                        monitor.worked(5);
                    }
                }
                catch (Exception e) {
                    handleException(e);
                    return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
                }
                finally {
                    if (isUsingProxy) {
                        setSystemProperty(HTTP_PROXY_SET, oldProxySet);
                        setSystemProperty(HTTP_PROXY_HOST, oldProxyHost);
                        setSystemProperty(HTTP_PROXY_PORT, oldProxyPort);
                    }
                    monitor.worked(5);
                }
            }
            return Status.OK_STATUS;
        }
        finally {
            monitor.done();
        }        
    }

    /**
     * @param monitor
     * @param url
     * @param defaultURL
     * @param conn
     * @return
     * @throws IOException
     */
    protected HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        try {
            monitor.beginTask(mConnectionFormat.format(new String[] {getName(),url.getHost()}),100);
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
                    monitor.setTaskName(mConnectionFormat.format(new String[] {url.getHost()}));
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

    protected final void setSystemProperty(String name, String value)
    {
        if (value == null) {
            try {
                System.getProperties().remove(name);
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
            }        
        }
        else {
            System.setProperty(name, value);
        }
    }

    protected URL getDefaultURL() throws IOException
    {
        if(false) {
            throw new IOException();
        }
        return null;
    }

    protected abstract URL getURL() throws IOException;
    protected abstract IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException;
}
