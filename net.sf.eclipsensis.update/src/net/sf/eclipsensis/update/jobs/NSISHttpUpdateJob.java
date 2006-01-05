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

import java.io.IOException;
import java.net.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.proxy.ProxyAuthenticator;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.SiteManager;

public abstract class NSISHttpUpdateJob extends NSISUpdateJob
{
    protected static final String HTTP_PROXY_PORT = "http.proxyPort"; //$NON-NLS-1$
    protected static final String HTTP_PROXY_HOST = "http.proxyHost"; //$NON-NLS-1$
    protected static final String HTTP_PROXY_SET = "http.proxySet"; //$NON-NLS-1$
    protected NSISHttpUpdateJob(String name, NSISUpdateJobSettings settings)
    {
        super(name, settings);
    }

    protected final IStatus doRun(IProgressMonitor monitor)
    {
        monitor.beginTask(getName(), 103);
        try {
            URL url = null;
            try {
                url = getURL();
            }
            catch (IOException e) {
                handleException(e);
                return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
            }
            if (url != null) {
                String oldProxySet = System.getProperty(HTTP_PROXY_SET);
                String oldProxyHost = System.getProperty(HTTP_PROXY_HOST);
                String oldProxyPort = System.getProperty(HTTP_PROXY_PORT);
                boolean isUsingProxy = SiteManager.isHttpProxyEnable();
                String proxyHost = SiteManager.getHttpProxyServer();
                int proxyPort;
                try {
                    proxyPort = Integer.parseInt(SiteManager.getHttpProxyPort());
                }
                catch (Exception e) {
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
                            conn = (HttpURLConnection)url.openConnection();
                            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,100);
                            IStatus status = handleConnection(conn, subMonitor);
                            if(!status.isOK()) {
                                return status;
                            }
                        }
                        finally {
                            if (conn != null) {
                                conn.disconnect();
                            }
                            monitor.worked(1);
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
                        monitor.worked(1);
                    }
                }
                catch (Exception e) {
                    handleException(e);
                }
                finally {
                    if (isUsingProxy) {
                        setSystemProperty(HTTP_PROXY_SET, oldProxySet);
                        setSystemProperty(HTTP_PROXY_HOST, oldProxyHost);
                        setSystemProperty(HTTP_PROXY_PORT, oldProxyPort);
                    }
                    monitor.worked(1);
                }
            }
            return Status.OK_STATUS;
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

    protected abstract URL getURL() throws IOException;
    protected abstract IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException;
}
