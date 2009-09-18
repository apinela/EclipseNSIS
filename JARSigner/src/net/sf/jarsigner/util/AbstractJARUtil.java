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

import net.sf.eclipsensis.utilities.util.AbstractToolsUtility;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.launching.IVMInstall;

public abstract class AbstractJARUtil extends AbstractToolsUtility
{
    public static final String JAR_SIGNER_MAIN_CLASS_NAME = "sun.security.tools.JarSigner"; //$NON-NLS-1$
    protected String mKeyStore = ""; //$NON-NLS-1$
    private MessageFormat mArgsFormat;

    public AbstractJARUtil(IVMInstall vmInstall, String toolsJar, List<?> targetJars)
    {
        super(vmInstall, toolsJar, JAR_SIGNER_MAIN_CLASS_NAME, targetJars);
    }

    public void setKeyStore(String keyStore)
    {
        mKeyStore = maybeQuote(keyStore);
    }

    @Override
    protected String getProgramArguments(Object target)
    {
        return mArgsFormat.format(new String[]{maybeQuote(((IFile)target).getLocation().toOSString())});
    }

    @Override
    public IStatus run(IProgressMonitor monitor)
    {
        mArgsFormat = createArgsFormat();
        return super.run(monitor);
    }

    @Override
    protected Plugin getPlugin()
    {
        return JARSignerPlugin.getDefault();
    }

    protected abstract MessageFormat createArgsFormat();
}