/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.settings.NSISProperties;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sunil.Kamath
 */
public class MakeNSISRunner implements INSISConstants
{
    private static MakeNSISProcess cProcess = null;
    private static String cBestCompressorFormat = null;
    private static String cProcessLock = "lock"; //$NON-NLS-1$
    private static long cHwnd = 0;
    private static HashSet cListeners = new HashSet();
    
    static {
        System.loadLibrary("MakeNSISRunner"); //$NON-NLS-1$
        cBestCompressorFormat = EclipseNSISPlugin.getResourceString("best.compressor.format"); //$NON-NLS-1$
    }

    public static void addListener(IMakeNSISRunListener listener)
    {
        cListeners.add(listener);
    }
    
    public static void removeListener(IMakeNSISRunListener listener)
    {
        cListeners.remove(listener);
    }
    
    private static void notifyListeners(boolean started)
    {
        for(Iterator iter=cListeners.iterator(); iter.hasNext(); ) {
            if(started) {
                ((IMakeNSISRunListener)iter.next()).started();
            }
            else {
                ((IMakeNSISRunListener)iter.next()).stopped();
            }
        }
    }
    
    public static boolean cancel()
    {
        synchronized(cProcessLock) {
            if(cProcess != null) {
                cProcess.cancel();
                boolean isCanceled = cProcess.isCanceled();
                cProcess = null;
                return isCanceled;
            }
        }
        return false;
    }

    public static boolean isRunning()
    {
        synchronized(cProcessLock) {
            return (cProcess != null);
        }
    }

    /**
     * @param process The process to set.
     */
    private static void setProcess(MakeNSISProcess process)
    {
        synchronized(cProcessLock) {
            cProcess = process;
        }
    }
    
    private static NSISSettings getSettings(IFile file)
    {
        NSISProperties properties = NSISProperties.getProperties(file);
        if (properties.getUseDefaults()) {
            return NSISPreferences.getPreferences();
        }
        else {
            return properties;
        }
    }
    
