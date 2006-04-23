/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.RegistryKeySelectionDialog;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class RegistryImporter
{
    public static final RegistryImporter INSTANCE = new RegistryImporter();
    
    private static final String REGEDIT_EXE = "regedit.exe"; //$NON-NLS-1$
    
    private static String[] cRegFileFilters = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filters"),','); //$NON-NLS-1$
    private static String[] cRegFileFilterNames = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filter.names"),','); //$NON-NLS-1$
    private static File cRegEdit = null;
    
    private static Map cRootKeyHandleMap = new CaseInsensitiveMap();

    private boolean mShowMultiSZWarning = true;

    private static void putRootKeyHandle(String longName, String shortName, int handle)
    {
        String hexHandle = "0x"+Integer.toHexString(handle);
        cRootKeyHandleMap.put(longName, hexHandle);
        cRootKeyHandleMap.put(shortName, hexHandle);
    }

    static {
        putRootKeyHandle("HKEY_CLASSES_ROOT","HKCR",WinAPI.HKEY_CLASSES_ROOT);
        putRootKeyHandle("HKEY_CURRENT_USER","HKCU",WinAPI.HKEY_CURRENT_USER);
        putRootKeyHandle("HKEY_LOCAL_MACHINE","HKLM",WinAPI.HKEY_LOCAL_MACHINE);
        putRootKeyHandle("HKEY_USERS","HKU",WinAPI.HKEY_USERS);
        putRootKeyHandle("HKEY_PERFORMANCE_DATA","HKPD",WinAPI.HKEY_PERFORMANCE_DATA);
        putRootKeyHandle("HKEY_CURRENT_CONFIG","HKCC",WinAPI.HKEY_CURRENT_CONFIG);
        putRootKeyHandle("HKEY_DYN_DATA","HKDD",WinAPI.HKEY_DYN_DATA);
    }

    public RegistryImporter()
    {
    }

    private File findRegEdit(Shell shell)
    {
        File regEdit = null;
        String pref = NSISPreferences.INSTANCE.getString(INSISPreferenceConstants.REGEDIT_LOCATION);
        if(!Common.isEmpty(pref)) {
            regEdit = new File(pref);
            if(IOUtility.isValidFile(regEdit)) {
                return regEdit;
            }
            regEdit = null;
        }
        String winDir = WinAPI.GetEnvironmentVariable("SystemRoot"); //$NON-NLS-1$
        if(winDir == null) {
            winDir = WinAPI.GetEnvironmentVariable("windir"); //$NON-NLS-1$
        }
        if(winDir != null) {
            regEdit = new File(winDir,REGEDIT_EXE);
        }
        if(IOUtility.isValidFile(regEdit)) {
            return regEdit;
        }
        regEdit = null;
        String path = WinAPI.GetEnvironmentVariable("PATH"); //$NON-NLS-1$
        if(!Common.isEmpty(path)) {
            String[] paths = Common.tokenize(path, System.getProperty("path.separator").charAt(0)); //$NON-NLS-1$
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
        
        Common.openWarning(shell, EclipseNSISPlugin.getResourceString("insert.regkey.messagebox.title"), EclipseNSISPlugin.getResourceString("select.regkey.dialog.title"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$ //$NON-NLS-2$
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFileName(REGEDIT_EXE);
        dialog.setText(EclipseNSISPlugin.getResourceString(EclipseNSISPlugin.getResourceString("select.regkey.dialog.message"))); //$NON-NLS-1$
        String file = dialog.open();
        if(file != null) {
            regEdit = new File(file);
            if(IOUtility.isValidFile(regEdit)) {
                return regEdit;
            }
        }
        return null;
    }

    private File getRegEdit(Shell shell)
    {
        if(!IOUtility.isValidFile(cRegEdit)) {
            cRegEdit = findRegEdit(shell);
            NSISPreferences.INSTANCE.setValue(INSISPreferenceConstants.REGEDIT_LOCATION, (cRegEdit==null?"":cRegEdit.getAbsolutePath())); //$NON-NLS-1$
        }
        return cRegEdit;
    }

    public void importRegKey(Shell shell, IRegistryImportStrategy callback)
    {
        File regEdit = getRegEdit(shell);
        if(regEdit == null) {
            Common.openError(shell, EclipseNSISPlugin.getResourceString("insert.regkey.messagebox.title"), EclipseNSISPlugin.getResourceString("missing.regedit.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            RegistryKeySelectionDialog dialog = new RegistryKeySelectionDialog(shell);
            if(dialog.open() == Window.OK) {
                try {
                    File regFile = File.createTempFile("exp", INSISConstants.REG_FILE_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
                    if (IOUtility.isValidFile(regFile)) {
                        regFile.delete();
                    }
                    String[] cmdArray = {regEdit.getAbsolutePath(),"/e", //$NON-NLS-1$
                                         regFile.getAbsolutePath(),
                                         dialog.getRegKey()};
                    Process p = Runtime.getRuntime().exec(cmdArray);
                    p.waitFor();
                    if (IOUtility.isValidFile(regFile)) {
                        importRegFile(shell, regFile.getAbsolutePath(), callback);
                    }
                    else {
                        throw new RuntimeException(EclipseNSISPlugin.getFormattedString("exec.regedit.error", //$NON-NLS-1$ 
                                new String[]{regEdit.getName()}));
                    }
                }
                catch (Exception e) {
                    Common.openError(shell, EclipseNSISPlugin.getResourceString("error.title"),  //$NON-NLS-1$
                            e.getMessage(), EclipseNSISPlugin.getShellImage());
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public void importRegFile(Shell shell, IRegistryImportStrategy callback)
    {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(cRegFileFilters);
        dialog.setFilterNames(cRegFileFilterNames);
        dialog.setText(EclipseNSISPlugin.getResourceString("insert.regfile.description")); //$NON-NLS-1$
        importRegFile(shell, dialog.open(), callback);
    }

    public void importRegFile(Shell shell, String filename, IRegistryImportStrategy callback)
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

                    String line = br.readLine();
                    if(line != null) {
                        if ( !(isRegEdit4 && line.equals("REGEDIT4")) && //$NON-NLS-1$
                             !(isRegEdit5 && line.equals("Windows Registry Editor Version 5.00"))) { //$NON-NLS-1$
                            throw ex;
                        }
                        String rootKey = null;
                        String subKey = null;
                        int count = 0;

                        while((line = br.readLine()) != null) {
                            line = line.trim();
                            if(line.length() == 0) {
                                if(rootKey != null && subKey != null && count == 0) {
                                    callback.addRegistryKey(rootKey, subKey);
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
                                            
                                            callback.beginRegistryKeySection(rootKey, subKey);
                                            if(rootKey.charAt(0) == '-') {
                                                rootKey = rootKey.substring(1);
                                                callback.deleteRegistryKey(rootKey, subKey);
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
                                                    callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_SZ, value);
                                                    count++;
                                                    continue;
                                                }
                                                else { 
                                                    if (value.equals("-")) { //$NON-NLS-1$
                                                        callback.deleteRegistryValue(rootKey, subKey, valueName);
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
                                                                callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_DWORD, value);
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
                                                                callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_BINARY, buf2.toString());
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
                                                                            bytes[i] = (byte)Integer.parseInt(values[i], 16);
                                                                        }
                                                                        else {
                                                                            bytes[i] = (byte)Integer.parseInt(values[i + 1], 16);
                                                                            bytes[i + 1] = (byte)Integer.parseInt(values[i], 16);
                                                                        }
                                                                    }
                                                                    callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_EXPAND_SZ, new String(bytes, (isRegEdit4?"8859_1":"UTF-16"))); //$NON-NLS-1$ //$NON-NLS-2$
                                                                    count++;
                                                                    continue;
                                                                }
                                                            }
                                                            else if (valueType.equals("hex(7)")) { //$NON-NLS-1$
                                                                if (mShowMultiSZWarning) {
                                                                    Common.openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), //$NON-NLS-1$
                                                                            EclipseNSISPlugin.getResourceString("reg.multistring.warning"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                                                    mShowMultiSZWarning = false;
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
    }

    public static final String rootKeyNameToHandle(String rootKey)
    {
        String handle = (String)cRootKeyHandleMap.get(rootKey);
        return (handle==null?"":handle);
    }
    
    public static interface IRegistryImportStrategy
    {
        public void reset();
        public void beginRegistryKeySection(String rootKey, String subKey);
        public void addRegistryKey(String rootKey, String subKey);
        public void deleteRegistryKey(String rootKey, String subKey);
        public void addRegistryValue(String rootKey, String subKey, String value, int type, String data);
        public void deleteRegistryValue(String rootKey, String subKey, String value);
    }
}
