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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.console.model.NSISConsoleModel;
import net.sf.eclipsensis.makensis.MakeNSISProcess;
import net.sf.eclipsensis.util.Common;

public class NSISConsoleWriter implements Runnable
{
    private MakeNSISProcess mProcess = null;
    private NSISConsoleModel mModel = null;
    private InputStream mInputStream = null;
    private INSISConsoleLineProcessor mLineProcessor = null;

    public NSISConsoleWriter(MakeNSISProcess process, NSISConsoleModel model, InputStream inputStream, INSISConsoleLineProcessor lineProcessor)
    {
        mProcess = process;
        mModel = model;
        mInputStream = inputStream;
        mLineProcessor = lineProcessor;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(mInputStream));
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
                    mModel.add(line);
                    try {
                        text = br.readLine();
                    }
                    catch (IOException ioe) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }
        catch(Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
            mModel.add(NSISConsoleLine.error(ex.getLocalizedMessage()));
        }
        finally {
            Common.closeIO(br);
        }
    }
}
