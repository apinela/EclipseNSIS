/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.console.model.NSISConsoleModel;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class MakeNSISRunner implements INSISConstants
{
    public static final MakeNSISRunner INSTANCE = null;
    public static final Pattern MAKENSIS_SYNTAX_ERROR_PATTERN = Pattern.compile("[\\w]+ expects [0-9\\-\\+]+ parameters, got [0-9]\\."); //$NON-NLS-1$
    public static final Pattern MAKENSIS_ERROR_PATTERN = Pattern.compile("error in script \"(.+)\" on line (\\d+).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    public static final Pattern MAKENSIS_WARNING_PATTERN = Pattern.compile(".+\\((.+):(\\d+)\\).*"); //$NON-NLS-1$

    public static final String MAKENSIS_NOTIFYHWND_OPTION = EclipseNSISPlugin.getResourceString("makensis.notifyhwnd.option"); //$NON-NLS-1$
    public static final String MAKENSIS_EXECUTE_OPTION = EclipseNSISPlugin.getResourceString("makensis.execute.option"); //$NON-NLS-1$
    public static final String MAKENSIS_DEFINE_OPTION = EclipseNSISPlugin.getResourceString("makensis.define.option"); //$NON-NLS-1$
    public static final String MAKENSIS_NOCD_OPTION = EclipseNSISPlugin.getResourceString("makensis.nocd.option"); //$NON-NLS-1$
    public static final String MAKENSIS_NOCONFIG_OPTION = EclipseNSISPlugin.getResourceString("makensis.noconfig.option"); //$NON-NLS-1$
    public static final String MAKENSIS_LICENSE_OPTION = EclipseNSISPlugin.getResourceString("makensis.license.option"); //$NON-NLS-1$
    public static final String MAKENSIS_VERSION_OPTION = EclipseNSISPlugin.getResourceString("makensis.version.option"); //$NON-NLS-1$
    public static final String MAKENSIS_HDRINFO_OPTION = EclipseNSISPlugin.getResourceString("makensis.hdrinfo.option"); //$NON-NLS-1$
    public static final String MAKENSIS_VERBOSITY_OPTION = EclipseNSISPlugin.getResourceString("makensis.verbosity.option"); //$NON-NLS-1$
    public static final String MAKENSIS_CMDHELP_OPTION = EclipseNSISPlugin.getResourceString("makensis.cmdhelp.option"); //$NON-NLS-1$

    private static Long ZERO = new Long(0);
    private static MakeNSISProcess cCompileProcess = null;
    private static String cBestCompressorFormat = null;
    private static String cCompileLock = "lock"; //$NON-NLS-1$
    private static String cHwndLock = "lock"; //$NON-NLS-1$
    private static long cHwnd = 0;
    private static HashSet cListeners = new HashSet();
    public static final int COMPRESSOR_DEFAULT = 0;
    public static final int COMPRESSOR_BEST;
    public static final String[] COMPRESSOR_DISPLAY_ARRAY;
    public static final String[] COMPRESSOR_NAME_ARRAY;
    
    private static final MessageFormat cCompilationTimeFormat;
    private static final MessageFormat cTotalCompilationTimeFormat;
    
    static {
        cCompilationTimeFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("compilation.time.format")); //$NON-NLS-1$
        cTotalCompilationTimeFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("total.compilation.time.format")); //$NON-NLS-1$
        System.loadLibrary("MakeNSISRunner"); //$NON-NLS-1$
        cBestCompressorFormat = EclipseNSISPlugin.getResourceString("best.compressor.format"); //$NON-NLS-1$

        int index = COMPRESSOR_DEFAULT + 1;
        String[] compressorArray = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),
                                                            "compressors"); //$NON-NLS-1$
        COMPRESSOR_NAME_ARRAY = new String[compressorArray.length+2];
        COMPRESSOR_DISPLAY_ARRAY = new String[compressorArray.length+2];

        COMPRESSOR_NAME_ARRAY[COMPRESSOR_DEFAULT] = ""; //$NON-NLS-1$
        COMPRESSOR_DISPLAY_ARRAY[COMPRESSOR_DEFAULT] = EclipseNSISPlugin.getResourceString("compressor.default.text"); //$NON-NLS-1$
        for(int i=0; i<compressorArray.length; i++) {
            COMPRESSOR_NAME_ARRAY[index] = compressorArray[i];
            COMPRESSOR_DISPLAY_ARRAY[index] = EclipseNSISPlugin.getResourceString(new StringBuffer("compressor.").append( //$NON-NLS-1$
                                                                                                   compressorArray[i]).append(
                                                                                                   ".text").toString(), //$NON-NLS-1$
                                                                                  compressorArray[i].toUpperCase()); 
            index++;
        }
        COMPRESSOR_BEST = index;
        COMPRESSOR_NAME_ARRAY[COMPRESSOR_BEST] = "best"; //$NON-NLS-1$
        COMPRESSOR_DISPLAY_ARRAY[COMPRESSOR_BEST] = EclipseNSISPlugin.getResourceString("compressor.best.text"); //$NON-NLS-1$
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
        synchronized(cCompileLock) {
            if(cCompileProcess != null) {
                cCompileProcess.cancel();
                return cCompileProcess.isCanceled();
            }
        }
        return false;
    }

    public static boolean isCompiling()
    {
        synchronized(cCompileLock) {
            return (cCompileProcess != null);
        }
    }

    /**
     * @param process The process to set.
     */
    private static void setCompileProcess(MakeNSISProcess process)
    {
        synchronized(cCompileLock) {
            cCompileProcess = process;
        }
    }
    
    private static void updateMarkers(final IFile file, final NSISConsoleModel model, final MakeNSISResults results)
    {
        if (!results.getCanceled()) {
            WorkspaceModifyOperation op = new WorkspaceModifyOperation(file)
            {
                protected void execute(IProgressMonitor monitor)throws CoreException
                {
                    try {
                        file.deleteMarkers(PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                        List errors = model.getErrors();
                        if (Common.isEmptyCollection(errors)) {
                            errors = results.getErrors();
                            if (!Common.isEmptyCollection(errors)) {
                                IMarker marker = file.createMarker(PROBLEM_MARKER_ID);
                                marker.setAttribute(IMarker.SEVERITY,
                                                    IMarker.SEVERITY_ERROR);
                                marker.setAttribute(IMarker.MESSAGE,
                                        EclipseNSISPlugin.getFormattedString("makensis.error.format", //$NON-NLS-1$
                                                                         new String[]{(String)errors.get(0) }));
                                marker.setAttribute(IMarker.LINE_NUMBER, 1);
                            }
                        }
                        else {
                            Iterator iter = errors.iterator();
                            NSISConsoleLine error = (NSISConsoleLine)iter.next();
                            IMarker marker = file.createMarker(PROBLEM_MARKER_ID);
                            marker.setAttribute(IMarker.SEVERITY,
                                                IMarker.SEVERITY_ERROR);
                            marker.setAttribute(IMarker.MESSAGE, error.toString());
                            while (error.getLineNum() <= 0 && iter.hasNext()) {
                                error = (NSISConsoleLine) iter.next();
                            }
                            int lineNum = (error.getLineNum() > 0?error.getLineNum():1);
                            marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
                            //reset the linenum for errors which don't have a linenum
                            for(iter=errors.iterator(); iter.hasNext(); ) {
                                error = (NSISConsoleLine) iter.next();
                                if(error.getLineNum() <= 0) {
                                    error.setLineNum(lineNum);
                                }
                            }
                        }
                        List warnings = model.getWarnings();
                        if(Common.isEmptyCollection(warnings)) {
                            warnings = results.getWarnings();
                            if (warnings != null) {
                                CaseInsensitiveMap map = new CaseInsensitiveMap();
                                for (Iterator iter = warnings.iterator(); iter.hasNext();) {
                                    String text = (String) iter.next();
                                    if(text.toLowerCase().startsWith("warning:")) { //$NON-NLS-1$
                                        text = text.substring(8).trim();
                                    }
                                    if(!map.containsKey(text)) {
                                        IMarker marker = file.createMarker(PROBLEM_MARKER_ID);
                                        marker.setAttribute(IMarker.SEVERITY,
                                                            IMarker.SEVERITY_WARNING);
                                        Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text);
                                        if(matcher.matches()) {
                                            IFile file2 = file.getWorkspace().getRoot().getFileForLocation(new Path(matcher.group(1)));
                                            if(file2 != null && file.equals(file2)) {
                                                marker.setAttribute(IMarker.LINE_NUMBER, Integer.parseInt(matcher.group(2)));
                                            }
                                            else {
                                                marker.setAttribute(IMarker.LINE_NUMBER, 1);
                                            }
                                        }
                                        else {
                                            marker.setAttribute(IMarker.LINE_NUMBER, 1);
                                        }
                                    
                                        marker.setAttribute(IMarker.MESSAGE,text);
                                        map.put(text,text);
                                    }
                                }
                            }
                        }
                        else {
                            CaseInsensitiveMap map = new CaseInsensitiveMap();
                            for(Iterator iter=warnings.iterator(); iter.hasNext(); ) {
                                NSISConsoleLine warning = (NSISConsoleLine)iter.next();
                                if(warning.getLineNum() > 0) {
                                    String text = warning.toString().trim();
                                    if(text.toLowerCase().startsWith("warning:")) { //$NON-NLS-1$
                                        text = text.substring(8).trim();
                                    }
                                    if(!map.containsKey(text)) {
                                        IMarker marker = file.createMarker(PROBLEM_MARKER_ID);
                                        marker.setAttribute(IMarker.SEVERITY,
                                                            IMarker.SEVERITY_WARNING);
                                        marker.setAttribute(IMarker.MESSAGE, text);
                                        int lineNum = (warning.getLineNum() > 0?warning.getLineNum():1);
                                        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
                                        map.put(text,text);
                                    }
                                }
                            }
                        }
                    }
                    catch (CoreException ex) {
                        model.add(NSISConsoleLine.error(ex.getLocalizedMessage()));
                    }
                    finally {
                        monitor.done();
                    }
                }
            };
            try {
                op.run(null);
            }
            catch (InvocationTargetException e) {
                model.add(NSISConsoleLine.error(e.getLocalizedMessage()));
            }
            catch (InterruptedException e) {
                model.add(NSISConsoleLine.error(e.getLocalizedMessage()));
            }
        }
    }
    
    public static synchronized MakeNSISResults compile(IFile file, INSISConsoleLineProcessor outputProcessor)
    {
        notifyListeners(true);
        MakeNSISResults results = null;
        if(file != null) {
            NSISConsole.autoShowConsole();
            NSISConsoleModel.INSTANCE.clear();
            reset();
            String fileName = file.getLocation().toFile().getAbsolutePath();
            File workDir = file.getLocation().toFile().getParentFile();
            NSISSettings settings = NSISProperties.getProperties(file);
            ArrayList options = new ArrayList();
            if(settings.getVerbosity() != settings.getDefaultVerbosity()) {
                options.add(MAKENSIS_VERBOSITY_OPTION+settings.getVerbosity()); //$NON-NLS-1$
            }
            
            NSISKeywords keywords = NSISKeywords.INSTANCE;
            String setCompressorFinalOption = new StringBuffer(MAKENSIS_EXECUTE_OPTION).append(
                                                                                     keywords.getKeyword("SetCompressor")).append( //$NON-NLS-1$
                                                                                     " ").append(keywords.getKeyword("/FINAL")).append( //$NON-NLS-1$ //$NON-NLS-2$
                                                                                     " ").toString(); //$NON-NLS-1$
            String solidOption = keywords.getKeyword("/SOLID"); //$NON-NLS-1$
            boolean solidSupported = keywords.isValidKeyword(solidOption);
            int compressor = settings.getCompressor();
            boolean solidCompression = settings.getSolidCompression();
            if(compressor != MakeNSISRunner.COMPRESSOR_DEFAULT &&
               compressor != MakeNSISRunner.COMPRESSOR_BEST) {
                StringBuffer buf = new StringBuffer(setCompressorFinalOption);
                if(solidCompression && solidSupported) {
                    buf.append(solidOption).append(" "); //$NON-NLS-1$
                }
                buf.append(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[compressor]);
                options.add(buf.toString()); //$NON-NLS-1$
            }
            if(settings.getHdrInfo()) {
                options.add(MAKENSIS_HDRINFO_OPTION); //$NON-NLS-1$
            }
            if(settings.getLicense()) {
                options.add(MAKENSIS_LICENSE_OPTION); //$NON-NLS-1$
            }
            if(settings.getNoConfig()) {
                options.add(MAKENSIS_NOCONFIG_OPTION); //$NON-NLS-1$
            }
            if(settings.getNoCD()) {
                options.add(MAKENSIS_NOCD_OPTION); //$NON-NLS-1$
            }
            Map symbols = settings.getSymbols();
            if(symbols != null) {
                for(Iterator iter=symbols.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    String key = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    if(!Common.isEmpty(key)) {
                        StringBuffer buf = new StringBuffer(MAKENSIS_DEFINE_OPTION).append(key); //$NON-NLS-1$
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
                        if(compressor == MakeNSISRunner.COMPRESSOR_DEFAULT ||
                           !instruction.toLowerCase().startsWith(keywords.getKeyword("SetCompressor").toLowerCase()+" ")) { //$NON-NLS-1$ //$NON-NLS-2$
                            options.add(MAKENSIS_EXECUTE_OPTION+instruction); //$NON-NLS-1$
                        }
                    }
                }
            }

            int rv = 0;
            String[] optionsArray = (String[])options.toArray(Common.EMPTY_STRING_ARRAY);
            int cmdArrayLen = optionsArray.length+(compressor != MakeNSISRunner.COMPRESSOR_BEST?3:4);
            String[] cmdArray = new String[cmdArrayLen];
            System.arraycopy(optionsArray,0,cmdArray,cmdArrayLen-optionsArray.length-3,optionsArray.length);
            cmdArray[cmdArrayLen-3]=MAKENSIS_NOTIFYHWND_OPTION; //$NON-NLS-1$
            cmdArray[cmdArrayLen-2]=Long.toString(cHwnd);
            cmdArray[cmdArrayLen-1]=fileName;
            try {
                if(compressor != MakeNSISRunner.COMPRESSOR_BEST) {
                    results = runCompileProcess(cmdArray,null,workDir,outputProcessor);
                }
                else {
                    long n = System.currentTimeMillis();
                    int count = solidSupported?2:1;
                    File tempFile = null;
                    String bestCompressor = null;
                    long bestFileSize = Long.MAX_VALUE;
                    int padding;
                    try {
                        padding = Integer.parseInt(EclipseNSISPlugin.getResourceString("summary.compressor.name.padding")); //$NON-NLS-1$
                    }
                    catch(Exception e) {
                        padding = 31;
                    }
                    List compressorSummaryList = new ArrayList();
                    int maxLineLength = 0;
                    outer:
                    for(int i=0; i<MakeNSISRunner.COMPRESSOR_NAME_ARRAY.length; i++) {
                        if(i != MakeNSISRunner.COMPRESSOR_DEFAULT && i != MakeNSISRunner.COMPRESSOR_BEST) {
                            for(int j=0; j<count; j++) {
                                outputProcessor.reset();
                                StringBuffer buf = new StringBuffer(setCompressorFinalOption);
                                if(j == 1 && solidSupported) {
                                    buf.append(solidOption).append(" "); //$NON-NLS-1$
                                }
                                buf.append(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[i]);
                                cmdArray[0]=buf.toString(); //$NON-NLS-1$

                                buf = new StringBuffer(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[i]);
                                if(j == 1) {
                                    buf.append(" ").append(solidOption); //$NON-NLS-1$
                                }
                                String compressorName = buf.toString();
                                String summaryCompressorName = Common.padString(EclipseNSISPlugin.getFormattedString("summary.compressor.name.format",new String[]{compressorName}),padding); //$NON-NLS-1$
                                MakeNSISResults tempresults = runCompileProcess(cmdArray,null,workDir,outputProcessor);
                                if(tempresults.getReturnCode() != MakeNSISResults.RETURN_SUCCESS) {
                                    results = tempresults;
                                    if(tempFile != null && tempFile.exists()) {
                                        tempFile.delete();
                                    }
                                    tempFile = null;
                                    break outer;
                                }
                                else {
                                    File outputFile = new File(tempresults.getOutputFileName());
                                    if(outputFile.isFile() && outputFile.exists()) {
                                        summaryCompressorName = EclipseNSISPlugin.getFormattedString("summary.line.format",new Object[]{summaryCompressorName,new Long(outputFile.length())}); //$NON-NLS-1$
                                        maxLineLength = Math.max(summaryCompressorName.length(),maxLineLength);
                                        compressorSummaryList.add(summaryCompressorName);
                                        if(bestCompressor == null || outputFile.length() < bestFileSize) {
                                            bestCompressor = compressorName;
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
                    }
                    if(tempFile != null && tempFile.exists()) {
                        File outputFile = new File(results.getOutputFileName());
                        if(!outputFile.exists()) {
                            tempFile.renameTo(outputFile);
                        }
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info("")); //$NON-NLS-1$
                        String header = EclipseNSISPlugin.getResourceString("summary.header"); //$NON-NLS-1$
                        maxLineLength = Math.max(maxLineLength,header.length());
                        char[] c = new char[maxLineLength];
                        Arrays.fill(c,'-');
                        String dashes = new String(c);
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(EclipseNSISPlugin.getResourceString("summary.title"))); //$NON-NLS-1$
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(dashes));
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(header));
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(dashes));
                        for (Iterator iter = compressorSummaryList.iterator(); iter.hasNext();) {
                            NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info((String)iter.next()));
                        }
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(dashes));
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info("")); //$NON-NLS-1$
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(MessageFormat.format(cBestCompressorFormat,
                                                                              new Object[]{bestCompressor})));
                    }
                    if(results.getReturnCode() != MakeNSISResults.RETURN_CANCEL) {
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(cTotalCompilationTimeFormat.format(splitCompilationTime(System.currentTimeMillis()-n))));
                    }
                }
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            finally {
                rv = results.getReturnCode();
                if(rv == MakeNSISResults.RETURN_CANCEL) {
                    NSISConsoleModel.INSTANCE.add(NSISConsoleLine.error(EclipseNSISPlugin.getResourceString("cancel.message"))); //$NON-NLS-1$
                }
                String outputFileName = results.getOutputFileName();
                File exeFile = (outputFileName==null?null:new File(outputFileName));
                try {
                    if(rv == MakeNSISResults.RETURN_SUCCESS && exeFile != null && exeFile.exists()) {
                        file.setPersistentProperty(NSIS_COMPILE_TIMESTAMP,Long.toString(System.currentTimeMillis()));
                        file.setPersistentProperty(NSIS_EXE_NAME,outputFileName);
                        file.setPersistentProperty(NSIS_EXE_TIMESTAMP,Long.toString(exeFile.lastModified()));
                    }
                    else {
                        file.setPersistentProperty(NSIS_COMPILE_TIMESTAMP,null);
                        file.setPersistentProperty(NSIS_EXE_NAME,null);
                        file.setPersistentProperty(NSIS_EXE_TIMESTAMP,null);
                        
                    }
                    updateMarkers(file, NSISConsoleModel.INSTANCE, results);
                }
                catch(CoreException cex) {
                    cex.printStackTrace();
                }
                notifyListeners(false);
            }
            try {
                file.getProject().refreshLocal(IResource.DEPTH_INFINITE,null);
            }
            catch(CoreException cex) {
                cex.printStackTrace();
            }
        }
        return results;
    }
    
    private static synchronized MakeNSISResults runCompileProcess(String[] cmdArray, String[] env, File workDir,
                                                        INSISConsoleLineProcessor outputProcessor)
    {
        long n = System.currentTimeMillis();
        MakeNSISResults results = new MakeNSISResults();
        try {
            MakeNSISProcess proc = createProcess(NSISPreferences.INSTANCE.getNSISExe(),cmdArray,env,workDir);
            setCompileProcess(proc);
            Process process = proc.getProcess();
            InputStream inputStream = process.getInputStream();
            new Thread(new NSISConsoleWriter(proc, NSISConsoleModel.INSTANCE, inputStream,outputProcessor)).start();
            InputStream errorStream = process.getErrorStream();
            new Thread(new NSISConsoleWriter(proc, NSISConsoleModel.INSTANCE, errorStream,
                        new INSISConsoleLineProcessor() {
                            public NSISConsoleLine processText(String text)
                            {
                                return NSISConsoleLine.error(text);
                            }
                            
                            public void reset()
                            {
                            }
                        })).start();
    
            int rv = process.waitFor();
            if(proc.isCanceled()) {
                rv = MakeNSISResults.RETURN_CANCEL;
            }
            results.setReturnCode(rv);
            String outputFileName = null;
            if(results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS) {
                outputFileName = getOutputFileName();
                results.setOutputFileName(outputFileName);
            }
            else {
                results.setCanceled(proc.isCanceled());
                ArrayList errors = null;
                ArrayList compileErrors = getErrors();
                if(!Common.isEmptyCollection(compileErrors)) {
                    errors = new ArrayList();
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    for(Iterator iter = compileErrors.iterator(); iter.hasNext() ;) {
                        String text = (String)iter.next();
                        if(text.endsWith("\n")) { //$NON-NLS-1$
                            buf.append(text.substring(0,text.length()-1));
                            errors.add(buf.toString());
                            buf = new StringBuffer(""); //$NON-NLS-1$
                        }
                        else {
                            buf.append(text);
                        }
                    }
                    errors.add(buf.toString());
                }
                results.setErrors(errors);
            }
            ArrayList warnings = getWarnings();
            results.setWarnings(warnings);
        }
        catch(IOException ioe){
            NSISConsoleModel.INSTANCE.add(NSISConsoleLine.error(ioe.getLocalizedMessage()));
        }
        catch(InterruptedException ie){
            NSISConsoleModel.INSTANCE.add(NSISConsoleLine.error(ie.getLocalizedMessage()));
        }
        finally {
            setCompileProcess(null);
            if(results.getReturnCode() != MakeNSISResults.RETURN_CANCEL) {
                NSISConsoleModel.INSTANCE.add(NSISConsoleLine.info(cCompilationTimeFormat.format(splitCompilationTime(System.currentTimeMillis()-n))));
            }
        }
        return results;
    }

    private static Long[] splitCompilationTime(long time)
    {
        Long[] result = new Long[3];
        Arrays.fill(result,ZERO);
        result[2] = new Long(time % 1000);
        time /= 1000;
        for(int i=1; i>=0; i--) {
            if(time > 0) {
                result[i] = new Long(time % 60);
                time /= 60;
            }
            else {
                break;
            }
        }
        
        return result;
    }
    
    public static void testInstaller(String exeName)
    {
        if(exeName != null) {
            File exeFile = new File(exeName);
            if (exeFile.exists()) {
                File workDir = exeFile.getParentFile();
                try {
                    Runtime.getRuntime().exec(new String[]{exeName},null,workDir);
                }
                catch(IOException ex) {
                    if(NSISConsoleModel.INSTANCE != null) {
                        NSISConsoleModel.INSTANCE.clear();
                        NSISConsoleModel.INSTANCE.add(NSISConsoleLine.error(ex.getLocalizedMessage()));
                    }
                    else {
                        Common.openError(EclipseNSISPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
                                         ex.getLocalizedMessage());
                    }
                }
            }
        }
    } 
    
    public static void startup()
    {
        if(cHwnd <= 0) {
            synchronized(cHwndLock) {
                if(cHwnd <= 0) {
                    cHwnd = init();
                }
            }
        }
    }
    
    public static void shutdown()
    {
        if(cHwnd > 0) {
            synchronized(cHwndLock) {
                if(cHwnd > 0) {
                    destroy();
                    cHwnd = 0;
                }
            }
        }
    }

    public static String[] runProcessWithOutput(String makensisExe, String[] cmdArray, File workDir)
    {
        return runProcessWithOutput(makensisExe, cmdArray, workDir, 0);
    }

    public static String[] runProcessWithOutput(String makensisExe, String[] cmdArray, File workDir, int validReturnCode)
    {
        String[] output = null;
        try {
            MakeNSISProcess proc = createProcess(makensisExe, cmdArray, null, workDir);
            Process process = proc.getProcess();
            new Thread(new RunnableInputStreamReader(process.getErrorStream(),false)).start();
            output = new RunnableInputStreamReader(process.getInputStream()).getOutput();
            int rv = process.waitFor();
            Common.closeIO(process.getOutputStream());
            if(rv != validReturnCode) {
                output = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            output = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            output = null;
        }
    
        return output;
    }
    
    private static MakeNSISProcess createProcess(String makeNSISExe, String[] cmdArray, String[] env, File workDir) throws IOException
    {
        MakeNSISProcess proc;
        if(EclipseNSISPlugin.getDefault().isNT()) {
            String[] newCmdArray = new String[1+ (Common.isEmptyArray(cmdArray)?0:cmdArray.length)];
            newCmdArray[0] = makeNSISExe;
            if(!Common.isEmptyArray(cmdArray)) {
                System.arraycopy(cmdArray,0,newCmdArray,1,cmdArray.length);
            }
            proc = new MakeNSISProcess(Runtime.getRuntime().exec(newCmdArray,env,workDir));
        }
        else {
            proc = new MakeNSISProcess(makeNSISExe, cmdArray, env, workDir);
        }
        return proc;
    }

    private static native long init();
    
    private static native void destroy();

    private static native void reset();

    private static native String getOutputFileName(); 

    private static native ArrayList getErrors(); 

    private static native ArrayList getWarnings();
}
