/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.makensis.MakeNSISRunner;

public class NSISCompileTestAction extends NSISCompileAction
{
    protected NSISCompileRunnable getRunnable()
    {
        return new NSISCompileTestRunnable();
    }
    
    protected class NSISCompileTestRunnable extends NSISCompileRunnable
    {
        /**
         * 
         */
        public NSISCompileTestRunnable()
        {
            super();
        }
        
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            super.run();
            if(mPlugin !=null && mOutputExeName != null) {
                MakeNSISRunner.testInstaller(mOutputExeName);
            }
        }
    }
}
