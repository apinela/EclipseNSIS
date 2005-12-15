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
import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class NSISCompileTestUtility
{
    public static final NSISCompileTestUtility INSTANCE = new NSISCompileTestUtility();

    private Map mResultsMap = new HashMap();

    private NSISCompileTestUtility()
    {
        super();
    }

    public MakeNSISResults getCachedResults(File script)
    {
        if(script != null && script.exists() && script.isFile()) {
            if (!MakeNSISRunner.isCompiling()) {
                MakeNSISResults results = (MakeNSISResults)mResultsMap.get(script);
                if(results != null) {
                    if(script.lastModified() > results.getCompileTimestamp()) {
                        results = null;
                        mResultsMap.remove(script);
                    }
                }
                return results;
            }
        }
        return null;
    }

    public void removeCachedResults(File script)
    {
        if(script != null && script.exists() && script.isFile()) {
            if (!MakeNSISRunner.isCompiling()) {
                mResultsMap.remove(script);
            }
        }
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
                    IEditorPart[] editors = pages[j].getDirtyEditors();
                    for (int k = 0; k < editors.length; k++) {
                        IEditorInput input = editors[k].getEditorInput();
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
                        if(!saveEditor(editors[k])) {
                            return;
                        }
                        break outer;
                    }
                }
            }
            new Thread(new NSISCompileRunnable(script,test)).start();
        }
    }

    private boolean saveEditor(IEditorPart editor)
    {
        Shell shell = editor.getSite().getShell();
        IPathEditorInput input = (IPathEditorInput)editor.getEditorInput();
        String name = input.getPath().lastSegment();
        if(Common.openConfirm(shell,
                EclipseNSISPlugin.getFormattedString("compile.save.confirmation", //$NON-NLS-1$
                                     new String[]{name}),
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
        MakeNSISResults results = getCachedResults(file);
        if(results != null) {
            String outputFileName = results.getOutputFileName();
            if(outputFileName != null) {
                File exeFile = new File(outputFileName);
                if(exeFile != null && exeFile.exists() && exeFile.isFile()) {
                    long exeTimestamp = exeFile.lastModified();
                    long fileTimestamp = file.lastModified();
                    if(exeTimestamp >= fileTimestamp) {
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
            MakeNSISRunner.testInstaller(exeName, EclipseNSISPlugin.getDefault().getConsole());
        }
    }

    private IFile getFile(IPath path)
    {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }

    private class NSISCompileRunnable implements Runnable, INSISConsoleLineProcessor
    {
        private String mOutputExeName = null;
        private IPath mScript = null;
        private boolean mTest = false;
        private INSISConsoleLineProcessor mDelegate;

        public NSISCompileRunnable(IPath script, boolean test)
        {
            mScript = script;
            mTest = test;
            mDelegate = new NSISConsoleLineProcessor(mScript);
        }

        public void run()
        {
            if(mScript != null) {
                reset();
                MakeNSISResults results;
                if(mScript.getDevice() == null) {
                    results = MakeNSISRunner.compile(getFile(mScript), EclipseNSISPlugin.getDefault().getConsole(), this);
                }
                else {
                    File file = new File(mScript.toOSString());
                    results = MakeNSISRunner.compile(file, NSISPreferences.INSTANCE,
                                                     EclipseNSISPlugin.getDefault().getConsole(),this);
                    mResultsMap.put(file, results);
                }
                mOutputExeName = results.getOutputFileName();
                if(mTest && mOutputExeName != null) {
                    MakeNSISRunner.testInstaller(mOutputExeName, null);
                }
            }
        }

        public NSISConsoleLine processText(String text)
        {
            return mDelegate.processText(text);
        }

        public void reset()
        {
            mOutputExeName = null;
            mDelegate.reset();
        }
    }
}
