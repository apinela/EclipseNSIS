/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class NSISCompileTestUtility
{
    public static final NSISCompileTestUtility INSTANCE = new NSISCompileTestUtility();

    private Map mResultsMap;
    private Pattern mNSISExtPattern = Pattern.compile(INSISConstants.NSI_WILDCARD_EXTENSION,Pattern.CASE_INSENSITIVE);

    private NSISCompileTestUtility()
    {
        super();
        File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
        final File cacheFile = new File(stateLocation, getClass().getName() + ".ResultsCache.ser"); //$NON-NLS-1$
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;

            public boolean isStarted()
            {
                return mStarted;
            }

            public void start(IProgressMonitor monitor)
            {
                Map map = null;
                if(IOUtility.isValidFile(cacheFile)) {
                    Object obj = null;
                    try {
                        obj = IOUtility.readObject(cacheFile);
                    }
                    catch (Exception e) {
                        obj = null;
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    if (obj != null && Map.class.isAssignableFrom(obj.getClass())) {
                        map = (Map)obj;
                    }
                }
                if(map == null) {
                    mResultsMap = new MRUMap(20);
                }
                else {
                    if(map instanceof MRUMap) {
                        mResultsMap = map;
                    }
                    else {
                        mResultsMap = new MRUMap(20, map);
                    }
                }
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                mStarted = false;
                try {
                    IOUtility.writeObject(cacheFile, mResultsMap);
                }
                catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        });
    }

    public MakeNSISResults getCachedResults(File script)
    {
        if(IOUtility.isValidFile(script)) {
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
        if(IOUtility.isValidFile(script)) {
            if (!MakeNSISRunner.isCompiling()) {
                mResultsMap.remove(script);
            }
        }
    }

    public synchronized boolean compile(IPath script)
    {
        return compile(script, false);
    }

    public boolean compile(IPath script, boolean test)
    {
        if(script != null) {
            List editorList = new ArrayList();
            int beforeCompileSave = NSISPreferences.INSTANCE.getPreferenceStore().getInt(INSISPreferenceConstants.BEFORE_COMPILE_SAVE);
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            outer:
            for (int i = 0; i < windows.length; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length; j++) {
                    IEditorPart[] editors = pages[j].getDirtyEditors();
                    for (int k = 0; k < editors.length; k++) {
                        IEditorInput input = editors[k].getEditorInput();
                        switch(beforeCompileSave) {
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_CONFIRM:
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_AUTO:
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
                                editorList.add(editors[k]);
                                break outer;
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_CONFIRM:
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_AUTO:
                                String ext;
                                if(input instanceof IFileEditorInput) {
                                    ext = ((IFileEditorInput)input).getFile().getFullPath().getFileExtension();
                                }
                                else if (input instanceof IPathEditorInput) {
                                    ext = ((IPathEditorInput)input).getPath().getFileExtension();
                                }
                                else {
                                    continue;
                                }
                                if(mNSISExtPattern.matcher(ext).matches()) {
                                    editorList.add(editors[k]);
                                }
                        }
                    }
                }
            }
            if(!saveEditors(editorList, beforeCompileSave)) {
                return false;
            }
            new Thread(new NSISCompileRunnable(script,test),EclipseNSISPlugin.getResourceString("makensis.thread.name")).start(); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    private boolean saveEditors(List editors, int beforeCompileSave)
    {
        if (!Common.isEmptyCollection(editors)) {
            boolean ok = false;
            String message = null;
            switch(beforeCompileSave) {
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_CONFIRM:
                    IEditorPart editor = (IEditorPart)editors.get(0);
                    if(editors.size() > 1) {
                        editors = editors.subList(0,1);
                    }
                    IPathEditorInput input = (IPathEditorInput)editor.getEditorInput();
                    String name = input.getPath().lastSegment();
                    message = EclipseNSISPlugin.getFormattedString("compile.save.current.confirmation", //$NON-NLS-1$
                                                                   new String[]{name});
                    break;
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_CONFIRM:
                    message = EclipseNSISPlugin.getResourceString("compile.save.all.confirmation"); //$NON-NLS-1$
                    break;
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_AUTO:
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_AUTO:
                    ok = true;
            }
            Shell shell = Display.getDefault().getActiveShell();
            if(!ok) {
                ok = Common.openConfirm(shell, message, EclipseNSISPlugin.getShellImage());
            }
            if(ok) {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                dialog.open();
                IProgressMonitor progressMonitor = dialog.getProgressMonitor();
                if(editors.size() > 1) {
                    progressMonitor.beginTask("Saving open NSIS files",editors.size());
                    for (Iterator iter = editors.iterator(); iter.hasNext();) {
                        IEditorPart editor = (IEditorPart)iter.next();
                        SubProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 1);
                        editor.doSave(monitor);
                        if(monitor.isCanceled()) {
                            break;
                        }
                    }
                }
                else {
                    ((IEditorPart)editors.get(0)).doSave(progressMonitor);
                }
                dialog.close();
                if (progressMonitor.isCanceled()) {
                    return false;
                }
            }
            return ok;
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
                if(IOUtility.isValidFile(exeFile)) {
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

    public void test(final IPath script)
    {
        test(getExeName(script), EclipseNSISPlugin.getDefault().getConsole());
    }

    private void test(final String exeName, final INSISConsole console)
    {
        if(exeName != null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    BusyIndicator.showWhile(Display.getDefault(),new Runnable() {
                        public void run()
                        {
                            MakeNSISRunner.testInstaller(exeName, console);
                        }
                    });
                }
            });
        }
    }

    private IFile getFile(IPath path)
    {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }

    private static final class MRUMap extends LinkedHashMap
    {
        private static final long serialVersionUID = -4303663274693162132L;

        private final int mMaxSize;

        public MRUMap(int maxSize)
        {
            super(15,0.75f,true);
            mMaxSize= maxSize;
        }

        public MRUMap(int maxSize, Map map)
        {
            this(maxSize);
            putAll(map);
        }

        public Object put(Object key, Object value)
        {
            Object object= remove(key);
            super.put(key, value);
            return object;
        }

        protected boolean removeEldestEntry(Map.Entry eldest)
        {
            return (mMaxSize > 0 && size() > mMaxSize);
        }
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
                    if(results != null) {
                        mResultsMap.put(file, results);
                    }
                }
                if(results != null) {
                    mOutputExeName = results.getOutputFileName();
                    if(mTest && mOutputExeName != null) {
                        test(mOutputExeName, null);
                    }
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
