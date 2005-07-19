/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.util;

import java.text.MessageFormat;
import java.util.List;

import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.core.resources.IFile;

public class JARVerifier extends AbstractJARUtil 
{
    private boolean mCerts = false;
    
    public JARVerifier(String toolsJar, List targetJars)
    {
        super(toolsJar, targetJars);
    }

    public void setCerts(boolean internalSF)
    {
        mCerts = internalSF;
    }

    protected String getSuccessMessage(IFile targetJar)
    {
        return JARSignerPlugin.getFormattedString("jar.verified.message",new Object[]{targetJar});
    }

    protected String getFailMessage(IFile targetJar)
    {
        return JARSignerPlugin.getFormattedString("jar.not.verified.message",new Object[]{targetJar});
    }

    protected String getConsoleTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.console.title");
    }

    protected String getLaunchTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.launch.title");
    }

    protected MessageFormat createArgsFormat()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        buf.append("-verify"); //$NON-NLS-1$
        if(mVerbose) {
            buf.append(" -verbose"); //$NON-NLS-1$
            if(mCerts) {
                buf.append(" -certs"); //$NON-NLS-1$
            }
            if(!JARSignerPlugin.isEmpty(mKeyStore)) {
                buf.append(" -keystore ").append(mKeyStore); //$NON-NLS-1$
            }
        }

        buf.append(" {0}"); //$NON-NLS-1$
        return new MessageFormat(buf.toString());
    }
}