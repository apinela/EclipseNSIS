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

import java.io.*;
import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.RegistryKeySelectionDialog;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class RegistryImporter
{
    private static final String REGEDIT_EXE = "regedit.exe";
    
    private static MessageFormat cDeleteRegKeyFormat=new MessageFormat("{0} {1} {2}"); //$NON-NLS-1$
    private static MessageFormat cDeleteRegValueFormat=new MessageFormat("{0} {1} {2} {3}"); //$NON-NLS-1$
    private static MessageFormat cWriteRegValueFormat=new MessageFormat("{0} {1} {2} {3} {4}"); //$NON-NLS-1$
    private static MessageFormat cCommentFormat=new MessageFormat(EclipseNSISPlugin.getResourceString("regfile.comment.format")); //$NON-NLS-1$
    private static String[] cRegFileFilters = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filters"),','); //$NON-NLS-1$
    private static String[] cRegFileFilterNames = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filter.names"),','); //$NON-NLS-1$
    private static File cRegEdit = null;

    private RegistryImporter()
    {
    }

    private static File findRegEdit(Shell shell)
    {
        File regEdit = null;
        String pref = NSISPreferences.INSTANCE.getPreferenceStore().getString(INSISPreferenceConstants.REGEDIT_LOCATION);
        if(!Common.isEmpty(pref)) {
            regEdit = new File(pref);
            if(IOUtility.isValidFile(regEdit)) {
                return regEdit;
            }
            regEdit = null;
        }
        String winDir = WinAPI.GetEnvironmentVariable("SystemRoot");
        if(winDir == null) {
            winDir = WinAPI.GetEnvironmentVariable("windir");
        }
        if(winDir != null) {
            regEdit = new File(winDir,REGEDIT_EXE);
        }
        if(IOUtility.isValidFile(regEdit)) {
            return regEdit;
        }
        regEdit = null;
        String path = WinAPI.GetEnvironmentVariable("PATH");
        if(!Common.isEmpty(path)) {
            String[] paths = Common.tokenize(path, System.getProperty("path.separator").charAt(0));
            if(!Common.isEmptyArray(paths)) {
                for (int i = 0; i < paths.length; i++) {
                    if(!paths[i].equalsIgnoreCase(winDir)) {
                        regEdit = new File(paths[i],REGEDIT_EXE);
                        if(IOUtility.isValidFile(regEdit)) {
                            return regEdit;
                        }
                        regEdit = null;
                    }
                }
            }
        }
        
        Common.openWarning(shell, "Import Registry Key", "Please select the location of regedit.exe", EclipseNSISPlugin.getShellImage());
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFileName(REGEDIT_EXE);
        dialog.setText(EclipseNSISPlugin.getResourceString("Select the location of regedit.exe"));
        String file = dialog.open();
        if(file != null) {
            regEdit = new File(file);
            if(IOUtility.isValidFile(regEdit)) {
                return regEdit;
            }
        }
        return null;
    }

    private static File getRegEdit(Shell shell)
    {
        if(!IOUtility.isValidFile(cRegEdit)) {
            cRegEdit = findRegEdit(shell);
            NSISPreferences.INSTANCE.getPreferenceStore().setValue(INSISPreferenceConstants.REGEDIT_LOCATION, (cRegEdit==null?"":cRegEdit.getAbsolutePath()));
        }
        return cRegEdit;
    }

    public static final String importRegKey(Shell shell)
    {
        File regEdit = getRegEdit(shell);
        if(regEdit == null) {
            Common.openError(shell, "Import Registry Key", "Cannot find "+REGEDIT_EXE, EclipseNSISPlugin.getShellImage());
        }
        else {
            RegistryKeySelectionDialog dialog = new RegistryKeySelectionDialog(shell);
            if(dialog.open() == Window.OK) {
                try {
                    File regFile = File.createTempFile("exp", ".reg");
                    if (IOUtility.isValidFile(regFile)) {
                        regFile.delete();
                    }
                    String[] cmdArray = {regEdit.getAbsolutePath(),"/e",
                                         regFile.getAbsolutePath(),
                                         dialog.getRegKey()};
                    Process p = Runtime.getRuntime().exec(cmdArray);
                    p.waitFor();
                    if (IOUtility.isValidFile(regFile)) {
                        return importRegFile(shell, regFile.getAbsolutePath());
                    }
                    else {
                        throw new RuntimeException("Error executing "+regEdit.getName());
                    }
                }
                catch (Exception e) {
                    Common.openError(shell, EclipseNSISPlugin.getResourceString("error.title"),  //$NON-NLS-1$
                            e.getMessage(), EclipseNSISPlugin.getShellImage());
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
        return null;
    }

    public static final String importRegFile(Shell shell)
    {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(cRegFileFilters);
        dialog.setFilterNames(cRegFileFilterNames);
        dialog.setText(EclipseNSISPlugin.getResourceString("import.regfile.description")); //$NON-NLS-1$
        return importRegFile(shell, dialog.open());
    }

    private static String importRegFile(Shell shell, String filename)
    {
        if(!Common.isEmpty(filename)) {
            File regFile = new File(filename);
            if(IOUtility.isValidFile(regFile)) {
                FileInputStream fis = null;
                BufferedReader br = null;
                try {
                    RuntimeException ex = new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$

                    boolean isRegEdit4 = false;
                    boolean isRegEdit5 = false;

                    byte[] bytes = new byte[2];
                    fis = new FileInputStream(regFile);
                    int n = fis.read(bytes);
                    if(n < bytes.length) {
                        throw ex;
                    }
                    if(bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) {
                        isRegEdit5 = true;
                        fis.close();
                        fis = new FileInputStream(regFile);
                        br = new BufferedReader(new InputStreamReader(fis,"UTF-16")); //$NON-NLS-1$
                    }
                    else if(bytes[0] == 'R' && bytes[1] == 'E') {
                        fis.close();
                        fis = new FileInputStream(regFile);
                        isRegEdit4 = true;
                        br = new BufferedReader(new InputStreamReader(fis,"8859_1")); //$NON-NLS-1$
                    }
                    else {
                        throw ex;
                    }

                    String deleteRegKey = NSISKeywords.getInstance().getKeyword("DeleteRegKey"); //$NON-NLS-1$
                    String writeRegStr = NSISKeywords.getInstance().getKeyword("WriteRegStr"); //$NON-NLS-1$
                    String deleteRegValue = NSISKeywords.getInstance().getKeyword("DeleteRegValue"); //$NON-NLS-1$
                    String writeRegDWORD = NSISKeywords.getInstance().getKeyword("WriteRegDWORD"); //$NON-NLS-1$
                    String writeRegExpandStr = NSISKeywords.getInstance().getKeyword("WriteRegExpandStr"); //$NON-NLS-1$
                    String writeRegBin = NSISKeywords.getInstance().getKeyword("WriteRegBin"); //$NON-NLS-1$

                    String line = br.readLine();
                    if(line != null) {
                        if ( !(isRegEdit4 && line.equals("REGEDIT4")) && //$NON-NLS-1$
                             !(isRegEdit5 && line.equals("Windows Registry Editor Version 5.00"))) { //$NON-NLS-1$
                            throw ex;
                        }
                        int textLimit;
                        try {
                            textLimit = Integer.parseInt(NSISPreferences.INSTANCE.getNSISDefaultSymbol("NSIS_MAX_STRLEN")); //$NON-NLS-1$
                        }
                        catch(Exception e){
                            textLimit = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
                        }
                        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                        String rootKey = null;
                        String subKey = null;
                        boolean showMultiSZWarning = true;
                        int count = 0;

                        while((line = br.readLine()) != null) {
                            line = line.trim();
                            if(line.length() == 0) {
                                if(rootKey != null && subKey != null && count == 0) {
                                    addLineToBuf(buf, makeRegCommand(cWriteRegValueFormat, 
                                            new String[]{writeRegStr, rootKey, subKey, "", ""}), textLimit); //$NON-NLS-1$ //$NON-NLS-2$
                                    addLineToBuf(buf, makeRegCommand(cDeleteRegValueFormat, 
                                            new String[]{deleteRegValue, rootKey, subKey, ""}), textLimit); //$NON-NLS-1$
                                }
                                rootKey = null;
                                subKey = null;
                                count = 0;
                            }
                            else {
                                if(rootKey == null) {
                                    if(line.charAt(0) == '[' && line.charAt(line.length()-1) == ']') {
                                        n = line.indexOf('\\');
                                        if(n > 1 && n < line.length()-2) {
                                            rootKey = line.substring(1,n).toUpperCase();
                                            subKey = line.substring(n+1,line.length()-1);
                                            
                                            if(buf.length() > 0) {
                                                buf.append(INSISConstants.LINE_SEPARATOR);
                                            }
                                            buf.append(cCommentFormat.format(new String[] {line})).append(
                                                    INSISConstants.LINE_SEPARATOR);
                                            if(rootKey.charAt(0) == '-') {
                                                rootKey = rootKey.substring(1);
                                                addLineToBuf(buf,
                                                             makeRegCommand(cDeleteRegKeyFormat, 
                                                                            new String[] {deleteRegKey,rootKey,subKey}),
                                                             textLimit);
                                                rootKey = null;
                                                subKey = null;
                                            }
                                            continue;
                                        }
                                    }
                                }
                                else {
                                    if(line.charAt(line.length()-1) =='\\') {
                                        StringBuffer buf2 = new StringBuffer(line.substring(0,line.length()-1));
                                        line = br.readLine();
                                        while(line != null) {
                                            line = line.trim();
                                            if(line.charAt(line.length()-1) =='\\') {
                                                buf2.append(line.substring(0,line.length()-1));
                                                line = br.readLine();
                                            }
                                            else {
                                                buf2.append(line);
                                                line = buf2.toString();
                                                break;
                                            }
                                        }
                                    }
                                    if (line != null) {
                                        //Unescape \ character
                                        line = Common.replaceAll(line, "\\\\", "\\", false); //$NON-NLS-1$ //$NON-NLS-2$
                                        n = line.indexOf('=');
                                        if (n > 0) {
                                            String valueName = line.substring(0, n); //remove the quotes
                                            String value = line.substring(n + 1);
                                            if (valueName.equals("@")) { //$NON-NLS-1$
                                                valueName = ""; //$NON-NLS-1$
                                            }
                                            else if (!Common.isQuoted(valueName)) {
                                                valueName = null;
                                            }
                                            else {
                                                valueName = valueName.substring(1, valueName.length() - 1);
                                            }
                                            if (valueName != null) {
                                                if (Common.isQuoted(value)) {
                                                    value = value.substring(1, value.length() - 1);
                                                    addLineToBuf(buf, makeRegCommand(cWriteRegValueFormat, new String[]{writeRegStr, rootKey, subKey, valueName, value}), textLimit);
                                                    count++;
                                                    continue;
                                                }
                                                else { //$NON-NLS-1$
                                                    if (value.equals("-")) { //$NON-NLS-1$
                                                        addLineToBuf(buf, makeRegCommand(cDeleteRegValueFormat, new String[]{deleteRegValue, rootKey, subKey, valueName}), textLimit);
                                                        continue;
                                                    }
                                                    else {
                                                        n = value.indexOf(':');
                                                        if (n > 0) {
                                                            String valueType = value.substring(0, n);
                                                            value = value.substring(n + 1);
                                                            if (valueType.equals("dword")) { //$NON-NLS-1$
                                                                //Validate that it is really a hex value
                                                                Integer.parseInt(value, 16);
                                                                addLineToBuf(buf, makeRegCommand(cWriteRegValueFormat, new String[]{writeRegDWORD, rootKey, subKey, valueName, "0x" + value}), //$NON-NLS-1$
                                                                        textLimit);
                                                                count++;
                                                                continue;
                                                            }
                                                            else if (valueType.equals("hex")) { //$NON-NLS-1$
                                                                StringBuffer buf2 = new StringBuffer(""); //$NON-NLS-1$
                                                                String[] values = Common.tokenize(value, ',');
                                                                if (!Common.isEmptyArray(values)) {
                                                                    for (int i = 0; i < values.length; i++) {
                                                                        //Validate that it is really a hex value
                                                                        Integer.parseInt(values[i], 16);
                                                                        buf2.append(values[i]);
                                                                    }
                                                                }
                                                                addLineToBuf(buf, makeRegCommand(cWriteRegValueFormat, new String[]{writeRegBin, rootKey, subKey, valueName, buf2.toString()}),
                                                                        textLimit);
                                                                count++;
                                                                continue;
                                                            }
                                                            else if (valueType.equals("hex(2)")) { //$NON-NLS-1$
                                                                //Expandable String
                                                                String[] values = Common.tokenize(value, ',');
                                                                if (!Common.isEmptyArray(values) && values.length % 2 == 0) {
                                                                    int delta = (isRegEdit4?1:2);
                                                                    bytes = new byte[values.length - delta]; //Last character is NULL
                                                                    for (int i = 0; i < bytes.length; i += delta) {
                                                                        if (isRegEdit4) {
                                                                            bytes[i] = Byte.parseByte(values[i], 16);
                                                                        }
                                                                        else {
                                                                            bytes[i] = Byte.parseByte(values[i + 1], 16);
                                                                            bytes[i + 1] = Byte.parseByte(values[i], 16);
                                                                        }
                                                                    }
                                                                    addLineToBuf(buf, makeRegCommand(cWriteRegValueFormat, new String[]{writeRegExpandStr, rootKey, subKey, valueName,
                                                                            new String(bytes, (isRegEdit4?"8859_1":"UTF-16"))}), textLimit); //$NON-NLS-1$ //$NON-NLS-2$
                                                                    count++;
                                                                    continue;
                                                                }
                                                            }
                                                            else if (valueType.equals("hex(7)")) { //$NON-NLS-1$
                                                                if (showMultiSZWarning) {
                                                                    Common.openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), //$NON-NLS-1$
                                                                            EclipseNSISPlugin.getResourceString("reg.multistring.warning"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                                                    showMultiSZWarning = false;
                                                                }
                                                                continue;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                throw ex;
                            }
                        }
                        return buf.toString();
                    }
                    else {
                        throw ex;
                    }
                }
                catch (Exception e) {
                    Common.openError(shell, EclipseNSISPlugin.getResourceString("error.title"),  //$NON-NLS-1$
                            e.getMessage(), EclipseNSISPlugin.getShellImage());
                    EclipseNSISPlugin.getDefault().log(e);
                }
                finally {
                    IOUtility.closeIO(br);
                    IOUtility.closeIO(fis);
                }
            }
        }
        return null;
    }

    private static String makeRegCommand(MessageFormat format, String[] args)
    {
        for(int i=0; i<args.length; i++) {
            args[i] = Common.maybeQuote(args[i]==null?"":args[i]); //$NON-NLS-1$
        }
        if(args[args.length-1].endsWith("\\")) { //$NON-NLS-1$
            args[args.length-1] = Common.quote(args[args.length-1]);
        }
        return format.format(args);
    }

    private static void addLineToBuf(StringBuffer buf, String line, int maxLen)
    {
        line = NSISKeywords.getInstance().replaceShellConstants(line);
        while(line.length() > maxLen) {
            buf.append(line.substring(0, maxLen-1)).append(
                    INSISConstants.LINE_CONTINUATION_CHAR).append(
                    INSISConstants.LINE_SEPARATOR);
            line = line.substring(maxLen-1);
        }
        buf.append(line).append(INSISConstants.LINE_SEPARATOR);
    }
}
