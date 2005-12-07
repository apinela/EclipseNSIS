/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISLaunchConfigDelegate implements ILaunchConfigurationDelegate
{
    private static final String VALID_FILENAME_CHARS = ":\\.$-{}!()&^+,=[]";
    
    public void launch(ILaunchConfiguration configuration, String mode, final ILaunch launch, final IProgressMonitor monitor) throws CoreException
    {
        String script = null;
        boolean useConsole = true;
        File outputFile = null;
        String output = null;
        boolean append = false;
        boolean runInstaller = false;
        String encoding;

        script = configuration.getAttribute(NSISLaunchSettings.SCRIPT, "");
        runInstaller = configuration.getAttribute(NSISLaunchSettings.RUN_INSTALLER, false);
        useConsole = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
        output = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
        append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
        encoding = configuration.getAttribute(IDebugUIConstants.ATTR_CONSOLE_ENCODING, (String)null);

        if (Common.isEmpty(script)) {
            throw new CoreException(new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,IStatus.ERROR,"No script specified",null));
        }
        IStringVariableManager stringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
        IPath path = new Path(stringVariableManager.performStringSubstitution(script));
        IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if(ifile != null) {
            path = ifile.getFullPath(); 
        }

        if (output != null) {
            output = stringVariableManager.performStringSubstitution(output);
        }

        ifile = null;
        if (output != null) {
            ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(output));

            try {
                if (ifile != null) {
                    if (append && ifile.exists()) {
                        ifile.appendContents(new ByteArrayInputStream(new byte[0]), true, true, new NullProgressMonitor());
                    }
                    else {
                        if (ifile.exists()) {
                            ifile.delete(true, new NullProgressMonitor());
                        }
                        ifile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
                    }
                }

                outputFile = new File(output);
            }
            catch (CoreException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }

        INSISConsole console;
        if (!useConsole) {
            if (outputFile == null) {
                console = new NullNSISConsole();
            }
            else {
                console = new FileNSISConsole(outputFile, append);
            }
        }
        else {
            if (outputFile == null) {
                console = EclipseNSISPlugin.getDefault().getConsole();
            }
            else {
                console = new CompoundNSISConsole(new INSISConsole[]{new FileNSISConsole(outputFile, append), EclipseNSISPlugin.getDefault().getConsole()});
            }
        }

        String defaultEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
        if(encoding != null && !encoding.equals(defaultEncoding)) {
            console = new EncodingNSISConsole(console, encoding);
        }
        NSISSettings settings = new NSISLaunchSettings(NSISPreferences.INSTANCE, configuration);
        final NSISLaunchProcess process = new NSISLaunchProcess(path, launch);
        launch.addProcess(process);

        try {
            if (MakeNSISRunner.isCompiling()) {
                monitor.beginTask("Waiting for previous MakeNSIS to terminate...", IProgressMonitor.UNKNOWN);
                while (MakeNSISRunner.isCompiling()) {
                    monitor.worked(5);
                    try {
                        Thread.sleep(50);
                        if (monitor.isCanceled()) {
                            process.terminate();
                            return;
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (monitor.isCanceled()) {
                process.terminate();
                return;
            }
            
            monitor.beginTask("Compiling NSIS script...", IProgressMonitor.UNKNOWN);
            new Thread(new Runnable() {
                public void run()
                {
                    while(true) {
                        if(!process.isTerminated()) {
                            if(monitor.isCanceled()) {
                                process.terminate();
                                break;
                            }
                        }
                        break;
                    }
                }
            }).start();
            MakeNSISResults results = MakeNSISRunner.compile(path, settings, console, new NSISConsoleLineProcessor(path));
            if(ifile != null) {
                try {
                    ifile.refreshLocal(IResource.DEPTH_ZERO, monitor);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            if(useConsole && outputFile != null) {
                String filename;
                final IEditorInput editorInput;
                IEditorDescriptor descriptor;
                final IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
                if(ifile != null) {
                    filename = ifile.getFullPath().toString();
                    editorInput = new FileEditorInput(ifile);
                    descriptor = registry.getDefaultEditor(ifile.getName());
                }
                else {
                    filename = outputFile.getAbsolutePath();
                    editorInput = new NSISExternalFileEditorInput(outputFile);
                    descriptor = registry.getDefaultEditor(outputFile.getName());
                }
                if(descriptor == null) {
                    descriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor");
                }
                if(descriptor == null) {
                    descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
                }
                if (descriptor != null) {
                    final String editorId = descriptor.getId();
                    final IHyperlink hyperlink = new IHyperlink() {
                        public void linkEntered()
                        {
                        }

                        public void linkExited()
                        {
                        }

                        public void linkActivated()
                        {
                            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                            try {
                                IDE.openEditor(page, editorInput, editorId);
                            }
                            catch (PartInitException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    };
                    String message = MessageFormat.format("[Console output redirected to file: {0}]", new String[]{filename});
                    final NSISConsole nsisConsole = EclipseNSISPlugin.getDefault().getConsole();
                    final String ffilename;
                    if(encoding != null && !encoding.equals(defaultEncoding)) {
                        String temp;
                        try {
                            message = new String(message.getBytes(), encoding);
                            temp = new String(filename.getBytes(), encoding);
                        }
                        catch (Exception e) {
                            temp = filename;
                        }
                        ffilename = temp;
                    }
                    else {
                        ffilename = filename;
                    }
                    System.out.println(ffilename);
                    nsisConsole.addPatternMatchListener(new IPatternMatchListener() {
                        String mPattern = escape(ffilename);
                        
                        private String escape(String path) 
                        {
                            StringBuffer buffer = new StringBuffer("");
                            if(path != null) {
                                char[] chars = path.toCharArray();
                                for (int i = 0; i < chars.length; i++) {
                                    switch(chars[i]) {
                                        case ' ':
                                            buffer.append("\\x20");
                                            break;
                                        case '\t':
                                            buffer.append("\\t");
                                            break;
                                        default:
                                            if(VALID_FILENAME_CHARS.indexOf(chars[i]) >= 0) {
                                                buffer.append('\\');
                                            }
                                            buffer.append(chars[i]);
                                    }
                                }
                            }
                            return buffer.toString();
                        }
                        
                        public String getPattern() 
                        {
                            return mPattern;
                        }

                        public void matchFound(PatternMatchEvent event) 
                        {
                            try {
                                nsisConsole.addHyperlink(hyperlink, event.getOffset(), event.getLength());
                                nsisConsole.removePatternMatchListener(this);
                            } catch (BadLocationException e) {
                            }
                        }

                        public int getCompilerFlags() 
                        {
                            return 0;
                        }

                        public String getLineQualifier() 
                        {
                            return null;
                        }

                        public void connect(TextConsole console) 
                        {
                        }

                        public void disconnect() {
                        }
                    });
                    nsisConsole.appendLine(NSISConsoleLine.info(message));
                }
            }
            if(results != null && results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS && runInstaller) {
                MakeNSISRunner.testInstaller(results.getOutputFileName(), console);
            }
        }
        finally {
            process.terminate();
        }
    }
}