/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.launching.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.*;

public abstract class AbstractJARUtil implements IJavaLaunchConfigurationConstants 
{
    protected List mTargetJars = null;
    protected File mToolsJar = null;
    protected String mKeyStore = ""; //$NON-NLS-1$
    protected boolean mVerbose = false;
    protected boolean mIgnoreErrors = false;
    
    public AbstractJARUtil(String toolsJar, List targetJars)
    {
        mToolsJar = new File(toolsJar);
        mTargetJars = targetJars;
    }
    
    protected String maybeQuote(String str)
    {
        if(!JARSignerPlugin.isEmpty(str)) {
            if(!str.startsWith("\"") || !str.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                char[] chars = str.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if(Character.isWhitespace(chars[i])) {
                        return new StringBuffer("\"").append(str).append("\"").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        return str;
    }
    
    public void setKeyStore(String keyStore)
    {
        mKeyStore = maybeQuote(keyStore);
    }

    public void setVerbose(boolean verbose)
    {
        mVerbose = verbose;
    }

    public void setIgnoreErrors(boolean ignoreErrors)
    {
        mIgnoreErrors = ignoreErrors;
    }

    protected void writeLogMessage(final MessageConsoleStream stream, final String message)
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                stream.print(message); //$NON-NLS-1$
            }
        });
    }
    
    public void run(IProgressMonitor monitor)
    {
        try {
            MessageFormat argsFormat = createArgsFormat();

            IVMInstall jre = JARSignerPlugin.getDefault().getVMInstall();
            IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(mToolsJar.getAbsolutePath()));
            toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
            IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
            IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath,
                                                    IRuntimeClasspathEntry.STANDARD_CLASSES);
            List classpath = new ArrayList();
            classpath.add(toolsEntry.getMemento());
            classpath.add(systemLibsEntry.getMemento());

            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = manager.getLaunchConfigurationType(ID_JAVA_APPLICATION);
            
            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, getLaunchTitle()); //$NON-NLS-1$
            
            // specify a JRE
            workingCopy.setAttribute(ATTR_VM_INSTALL_NAME, jre.getName());
            workingCopy.setAttribute(ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
            
            // specify main type and program arguments
            workingCopy.setAttribute(ATTR_MAIN_TYPE_NAME, "sun.security.tools.JarSigner"); //$NON-NLS-1$
            workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
            workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH, false);
            
            final MessageConsoleStream[] streams = new MessageConsoleStream[2];
            monitor.beginTask("", mTargetJars.size()); //$NON-NLS-1$
            final MessageConsole console = new MessageConsole(getConsoleTitle(),null); //$NON-NLS-1$
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
                    consoleManager.addConsoles(new IConsole[]{console});
                    streams[0] = console.newMessageStream();
    
                    streams[1] = console.newMessageStream();
                    streams[1].setColor(JFaceResources.getColorRegistry().get("ERROR_COLOR")); //$NON-NLS-1$
                    consoleManager.showConsoleView(console);
                }
            });
            for(Iterator iter=mTargetJars.iterator(); iter.hasNext(); ) {
                IFile targetJar = (IFile)iter.next();
                try {
                    String arg = argsFormat.format(new String[]{maybeQuote(targetJar.getLocation().toOSString())});
                    workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, arg);
                    ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE,new SubProgressMonitor(monitor,100),false,false);
                    IProcess process = launch.getProcesses()[0];
                    IStreamsProxy proxy = process.getStreamsProxy();
                    IStreamMonitor stdout = proxy.getOutputStreamMonitor();
                    stdout.addListener(new IStreamListener(){
                        public void streamAppended(String text, IStreamMonitor monitor)
                        {
                            writeLogMessage(streams[0],text);
                        }
                    });
                    IStreamMonitor stderr = proxy.getErrorStreamMonitor();
                    stderr.addListener(new IStreamListener(){
                        public void streamAppended(String text, IStreamMonitor monitor)
                        {
                            writeLogMessage(streams[1],text);
                        }
                    });
                    while(!process.isTerminated()) {
                        Thread.sleep(100);
                    }
                    if(process.getExitValue() != 0) {
                        writeLogMessage(streams[1],getFailMessage(targetJar)); //$NON-NLS-1$
                    }
                    else {
                        writeLogMessage(streams[0],getSuccessMessage(targetJar)); //$NON-NLS-1$
                    }
                }
                catch (Throwable e) {
                    writeLogMessage(streams[1],getExceptionMessage(e));
                    if(!mIgnoreErrors) {
                        return;
                    }
                }
                postJAR(targetJar);
                monitor.worked(1);
                if(iter.hasNext()) {
                    writeLogMessage(streams[0],"\n"); //$NON-NLS-1$
                }
            }
         }
        catch(Exception ex) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),JARSignerPlugin.getResourceString("error.title"),ex.getMessage()); //$NON-NLS-1$
        }
    }
    
    protected void postJAR(IFile targetJar) throws Exception
    {
    }

    protected String getExceptionMessage(Throwable exception)
    {
        String message = null;
        if(exception != null) {
            message = exception.getMessage();
            if(message == null || message.length() == 0) {
                message = getExceptionMessage(exception.getCause());
                if(message == null) {
                    message = exception.getClass().getName();
                }
            }
        }
        return message;
    }

    protected abstract String getSuccessMessage(IFile targetJar);

    protected abstract String getFailMessage(IFile targetJar);

    protected abstract String getConsoleTitle();

    protected abstract String getLaunchTitle();

    protected abstract MessageFormat createArgsFormat();
}