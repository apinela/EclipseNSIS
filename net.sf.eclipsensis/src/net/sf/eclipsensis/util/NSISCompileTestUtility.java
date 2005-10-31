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

import java.io.File;
import java.util.regex.Matcher;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class NSISCompileTestUtility
{
    public static final NSISCompileTestUtility INSTANCE = new NSISCompileTestUtility();
    
    private NSISCompileRunnable mRunnable = new NSISCompileRunnable();
    
    private NSISCompileTestUtility()
    {
        super();
    }
    
    public synchronized void compile(IPath script)
    {
        compile(script, false);
    }
    
    public void compile(IPath script, boolean test)
    {
        if(script != null) {
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            outer:
            for (int i = 0; i < windows.length; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length; j++) {
                    IEditorPart[] editors = pages[i].getDirtyEditors();
                    for (int k = 0; k < editors.length; k++) {
                        IEditorInput input = editors[i].getEditorInput();
                        if(script.getDevice()== null && input != null && input instanceof IFileEditorInput) {
                            if(!script.equals(((IFileEditorInput)input).getFile().getFullPath())) {
                                continue;
                            }
                        }
                        else if (script.getDevice() != null && input != null && input instanceof IPathEditorInput) {
                            if(!script.equals(((IPathEditorInput)input).getPath())) {
                                continue;
                            }
                        }
                        else {
                            continue;
                        }
                        if(!saveEditor(editors[i])) {
                            return;
                        }
                        break outer;
                    }
                }
            }
            mRunnable.setScript(script);
            mRunnable.setTest(test);
            new Thread(mRunnable).start();
        }
    }

    private boolean saveEditor(IEditorPart editor)
    {
        Shell shell = editor.getSite().getShell();
        IPathEditorInput input = (IPathEditorInput)editor.getEditorInput();
        String name = input.getPath().lastSegment();
        if(Common.openConfirm(shell, 
                EclipseNSISPlugin.getFormattedString("compile.save.confirmation", //$NON-NLS-1$
                                     new String[]{name}), //$NON-NLS-1$
                EclipseNSISPlugin.getShellImage())) {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            dialog.open();
            IProgressMonitor progressMonitor = dialog.getProgressMonitor();
            editor.doSave(progressMonitor);
            dialog.close();
            if(progressMonitor.isCanceled()) {
                return false;
            }
        }
        else {
            return false;
        }
        return true;
    }
    
    public boolean canTest(IPath script)
    {
        return getExeName(script) != null;
    }
    
    private String getExeName(IPath script)
    {
        if(script != null) {
            if(script.getDevice() == null) {
                return getExeName(getFile(script));
            }
            else {
                return getExeName(new File(script.toOSString()));
            }
        }
        return null;
    }
    
    private String getExeName(File file)
    {
        MakeNSISResults results = MakeNSISRunner.getResults(file);
        if (results != null) {
            String outputFileName = results.getOutputFileName();
            if (outputFileName != null) {
                File exeFile = new File(outputFileName);
                if (exeFile != null && exeFile.exists() && exeFile.isFile()) {
                    long exeTimestamp = exeFile.lastModified();
                    long fileTimestamp = file.lastModified();
                    if (exeTimestamp >= fileTimestamp) {
                        return exeFile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
    
    private String getExeName(IFile file)
    {
        if(file != null && file.isSynchronized(IResource.DEPTH_ZERO)) {
            if (!MakeNSISRunner.isCompiling()) {
                try {
                    String temp = file.getPersistentProperty(INSISConstants.NSIS_COMPILE_TIMESTAMP);
                    if(temp != null) {
                        long nsisCompileTimestamp = Long.parseLong(temp);
                        if(nsisCompileTimestamp >= file.getLocalTimeStamp()) {
                            temp = file.getPersistentProperty(INSISConstants.NSIS_EXE_NAME);
                            if(temp != null) {
                                File exeFile = new File(temp);
                                if(exeFile.exists()) {
                                    temp = file.getPersistentProperty(INSISConstants.NSIS_EXE_TIMESTAMP);
                                    if(temp != null) {
                                        if(Long.parseLong(temp) == exeFile.lastModified()) {
                                            return exeFile.getAbsolutePath();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception ex) {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        }
        return null;
    }

    public void test(IPath script)
    {
        String exeName = getExeName(script);
        if(exeName != null) {
            MakeNSISRunner.testInstaller(exeName, NSISConsole.MODEL);
        }
    }

    private IFile getFile(IPath path)
    {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }
    
    protected class NSISCompileRunnable implements Runnable, INSISConsoleLineProcessor
    {
        protected String mOutputExeName = null;
        protected boolean mWarningMode = false;
        protected boolean mErrorMode = false;
        protected IPath mScript = null;
        protected boolean mTest = false;
        
        protected void setScript(IPath script)
        {
            mScript = script;
        }

        protected void setTest(boolean test)
        {
            mTest = test;
        }

        public void run()
        {
            if(mScript != null) {
                mWarningMode = false;
                mErrorMode = false;
                MakeNSISResults results;
                if(mScript.getDevice() == null) {
                    results = MakeNSISRunner.compile(getFile(mScript), NSISConsole.MODEL, this);
                }
                else {
                    results = MakeNSISRunner.compile(new File(mScript.toOSString()), NSISPreferences.INSTANCE,
                                                     NSISConsole.MODEL,this, true);
                }
                mOutputExeName = results.getOutputFileName();
                if(mTest && mOutputExeName != null) {
                    MakeNSISRunner.testInstaller(mOutputExeName, null);
                }
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
            if(lText.startsWith("error")) { //$NON-NLS-1$
                Matcher matcher = MakeNSISRunner.MAKENSIS_ERROR_PATTERN.matcher(text);
                if(matcher.matches()) {
                    line = NSISConsoleLine.error(text);
                    setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
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
            if(line.getType() == NSISConsoleLine.TYPE_WARNING) {
                Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text);
                if(matcher.matches()) {
                    setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                }
                else if(!text.endsWith("warnings:") && !text.endsWith("warning:")) { //$NON-NLS-1$ //$NON-NLS-2$ 
                    setLineInfo(line, (mScript.getDevice() != null?mScript:null), 1);
                }
            }
            
            return line;
        }
        
        private void setLineInfo(NSISConsoleLine line, IPath path, int lineNum)
        {
            if(mScript.getDevice() == null) {
                if(path == null) {
                    path = mScript;
                }
                else {
                    path = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path).getFullPath();
                }
                if(path.equals(mScript)) {
                    line.setSource(path);
                    line.setLineNum(lineNum);
                }
            }
            else if(mScript.equals(path)) {
                line.setSource(mScript);
                line.setLineNum(lineNum);
            }
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