    public static synchronized MakeNSISResults run(IFile file, INSISConsoleLineProcessor outputProcessor)
    {
        try {
            notifyListeners(true);
            MakeNSISResults results = null;
            if(file != null) {
                NSISConsole console = NSISConsole.getConsole();
                try {
                    console.clear();
                    reset();
                    startup();
                    String fileName = file.getLocation().toFile().getAbsolutePath();
                    File workDir = file.getLocation().toFile().getParentFile();
                    String[] env = Common.getEnv();
                    NSISSettings settings = getSettings(file);
                    ArrayList options = new ArrayList();
                    if(settings.getVerbosity() != settings.getDefaultVerbosity()) {
                        options.add(EclipseNSISPlugin.getResourceString("makensis.verbose.option")+settings.getVerbosity()); //$NON-NLS-1$
                    }
                    int compressor = settings.getCompressor();
                    switch(compressor) {
                        case COMPRESSOR_DEFAULT:
                        case COMPRESSOR_BEST:
                            break;
                        default:
                            options.add(EclipseNSISPlugin.getResourceString("makensis.setcompressor.final.option")+COMPRESSOR_NAME_ARRAY[compressor]); //$NON-NLS-1$
                    }
                    if(settings.getHdrInfo()) {
                        options.add(EclipseNSISPlugin.getResourceString("makensis.hdrinfo.option")); //$NON-NLS-1$
                    }
                    if(settings.getLicense()) {
                        options.add(EclipseNSISPlugin.getResourceString("makensis.license.option")); //$NON-NLS-1$
                    }
                    if(settings.getNoConfig()) {
                        options.add(EclipseNSISPlugin.getResourceString("makensis.noconfig.option")); //$NON-NLS-1$
                    }
                    if(settings.getNoCD()) {
                        options.add(EclipseNSISPlugin.getResourceString("makensis.nocd.option")); //$NON-NLS-1$
                    }
                    Map symbols = settings.getSymbols();
                    if(symbols != null) {
                        for(Iterator iter=symbols.entrySet().iterator(); iter.hasNext(); ) {
                            Map.Entry entry = (Map.Entry)iter.next();
                            String key = (String)entry.getKey();
                            String value = (String)entry.getValue();
                            if(!Common.isEmpty(key)) {
                                StringBuffer buf = new StringBuffer(EclipseNSISPlugin.getResourceString("makensis.define.option")).append(key); //$NON-NLS-1$
                                if(!Common.isEmpty(value)) {
                                    buf.append("=").append(value); //$NON-NLS-1$
                                }
                                options.add(buf.toString());
                            }
                        }
                    }
                    List instructions = settings.getInstructions();
                    if(instructions != null) {
                        for(Iterator iter=instructions.iterator(); iter.hasNext(); ) {
                            String instruction = (String)iter.next();
                            if(!Common.isEmpty(instruction)) {
                                if(compressor == COMPRESSOR_DEFAULT ||
                                   !instruction.toLowerCase().startsWith(EclipseNSISPlugin.getResourceString("makensis.setcompressor.instruction").toLowerCase()+" ")) { //$NON-NLS-1$ //$NON-NLS-2$
                                    options.add(EclipseNSISPlugin.getResourceString("makensis.execute.option")+instruction); //$NON-NLS-1$
                                }
                            }
                        }
                    }
    
                    int rv = 0;
                    String[] optionsArray = (String[])options.toArray(new String[0]);
                    int cmdArrayLen = optionsArray.length+(compressor != COMPRESSOR_BEST?4:5);
                    String[] cmdArray = new String[cmdArrayLen];
                    cmdArray[0] = NSISPreferences.getPreferences().getNSISExe();
                    System.arraycopy(optionsArray,0,cmdArray,cmdArrayLen-optionsArray.length-3,optionsArray.length);
                    cmdArray[cmdArrayLen-3]=EclipseNSISPlugin.getResourceString("makensis.notifyhwnd.option"); //$NON-NLS-1$
                    cmdArray[cmdArrayLen-2]=Long.toString(cHwnd);
                    cmdArray[cmdArrayLen-1]=fileName;
                    if(compressor != COMPRESSOR_BEST) {
                        results = runProcess(cmdArray,env,workDir,outputProcessor);
                    }
                    else {
                        File tempFile = null;
                        String bestCompressor = null;
                        long bestFileSize = Long.MAX_VALUE;
                        for(int i=0; i<COMPRESSOR_NAME_ARRAY.length; i++) {
                            if(i != COMPRESSOR_DEFAULT && i != COMPRESSOR_BEST) {
                                cmdArray[1]=EclipseNSISPlugin.getResourceString("makensis.setcompressor.final.option")+COMPRESSOR_NAME_ARRAY[i]; //$NON-NLS-1$
                                MakeNSISResults tempresults = runProcess(cmdArray,env,workDir,outputProcessor);
                                if(tempresults.getReturnCode() != MakeNSISResults.RETURN_SUCCESS) {
                                    results = tempresults;
                                    if(tempFile != null && tempFile.exists()) {
                                        tempFile.delete();
                                    }
                                    tempFile = null;
                                    break;
                                }
                                else {
                                    File outputFile = new File(tempresults.getOutputFileName());
                                    if(outputFile.isFile() && outputFile.exists()) {
                                        if(bestCompressor == null || outputFile.length() < bestFileSize) {
                                            bestCompressor = COMPRESSOR_NAME_ARRAY[i];
                                            if(tempFile == null) {
                                                tempFile = new File(outputFile.getAbsolutePath()+EclipseNSISPlugin.getResourceString("makensis.tempfile.suffix")); //$NON-NLS-1$
                                            }
                                            if(tempFile.exists()) {
                                                tempFile.delete();
                                            }
                                            bestFileSize = outputFile.length();
                                            outputFile.renameTo(tempFile);
                                            results = tempresults;
                                        }
                                    }
                                }
                            }
                        }
                        if(tempFile != null && tempFile.exists()) {
                            File outputFile = new File(results.getOutputFileName());
                            if(!outputFile.exists()) {
                                tempFile.renameTo(outputFile);
                            }
                            console.add(NSISConsoleLine.info(MessageFormat.format(cBestCompressorFormat,
                                                                                  new Object[]{bestCompressor})));
                        }
                    }
                    rv = results.getReturnCode();
                    String outputFileName = results.getOutputFileName();
                    File exeFile = (outputFileName==null?null:new File(outputFileName));
                    try {
                        if(rv == 0 && exeFile != null && exeFile.exists()) {
                            file.setPersistentProperty(NSIS_COMPILE_TIMESTAMP,Long.toString(System.currentTimeMillis()));
                            file.setPersistentProperty(NSIS_EXE_NAME,outputFileName);
                            file.setPersistentProperty(NSIS_EXE_TIMESTAMP,Long.toString(exeFile.lastModified()));
                        }
                        else {
                            file.setPersistentProperty(NSIS_COMPILE_TIMESTAMP,null);
                            file.setPersistentProperty(NSIS_EXE_NAME,null);
                            file.setPersistentProperty(NSIS_EXE_TIMESTAMP,null);
                        }
                    }
                    catch(CoreException cex) {
                    }
                    try {
                        file.getProject().refreshLocal(IResource.DEPTH_INFINITE,null);
                    }
                    catch(CoreException cex) {
                    }
                }
                catch(IOException ioe){
                    console.add(NSISConsoleLine.error(ioe.getMessage()));
                }
            }
            return results;
        }
        finally {
            notifyListeners(false);
        }
    }
    
