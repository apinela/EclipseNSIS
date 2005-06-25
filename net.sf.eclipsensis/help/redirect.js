<!--
<!--
//###############################################################################
//# Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
//# All rights reserved.
//# This program is made available under the terms of the Common Public License
//# v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
//#
//# Contributors:
//# Sunil Kamath (IcemanK) - initial API and implementation
//###############################################################################

function prependSlash(url)
{
    if(url && url.charAt(0) != '/') {
        url = "/" + url;
    }
    return url;
}

function redirectEclipse(url)
{
    document.location = "/help/topic" + prependSlash(url);
}

function redirectNSIS(url)
{
    document.location = "/help/topic/net.sf.eclipsensis/help/NSIS/Docs"+prependSlash(url);
}
//-->
