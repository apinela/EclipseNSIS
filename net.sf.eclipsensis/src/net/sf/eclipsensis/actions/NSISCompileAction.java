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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.console.INSISConsoleLineProcessor;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;

public class NSISCompileAction extends NSISScriptAction
{
    private static final Pattern cSyntaxPattern = Pattern.compile("[\\w]+ expects [0-9\\-\\+]+ parameters, got [0-9]\\."); //$NON-NLS-1$
    private static final Pattern cErrorPattern = Pattern.compile("error in script \"(.+)\" on line (\\d+).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	public void run(IAction action) {
        if(mPlugin != null) {
            new Thread(getRunnable()).start();
            action.setEnabled(false);
        }
	}
    
    protected NSISCompileRunnable getRunnable()
    {
        return new NSISCompileRunnable();
    }
    
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
            mAction.setEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return (!MakeNSISRunner.isRunning());
        }
        return false;
    }
    
    protected class NSISCompileRunnable implements Runnable, INSISConsoleLineProcessor
    {
        protected String mOutputExeName = null;
        protected boolean warningsMode = false;
        protected boolean errorMode = false;
        
        public void run()
        {
            if(mFile != null) {
                MakeNSISResults results = MakeNSISRunner.run(mFile, this);
                mOutputExeName = results.getOutputFileName();
            }
        }        

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.console.INSISConsoleListener#addedLine(net.sf.eclipsensis.console.NSISConsoleLine)
         */
        public NSISConsoleLine processText(String text)
        {
            NSISConsoleLine line;
            Matcher matcher = cErrorPattern.matcher(text);
            if(matcher.matches()) {
                line = NSISConsoleLine.error(text);
                IFile file = mFile.getWorkspace().getRoot().getFileForLocation(new Path(matcher.group(1)));
                if(file != null && file.equals(mFile)) {
                    line.setFile(mFile);
                    line.setLineNum(Integer.parseInt(matcher.group(2)));
                }
            }
            else {
                String lText = text.toLowerCase();
                if(lText.startsWith("error ") || lText.startsWith("error:") || //$NON-NLS-1$ //$NON-NLS-2$
                   lText.startsWith("!include: error ") || lText.startsWith("!include: error:")) { //$NON-NLS-1$ //$NON-NLS-2$
                    line = NSISConsoleLine.error(text);
                }
                else if(lText.startsWith("warning ") || lText.startsWith("warning:") || lText.startsWith("invalid ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    line = NSISConsoleLine.warning(text);
                }
                else if(lText.endsWith(" warning:") || lText.endsWith(" warnings:")) { //$NON-NLS-1$ //$NON-NLS-2$
                    warningsMode = true;
                    line = NSISConsoleLine.warning(text);
                }
                else if(cSyntaxPattern.matcher(lText).matches()) {
                    errorMode = true;
                    line = NSISConsoleLine.error(text);
                }
                else if(errorMode) {
                    line = NSISConsoleLine.error(text);
                }
                else if(warningsMode) {
                    line = NSISConsoleLine.warning(text);
                }
                else {
                    line = NSISConsoleLine.info(text);
                }
            }
            
            return line;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.console.INSISConsoleLineProcessor#reset()
         */
        public void reset()
        {
            mOutputExeName = null;
            warningsMode = false;
            errorMode = false;
        }
    }
}