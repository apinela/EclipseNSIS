/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

/**
 * @author Sunil.Kamath
 */
public class MakeNSISProcess
{
    private boolean mCanceled = false;
    private Process mProcess = null;
    private String mLock = "lock"; //$NON-NLS-1$
    
    
    /**
     * @param process
     */
    public MakeNSISProcess(Process process)
    {
        mProcess = process;
    }
    
    public void cancel()
    {
        synchronized(mLock) {
            try {
                mProcess.destroy();
                mCanceled = true;
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    public boolean isCanceled()
    {
        synchronized(mLock) {
            return mCanceled;
        }
    }
}
