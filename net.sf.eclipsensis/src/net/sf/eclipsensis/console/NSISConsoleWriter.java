/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.io.*;

import net.sf.eclipsensis.makensis.MakeNSISProcess;

public class NSISConsoleWriter implements Runnable
{
    private MakeNSISProcess mProcess = null;
    private NSISConsole mConsole = null;
    private InputStream mInputStream = null;
    private INSISConsoleLineProcessor mLineProcessor = null;
    
    public NSISConsoleWriter(MakeNSISProcess process, NSISConsole console, InputStream inputStream, INSISConsoleLineProcessor lineProcessor)
    {
        mProcess = process;
        mConsole = console;
        mInputStream = inputStream;
        mLineProcessor = lineProcessor;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
            NSISConsoleLine line;
            String text = br.readLine();
            while(text != null) {
                if(!mProcess.isCanceled()) {
                    if(mLineProcessor != null) {
                        line = mLineProcessor.processText(text);
                    }
                    else {
                        line = NSISConsoleLine.info(text);
                    }
                    mConsole.add(line);
                    text = br.readLine();
                }
                else {
                    break;
                }
            }
            br.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            mConsole.add(NSISConsoleLine.error(ex.getMessage()));
        }
    }
}
