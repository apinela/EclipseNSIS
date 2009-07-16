/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.io.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Version;

public class MakeNSISProcess extends Process
{
    //These are for the MakeNSIS Process wrapper (needed for Windows NT & better)
    private boolean mCanceled = false;
    private String mLock = "lock"; //$NON-NLS-1$
    private Process mProcess = null;

    //These are for the Process extension (needed for Windows 98 and ME)
    private long mHandle = 0;
    private FileDescriptor mStdIn;
    private FileDescriptor mStdOut;
    private FileDescriptor mStdErr;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private InputStream mErrorStream;

    static
    {
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        if(!plugin.isWinNT()) {
            init(FileDescriptor.class, plugin.getJavaVendor(),
                 plugin.getJavaVersion().getNumber(Version.MAJOR),
                 plugin.getJavaVersion().getNumber(Version.MINOR));
        }
    }

    MakeNSISProcess(Process process)
    {
        setProcess(process);
    }

    /**
     * @param process
     */
    MakeNSISProcess(String nsisExe, String[] args, String[] env, File workingDir)
    {
        StringBuffer cmdbuf = new StringBuffer();
        appendArg(cmdbuf, nsisExe);
        for (int i = 0; i < args.length; i++) {
            cmdbuf.append(' ');
            appendArg(cmdbuf, args[i]);
        }
        String cmdstr = cmdbuf.toString();

        String envstr = null;
        if (env != null) {
            StringBuffer envbuf = new StringBuffer(256);
            for (int i = 0; i < env.length; i++) {
                envbuf.append(env[i]).append('\0');
            }
            envstr = envbuf.toString();
        }

        mStdIn = new FileDescriptor();
        mStdOut = new FileDescriptor();
        mStdErr = new FileDescriptor();
        String path = (workingDir == null?null:workingDir.getAbsolutePath());

        mHandle = create(cmdstr, envstr, path, mStdIn, mStdOut, mStdErr);
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {
            public Object run()
            {
                mOutputStream = new BufferedOutputStream(
                        new FileOutputStream(mStdIn));
                mInputStream = new BufferedInputStream(
                        new FileInputStream(mStdOut));
                mErrorStream = new FileInputStream(mStdErr);
                return null;
            }
        });
        setProcess(this);
    }

    public Process getProcess()
    {
        return mProcess;
    }

    private void setProcess(Process process)
    {
        mProcess = process;
    }

    private void appendArg(StringBuffer buffer, String arg)
    {
        if (arg.indexOf(' ') >= 0 || arg.indexOf('\t') >= 0) {
            if (arg.charAt(0) != '"') {
                buffer.append('"');
                buffer.append(arg);
                if (arg.endsWith("\\")) { //$NON-NLS-1$
                    buffer.append("\\"); //$NON-NLS-1$
                }
                buffer.append('"');
            }
            else if (arg.endsWith("\"")) { //$NON-NLS-1$
                /* The argument has already been quoted. */
                buffer.append(arg);
            }
            else {
                /* Unmatched quote for the argument. */
                throw new IllegalArgumentException();
            }
        }
        else {
            buffer.append(arg);
        }
    }

    public void cancel()
    {
        synchronized (mLock) {
            try {
                mProcess.destroy();
                mCanceled = true;
            }
            catch (Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
    }

    public boolean isCanceled()
    {
        synchronized (mLock) {
            return mCanceled;
        }
    }

    private void checkHandle()
    {
        if(mHandle <= 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
	public InputStream getErrorStream()
    {
        checkHandle();
        return mErrorStream;
    }

    @Override
	public InputStream getInputStream()
    {
        checkHandle();
        return mInputStream;
    }

    @Override
	public OutputStream getOutputStream()
    {
        checkHandle();
        return mOutputStream;
    }

    @Override
	public int exitValue()
    {
        checkHandle();
        return exitValue(mHandle);
    }

    @Override
	public int waitFor() throws InterruptedException
    {
        checkHandle();
        return waitFor(mHandle);
    }

    @Override
	public void destroy()
    {
        checkHandle();
        destroy(mHandle);
    }

    @Override
	protected void finalize() throws Throwable
    {
        if(mHandle > 0) {
            close(mHandle);
        }
        super.finalize();
    }

    private static native void init(Class<?> clasz, String vmName, int vmMajorVersion, int vmMinorVersion);

    private native int exitValue(long handle);

    private native int waitFor(long handle) throws InterruptedException;

    private native void destroy(long handle);

    private native long create(String cmdstr, String envstr, String path,
            FileDescriptor in_fd, FileDescriptor out_fd, FileDescriptor err_fd);

    private native void close(long handle);
}
