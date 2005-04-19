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
    public static final int GWL_WNDPROC = 0xfffffffc;
    
    public static final int WM_NCHITTEST = 0x84;
    public static final int WM_SETFOCUS = 0x7;
    public static final int WM_KEYDOWN = 0x100;
    public static final int WM_CHAR = 0x102;
    public static final int WM_SYSCHAR = 0x106;
    
    public static final int LWA_COLORKEY = 1;
    public static final int LWA_ALPHA = 2;
    
    public static final int WS_EX_LAYERED = 0x80000;
    
    public static final int HH_DISPLAY_TOPIC = 0x0;
    
    public static final int HTTRANSPARENT = 0xffffffff;
    
    public static final int HKEY_CLASSES_ROOT = 0x80000000;
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002; 
    public static final int HKEY_USERS = 0x80000003;
    
    private static native void init();
    public static native int SetWindowLong(int hWnd, int nIndex, int dwNewLong);
    public static native int GetWindowLong(int hWnd, int nIndex);
    public static native boolean SetLayeredWindowAttributes(int hWnd, int red, int green, int blue, 
                                                            int bAlpha, int dwFlags);
    
    public static native String RegQueryStrValue(int hRootKey, String pszSubKey, String pszValue);
    public static native int GetDesktopWindow();
    public static native int HtmlHelp(int hwndCaller, String  pszFile, int uCommand, int dwData);
    public static native int GetUserDefaultLangID();
    public static native String ExtractHtmlHelpAndTOC(String pszFile, String pszFolder);
    public static native String[] GetPluginExports(String pszPluginFile);
    public static final native int SendMessage(int hWnd, int msg, int wParam, int lParam);
    public static final native int CallWindowProc(int lpWndProc, int hWnd, int Msg, int wParam, int lParam);
}
