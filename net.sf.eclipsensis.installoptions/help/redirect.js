<!--
//###############################################################################
//# Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
//# All rights reserved.
//# This program is made available under the terms of the Common Public License
//# v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
//#
//# Contributors:
//# Sunil Kamath (IcemanK) - initial API and implementation
//###############################################################################

function getPrefix()
{
    var prefix;
    var parts;
    var path;
    
    path = location.pathname;
    if(path.charAt(0) == '/') {
        path = path.substr(1);
    }
    
    parts = path.split('/');
    if(parts.length >= 2) {
        prefix = '/'+parts[0]+'/'+parts[1];
    }
    else {
        prefix = '/help/topic';
    }
    return prefix;    
}

function prependSlash(url)
{
    if(url && url.charAt(0) != '/') {
        url = "/" + url;
    }
    return url;
}

function redirectEclipse(url)
{
    document.location = getPrefix() + prependSlash(url);
}

function _redirectNSIS(path, url)
{
    document.location = getPrefix() + prependSlash(path) + prependSlash(url);
}

function redirectNSIS(url)
{
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/Docs", url);
}

function redirectNSISContrib(url)
{
    if(nsisContribPath != null) {
        _redirectNSIS(nsisContribPath, url);
        return;
    }
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/Contrib", url);
}

function redirectNSISKeyword(keyword)
{
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/keyword/", url);
}
//-->
