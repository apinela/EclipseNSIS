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

import java.util.regex.Matcher;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.console.INSISConsoleLineProcessor;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class NSISCompileAction extends NSISScriptAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	final public void run(IAction action) {
        if(mPlugin != null) {
            action.setEnabled(false);
            MakeNSISRunner.startup();
            if(mFile != null) {
                IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                outer:
                for (int i = 0; i < windows.length; i++) {
                    IWorkbenchPage[] pages = windows[i].getPages();
                    for (int j = 0; j < pages.length; j++) {
                        IEditorPart[] editors = pages[i].getDirtyEditors();
                        for (int k = 0; k < editors.length; k++) {
                            IEditorInput input = editors[i].getEditorInput();
                            if(input != null && input instanceof IFileEditorInput) {
                                if(mFile.equals(((IFileEditorInput)input).getFile())) {
                                    if(Common.openConfirm(windows[i].getShell(), 
                                            EclipseNSISPlugin.getFormattedString("compile.save.confirmation", //$NON-NLS-1$
                                                                 new String[]{mFile.getName()}))) { //$NON-NLS-1$
                                        if(editors[i] instanceof ITextEditor) {
                                            IAction saveAction = ((ITextEditor)editors[i]).getAction(ITextEditorActionConstants.SAVE);
                                            if(saveAction != null) {
                                                saveAction.run();
                                                break outer;
                                            }
                                        }
                                        editors[i].doSave(null);
                                        break outer;
                                    }
                                    else {
                                        action.setEnabled(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                new Thread(getRunnable()).start();
            }
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
        protected boolean mWarningMode = false;
        protected boolean mErrorMode = false;
        
        public void run()
        {
            if(mFile != null) {
                MakeNSISResults results = MakeNSISRunner.compile(mFile, this);
                mOutputExeName = results.getOutputFileName();
            }
        }        

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.console.INSISConsoleListener#addedLine(net.sf.eclipsensis.console.NSISConsoleLine)
         */
        public NSISConsoleLine processText(String text)
        {
            NSISConsoleLine line;
            text = text.trim();

            String lText = text.toLowerCase();
            if(lText.startsWith("error")) {
                Matcher matcher = MakeNSISRunner.MAKENSIS_ERROR_PATTERN.matcher(text);
                if(matcher.matches()) {
                    line = NSISConsoleLine.error(text);
                    IFile file = mFile.getWorkspace().getRoot().getFileForLocation(new Path(matcher.group(1)));
                    if(file != null && file.equals(mFile)) {
                        line.setFile(file);
                        line.setLineNum(Integer.parseInt(matcher.group(2)));
                    }
                    return line;
                }
            }
            if(lText.startsWith("error ") || lText.startsWith("error:") || //$NON-NLS-1$ //$NON-NLS-2$
               lText.startsWith("!include: error ") || lText.startsWith("!include: error:")) { //$NON-NLS-1$ //$NON-NLS-2$
                line = NSISConsoleLine.error(text);
            }
            else if(lText.startsWith("warning ") || lText.startsWith("warning:") || lText.startsWith("invalid ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                line = NSISConsoleLine.warning(text);
            }
            else if(lText.endsWith(" warning:") || lText.endsWith(" warnings:")) { //$NON-NLS-1$ //$NON-NLS-2$
                mWarningMode = true;
                line = NSISConsoleLine.warning(text);
            }
            else if(MakeNSISRunner.MAKENSIS_SYNTAX_ERROR_PATTERN.matcher(lText).matches()) {
                mErrorMode = true;
                line = NSISConsoleLine.error(text);
            }
            else if(mErrorMode) {
                line = NSISConsoleLine.error(text);
            }
            else if(mWarningMode) {
                line = NSISConsoleLine.warning(text);
            }
            else {
                line = NSISConsoleLine.info(text);
            }
            if(line.getType() == NSISConsoleLine.WARNING) {
                Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text);
                if(matcher.matches()) {
                    IFile file = mFile.getWorkspace().getRoot().getFileForLocation(new Path(matcher.group(1)));
                    if(file != null && file.equals(mFile)) {
                        line.setFile(file);
                        line.setLineNum(Integer.parseInt(matcher.group(2)));
                    }
                }
                else if(!text.endsWith("warnings:") && !text.endsWith("warning:")) { //$NON-NLS-1$ //$NON-NLS-2$ 
                    line.setFile(mFile);
                    line.setLineNum(1);
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
            mWarningMode = false;
            mErrorMode = false;
        }
    }
}