    private static synchronized MakeNSISResults runProcess(String[] cmdArray, String[] env, File workDir,
                                       INSISConsoleLineProcessor outputProcessor)
    {
        NSISConsole console = NSISConsole.getConsole();
        MakeNSISResults results = new MakeNSISResults();
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray,env,workDir);
            MakeNSISProcess process = new MakeNSISProcess(proc);
            setProcess(process);
            new Thread(new NSISConsoleWriter(process, console, proc.getInputStream(),outputProcessor)).start();
            new Thread(new NSISConsoleWriter(process, console, proc.getErrorStream(),
                        new INSISConsoleLineProcessor() {
                            public NSISConsoleLine processText(String text)
                            {
                                return NSISConsoleLine.error(text);
                            }
                        })).start();
    
            int rv = proc.waitFor();
            results.setReturnCode(rv);
            String outputFileName = null;
            if(results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS) {
                outputFileName = getOutputFileName();
                results.setOutputFileName(outputFileName);
            }
            else {
                ArrayList errors = getErrors();
                results.setErrors(errors);
                if(errors != null) {
                    for(Iterator iter=errors.iterator(); iter.hasNext(); ) {
                        console.add(NSISConsoleLine.error(((String)iter.next()).trim()));
                    }
                }
            }
            ArrayList warnings = getWarnings();
            results.setWarnings(warnings);
            if(warnings != null) {
                for(Iterator iter=warnings.iterator(); iter.hasNext(); ) {
                    console.add(NSISConsoleLine.warning(((String)iter.next()).trim()));
                }
            }
        }
        catch(IOException ioe){
            console.add(NSISConsoleLine.error(ioe.getMessage()));
        }
        catch(InterruptedException ie){
            console.add(NSISConsoleLine.error(ie.getMessage()));
        }
        finally {
            setProcess(null);
        }
        return results;
    }
    
    public static synchronized void startup()
    {
        if(cHwnd <= 0) {
            cHwnd = init();
        }
    }
    
    public static synchronized void shutdown()
    {
        if(cHwnd > 0) {
            destroy();
        }
    }
    
    private static native long init();
    
    private static native void destroy();

    private static native void reset();

    private static native String getScriptFileName(); 

    private static native String getOutputFileName(); 

    private static native ArrayList getErrors(); 

    private static native ArrayList getWarnings(); 
}
