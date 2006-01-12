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

import java.net.Authenticator;

public class WinAPI
{
    static {
        System.loadLibrary("WinAPI"); //$NON-NLS-1$
        init();
    }

    public static final int GWL_EXSTYLE = 0xffffffec;
    public static final int GWL_STYLE = 0xfffffff0;
    public static final int GWL_WNDPROC = 0xfffffffc;

    public static final int WM_NCHITTEST = 0x84;
    public static final int WM_SETFOCUS = 0x7;
    public static final int WM_KEYDOWN = 0x100;
    public static final int WM_CHAR = 0x102;
    public static final int WM_SYSCHAR = 0x106;

    public static final int LWA_COLORKEY = 1;
    public static final int LWA_ALPHA = 2;

    public static final int WS_EX_LAYERED = 0x80000;
    public static final int WS_EX_LAYOUTRTL = 0x00400000;

    public static final int HH_DISPLAY_TOPIC = 0x0;

    public static final int HTTRANSPARENT = 0xffffffff;

    public static final int HKEY_CLASSES_ROOT = 0x80000000;
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    public static final int HKEY_USERS = 0x80000003;
    public static final int HKEY_PERFORMANCE_DATA = 0x80000004;
    public static final int HKEY_CURRENT_CONFIG = 0x80000005;
    public static final int HKEY_DYN_DATA = 0x80000006;

    public static final int BS_LEFTTEXT = 0x20;
    public static final int CB_SHOWDROPDOWN = 0x14f;
    public static final int CB_GETDROPPEDSTATE = 0x157;

    public static final int WM_PRINT = 0x317;
    public static final int PRF_NONCLIENT = 0x2;
    public static final int PRF_CLIENT = 0x4;
    public static final int PRF_ERASEBKGND = 0x8;
    public static final int PRF_CHILDREN = 0x10;

    public static final int EP_EDITTEXT = 1;
    public static final int ETS_NORMAL = 1;
    public static final int ETS_DISABLED = 4;
    public static final int ETS_READONLY = 6;

    public static final int LVP_LISTITEM = 1;
    public static final int LIS_NORMAL = 1;
    public static final int LIS_DISABLED = 4;

    public static final int COLOR_GRAYTEXT = 0x11;
    public static final int COLOR_3DHILIGHT = 0x14;

    public static final int WS_HSCROLL = 0x100000;
    public static final int WS_VSCROLL = 0x200000;

    public static final int LB_SETHORIZONTALEXTENT = 0x194;

    public static final int SM_CXVSCROLL = 0x2;
    public static final int SM_CYVSCROLL = 0x14;
    public static final int SM_CYHSCROLL = 0x3;

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

    public static final native boolean AreVisualStylesEnabled();
    
    public static final native void DrawWidgetThemeBackGround(int hWnd, int hDC, String theme, int partId, int stateId);

    public static final native int GetSysColor(int index);

    public static final native int GetSystemMetrics (int nIndex);

    public static final native void SetObjectFieldValue(Object object, String field, int value);

    public static final native String GetEnvironmentVariable(String name);

    public static final native String strftime(String format);

    public static final native String GetShellFolder(int id);
    
    public static final native String GetShortPathName(String longPathName);

    public static final native String[] RegGetSubKeys(int hRootKey, String pszSubKey);

    public static final native boolean RegKeyExists(int hRootKey, String pszSubKey);
    
    public static final native Authenticator getDefaultAuthenticator();
    
    private WinAPI()
    {
    }
}
