/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.util.ArrayList;

public class RunnableInputStreamReader implements Runnable
{
    private InputStream mInputStream = null;
    private String[] mOutput = null;
    private boolean mSaveOutput = true;
    
    public RunnableInputStreamReader(InputStream inputStream)
    {
        this(inputStream,true);
    }
    
    public RunnableInputStreamReader(InputStream inputStream, boolean saveOutput)
    {
        mInputStream = inputStream;
        mSaveOutput = saveOutput;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        if(mSaveOutput) {
            mOutput = getOutput();
        }
    }

    public synchronized String[] getOutput()
    {
        if(mOutput == null) {
            ArrayList output = new ArrayList();
            BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
            try {
                String line = br.readLine();
                while(line != null) {
                    if(mSaveOutput) {
                        output.add(line);
                    }
                    line = br.readLine();
                }
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
            finally {
                Common.closeIO(br);
            }
            mOutput = (String[])output.toArray(Common.EMPTY_STRING_ARRAY);
        }
        return mOutput;
    }
}
