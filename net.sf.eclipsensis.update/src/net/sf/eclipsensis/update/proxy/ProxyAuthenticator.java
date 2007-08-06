/*******************************************************************************
 * Copyright (c) 2005-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.proxy;

import java.net.*;
import java.util.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;

import org.eclipse.core.runtime.*;

public class ProxyAuthenticator extends Authenticator
{
    private static final String USER = "user"; //$NON-NLS-1$
    private static final String PASSWORD = "password"; //$NON-NLS-1$
    private static final char[] NULL_PASSWORD = new char[0];

    private String mProxyHost;
    private int mProxyPort;
    private boolean mUseSaved = true;
    private boolean mNeedsSave = false;
    private AuthorizationInfo mAuthorizationInfo = new AuthorizationInfo();

    public ProxyAuthenticator(String proxyHost, int proxyPort)
    {
        mProxyHost = proxyHost;
        mProxyPort = proxyPort;
    }

    public void success()
    {
        synchronized (mAuthorizationInfo) {
            if (mNeedsSave) {
                Map map = new HashMap();
                map.put(USER, mAuthorizationInfo.userName);
                map.put(PASSWORD, mAuthorizationInfo.password);
                try {
                    Platform.addAuthorizationInfo(mAuthorizationInfo.url, mAuthorizationInfo.realm, mAuthorizationInfo.scheme, map);
                }
                catch (CoreException e) {
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                }
                mAuthorizationInfo = null;
                mNeedsSave = false;
            }
            mUseSaved = true;
        }
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        String host = getRequestingHost();
        int port = getRequestingPort();
        String protocol= getRequestingProtocol();
        InetAddress address = getRequestingSite();
        String realm = getRequestingPrompt();
        String scheme = getRequestingScheme();

        if (host == null && address != null) {
            host = address.getHostName();
        }
        if (host == null) {
            host = ""; //$NON-NLS-1$
        }
        else {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(protocol != null) {
                buf.append(protocol).append("://"); //$NON-NLS-1$
            }
            buf.append(host);
            if(port >= 0) {
                buf.append(":").append(port); //$NON-NLS-1$
            }
        }

        if (realm == null) {
            realm = ""; //$NON-NLS-1$
        }

        if (scheme == null) {
            scheme = ""; //$NON-NLS-1$
        }

        URL url;
        try {
            url = new URL(protocol, host, port,""); //$NON-NLS-1$

            PasswordAuthentication authentication = null;
            if(mProxyHost.equalsIgnoreCase(host) && mProxyPort == port) {
                if(mUseSaved) { //Use the saved only once. If it fails, we request the user again.
                    mUseSaved = false;
                    Map map = Platform.getAuthorizationInfo(url, realm, scheme);
                    if(map != null) {
                        String user;
                        try {
                            user = (String)map.get(USER);
                            if(user == null) {
                                user = ""; //$NON-NLS-1$
                            }
                        }
                        catch (Exception e) {
                            user = ""; //$NON-NLS-1$
                        }

                        char[] password;
                        try {
                            password = (char[])map.get(PASSWORD);
                            if(password == null) {
                                password = NULL_PASSWORD;
                            }
                        }
                        catch (Exception e) {
                            password = NULL_PASSWORD;
                        }

                        authentication = new PasswordAuthentication(user, password);
                    }
                }
            }
            if(authentication == null) {
                authentication = AuthenticationDialog.getAuthentication(url.toString(), realm);
                if (mProxyHost.equalsIgnoreCase(host) && mProxyPort == port && authentication != null) {
                    synchronized (mAuthorizationInfo) {
                        mNeedsSave = true;
                        mAuthorizationInfo.url= url;
                        mAuthorizationInfo.realm = realm;
                        mAuthorizationInfo.scheme = scheme;
                        mAuthorizationInfo.userName = authentication.getUserName();
                        mAuthorizationInfo.password = authentication.getPassword();
                    }
                }
            }
            return authentication;
        }
        catch (MalformedURLException e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
            return null;
        }
    }

    private class AuthorizationInfo
    {
        URL url;
        String realm;
        String scheme;
        String userName;
        char[] password;
    }
}
