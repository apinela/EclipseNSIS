/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import java.io.File;

import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;

public class NSISTestAction extends NSISScriptAction
{
    private String mExeName = null;

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#started()
     */
    public void started()
    {
        if(mAction != null) {
            mAction.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#stopped()
     */
    public void stopped()
    {
        if(mAction != null) {
            mAction.setEnabled(isEnabled());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        mExeName = null;
        if(super.isEnabled() && mFile != null && mFile.isSynchronized(IResource.DEPTH_ZERO)) {
            if (!MakeNSISRunner.isRunning()) {
                try {
                    String temp = mFile.getPersistentProperty(NSIS_COMPILE_TIMESTAMP);
                    if(temp != null) {
                        long nsisCompileTimestamp = Long.parseLong(temp);
                        if(nsisCompileTimestamp >= mFile.getLocalTimeStamp()) {
                            temp = mFile.getPersistentProperty(NSIS_EXE_NAME);
                            if(temp != null) {
                                File exeFile = new File(temp);
                                if(exeFile.exists()) {
                                    temp = mFile.getPersistentProperty(NSIS_EXE_TIMESTAMP);
                                    if(temp != null) {
                                        if(Long.parseLong(temp) == exeFile.lastModified()) {
                                            mExeName = exeFile.getAbsolutePath();
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        if(mPlugin !=null && mExeName != null) {
            mPlugin.testInstaller(mExeName);
        }
    }

}
