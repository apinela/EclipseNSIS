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

public class WinAPI
{
    static {
        System.loadLibrary("WinAPI"); //$NON-NLS-1$
        init();
    }

    public static final int GWL_EXSTYLE = 0xffffffec;
    public static final int LWA_COLORKEY = 1;
    public static final int LWA_ALPHA = 2;
    public static final int WS_EX_LAYERED = 0x80000;
    public static final int HH_DISPLAY_TOPIC = 0x0;
    public static final int HKEY_CLASSES_ROOT = 0x80000000;
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002; 
    public static final int HKEY_USERS = 0x80000003;
    
    private static native void init();
    public static native long SetWindowLong(long hWnd, int nIndex, long dwNewLong);
    public static native long GetWindowLong(long hWnd, int nIndex);
    public static native boolean SetLayeredWindowAttributes(long hwnd, int red, int green, int blue, 
                                                            int bAlpha, long dwFlags);
    
    public static native String RegQueryStrValue(long hRootKey, String sSubKey, String sValue);
    public static native long GetDesktopWindow();
    public static native long HtmlHelp(long hwndCaller, String  pszFile, int uCommand, long dwData);
    public static native int GetUserDefaultLangID();
    public static native String ExtractHtmlHelpTOC(String pszFile, String pszFolder);
}
