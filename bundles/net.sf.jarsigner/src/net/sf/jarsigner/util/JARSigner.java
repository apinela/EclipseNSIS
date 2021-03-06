/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.launching.IVMInstall;

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

    private boolean mUseTimestamping = false;
    private String mTSA = null;
    private String mTSACert = null;

    private boolean mUseAltSigning = false;
    private String mAltSigner = null;
    private String mAltSignerPath = null;

    public JARSigner(IVMInstall vmInstall, String toolsJar, List<?> targetJars, String keyStore, String storePass, String alias)
    {
        super(vmInstall, toolsJar,targetJars);
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

    public void setUseTimestamping(boolean useTimestamping)
    {
        mUseTimestamping = useTimestamping;
    }

    public void setTSA(String tsa)
    {
        mTSA = maybeQuote(tsa);
    }

    public void setTSACert(String tsaCert)
    {
        mTSACert = maybeQuote(tsaCert);
    }

    public void setUseAltSigning(boolean useAltSigning)
    {
        mUseAltSigning = useAltSigning;
    }

    public void setAltSigner(String altSigner)
    {
        mAltSigner = altSigner;
    }

    public void setAltSignerPath(String altSignerPath)
    {
        mAltSignerPath = maybeQuote(altSignerPath);
    }

    @Override
    protected String getSuccessMessage(Object target)
    {
        return JARSignerPlugin.getFormattedString("jar.signed.message",new Object[]{target}); //$NON-NLS-1$
    }

    @Override
    protected String getFailMessage(Object target)
    {
        return JARSignerPlugin.getFormattedString("jar.not.signed.message",new Object[]{target}); //$NON-NLS-1$
    }

    @Override
    protected String getConsoleTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.console.title"); //$NON-NLS-1$
    }

    @Override
    protected String getLaunchTitle()
    {
        return JARSignerPlugin.getResourceString("jarsigner.launch.title"); //$NON-NLS-1$
    }

    @Override
    protected MessageFormat createArgsFormat()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        buf.append("-keystore ").append(mKeyStore); //$NON-NLS-1$
        buf.append(" -storepass ").append(mStorePass); //$NON-NLS-1$
        if(!Common.isEmpty(mStoreType)) {
            buf.append(" -storetype ").append(mStoreType); //$NON-NLS-1$
        }
        if(!Common.isEmpty(mKeyPass)) {
            buf.append(" -keypass ").append(mKeyPass); //$NON-NLS-1$
        }
        if(!Common.isEmpty(mSigFile)) {
            buf.append(" -sigfile ").append(mSigFile); //$NON-NLS-1$
        }
        if(!Common.isEmpty(mSignedJar)) {
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

        if(mUseTimestamping)
        {
            if(!Common.isEmpty(mTSA))
            {
                buf.append(" -tsa ").append(mTSA); //$NON-NLS-1$
            }
            else if(!Common.isEmpty(mTSACert))
            {
                buf.append(" -tsacert ").append(mTSACert); //$NON-NLS-1$
            }
            else
            {
                throw new IllegalArgumentException(JARSignerPlugin.getResourceString("missing.tsa.info")); //$NON-NLS-1$
            }
        }

        if(mUseAltSigning)
        {
            if(!Common.isEmpty(mAltSigner))
            {
                buf.append(" -altsigner ").append(mAltSigner); //$NON-NLS-1$
            }
            if(!Common.isEmpty(mAltSignerPath))
            {
                buf.append(" -altsignerpath ").append(mAltSignerPath); //$NON-NLS-1$
            }
            else
            {
                throw new IllegalArgumentException(JARSignerPlugin.getResourceString("missing.signer.class.name")); //$NON-NLS-1$
            }
        }

        buf.append(" {0} ").append(mAlias); //$NON-NLS-1$
        return new MessageFormat(buf.toString());
    }

    @Override
    protected String getCancelMessage()
    {
        return JARSignerPlugin.getResourceString("jarsigner.cancel.message"); //$NON-NLS-1$
    }

    @Override
    protected String getTaskName()
    {
        return JARSignerPlugin.getResourceString("jarsigner.task.name"); //$NON-NLS-1$
    }

    @Override
    protected String getSubTaskName(Object target)
    {
        return JARSignerPlugin.getFormattedString("jarsigner.subtask.name", new Object[]{target}); //$NON-NLS-1$
    }

    @Override
    protected IStatus postProcess(Object target, IProgressMonitor monitor)
    {
        if(Common.isEmpty(mSignedJar)) {
            try {
                ((IFile)target).refreshLocal(IResource.DEPTH_ONE,null);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
        else {
            IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(mSignedJar).getAbsoluteFile().toURI());
            if(!Common.isEmptyArray(files)) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        files[i].refreshLocal(IResource.DEPTH_ONE,null);
                    }
                    catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return super.postProcess(target, monitor);
    }

    protected void postJAR(IFile targetJar)
    {
    }
}