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
import org.eclipse.core.resources.IResource;

public class JARSigner extends AbstractJARUtil 
{
    private String mStorePass = ""; //$NON-NLS-1$
    private String mAlias = ""; //$NON-NLS-1$
    private String mStoreType = ""; //$NON-NLS-1$
    private String mKeyPass = ""; //$NON-NLS-1$
    private String mSigFile = ""; //$NON-NLS-1$
    private String mSignedJar = ""; //$NON-NLS-1$
    private boolean mInternalSF = false;
    private boolean mSectionsOnly = false;
    
    public JARSigner(String toolsJar, List targetJars, String keyStore, String storePass, String alias)
    {
        super(toolsJar,targetJars);
        setKeyStore(keyStore);
        mStorePass = maybeQuote(storePass);
        mAlias = maybeQuote(alias);
    }

    public void setInternalSF(boolean internalSF)
    {
        mInternalSF = internalSF;
    }

    public void setKeyPass(String keyPass)
    {
        mKeyPass = keyPass;
    }

    public void setSectionsOnly(boolean sectionsOnly)
    {
        mSectionsOnly = sectionsOnly;
    }

    public void setSigFile(String sigFile)
    {
        mSigFile = maybeQuote(sigFile);
    }

    public void setSignedJar(String signedJar)
    {
        mSignedJar = maybeQuote(signedJar);
    }

    public void setStoreType(String storeType)
    {
        mStoreType = maybeQuote(storeType);
    }

    protected String getSuccessMessage(IFile targetJar)
    {
        return JARSignerPlugin.getFormattedString("jar.signed.message",new Object[]{targetJar});
    }

    protected String getFailMessage(IFile targetJar)
    {
        return JARSignerPlugin.getFormattedString("jar.not.signed.message",new Object[]{targetJar});
    }

    protected String getConsoleTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.console.title");
    }

    protected String getLaunchTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.launch.title");
    }

    protected MessageFormat createArgsFormat()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        buf.append("-keystore ").append(mKeyStore); //$NON-NLS-1$
        buf.append(" -storepass ").append(mStorePass); //$NON-NLS-1$
        if(!JARSignerPlugin.isEmpty(mStoreType)) {
            buf.append(" -storetype ").append(mStoreType); //$NON-NLS-1$
        }
        if(!JARSignerPlugin.isEmpty(mKeyPass)) {
            buf.append(" -keypass ").append(mKeyPass); //$NON-NLS-1$
        }
        if(!JARSignerPlugin.isEmpty(mSigFile)) {
            buf.append(" -sigfile ").append(mSigFile); //$NON-NLS-1$
        }
        if(!JARSignerPlugin.isEmpty(mSignedJar)) {
            buf.append(" -signedjar ").append(mSignedJar); //$NON-NLS-1$
        }
        if(mVerbose) {
            buf.append(" -verbose"); //$NON-NLS-1$
        }
        if(mInternalSF) {
            buf.append(" -internalsf"); //$NON-NLS-1$
        }
        if(mSectionsOnly) {
            buf.append(" -sectionsonly"); //$NON-NLS-1$
        }

        buf.append(" {0} ").append(mAlias); //$NON-NLS-1$
        return new MessageFormat(buf.toString());
    }

    protected void postJAR(IFile targetJar) throws Exception
    {
        if(JARSignerPlugin.isEmpty(mSignedJar)) {
            targetJar.refreshLocal(IResource.DEPTH_ONE,null);
        }
    }
}