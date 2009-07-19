/*******************************************************************************
 * Copyright (c) 2005-2009 Sunil Kamath (IcemanK).
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

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jdt.launching.IVMInstall;

public class JARVerifier extends AbstractJARUtil
{
    private boolean mCerts = false;

    public JARVerifier(IVMInstall vmInstall, String toolsJar, List<?> targetJars)
    {
        super(vmInstall, toolsJar, targetJars);
    }

    public void setCerts(boolean internalSF)
    {
        mCerts = internalSF;
    }

    @Override
	protected String getSuccessMessage(Object target)
    {
        return JARSignerPlugin.getFormattedString("jar.verified.message",new Object[]{target}); //$NON-NLS-1$
    }

    @Override
	protected String getFailMessage(Object target)
    {
        return JARSignerPlugin.getFormattedString("jar.not.verified.message",new Object[]{target}); //$NON-NLS-1$
    }

    @Override
	protected String getCancelMessage()
    {
        return JARSignerPlugin.getResourceString("jarverifier.cancel.message"); //$NON-NLS-1$
    }

    @Override
	protected String getTaskName()
    {
        return JARSignerPlugin.getResourceString("jarverifier.task.name"); //$NON-NLS-1$
    }

    @Override
	protected String getSubTaskName(Object target)
    {
        return JARSignerPlugin.getFormattedString("jarverifier.subtask.name", new Object[]{target}); //$NON-NLS-1$
    }

    @Override
	protected String getConsoleTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.console.title"); //$NON-NLS-1$
    }

    @Override
	protected String getLaunchTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.launch.title"); //$NON-NLS-1$
    }

    @Override
	protected MessageFormat createArgsFormat()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        buf.append("-verify"); //$NON-NLS-1$
        if(mVerbose) {
            buf.append(" -verbose"); //$NON-NLS-1$
            if(mCerts) {
                buf.append(" -certs"); //$NON-NLS-1$
            }
            if(!Common.isEmpty(mKeyStore)) {
                buf.append(" -keystore ").append(mKeyStore); //$NON-NLS-1$
            }
        }

        buf.append(" {0}"); //$NON-NLS-1$
        return new MessageFormat(buf.toString());
    }
}