/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.net.Authenticator;

import net.sf.eclipsensis.dialogs.RegistryValueSelectionDialog.RegistryValue;

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

    public static final int REG_SZ = 1;
    public static final int REG_EXPAND_SZ = 2;
    public static final int REG_BINARY = 3;
    public static final int REG_DWORD = 4;
    public static final int REG_MULTI_SZ = 7;

    public static final int KEY_QUERY_VALUE = 0x0001;
    public static final int KEY_ENUMERATE_SUB_KEYS = 0x0008;

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

    public static final int BP_RADIOBUTTON = 2;
    public static final int RBS_UNCHECKEDNORMAL = 1;
    public static final int RBS_UNCHECKEDHOT = 2;
    public static final int RBS_CHECKEDNORMAL = 5;
    public static final int RBS_CHECKEDHOT = 6;

    public static final int TVS_HASBUTTONS = 0x1;
    public static final int TVS_HASLINES = 0x2;

    public static final int COLOR_GRAYTEXT = 0x11;
    public static final int COLOR_3DHILIGHT = 0x14;

    public static final int WS_HSCROLL = 0x100000;
    public static final int WS_VSCROLL = 0x200000;

    public static final int LB_SETHORIZONTALEXTENT = 0x194;

    public static final int SM_CXVSCROLL = 0x2;
    public static final int SM_CYVSCROLL = 0x14;
    public static final int SM_CYHSCROLL = 0x3;

    public static final int SND_SYNC = 0x0;
    public static final int SND_ASYNC = 0x1;
    public static final int SND_NODEFAULT = 0x2;
    public static final int SND_LOOP = 0x8;
    public static final int SND_PURGE = 0x40;
    public static final int SND_FILENAME = 0x20000;

    public static final int FILE_ATTRIBUTE_ARCHIVE = 0x20;
    public static final int FILE_ATTRIBUTE_DIRECTORY = 0x10;
    public static final int FILE_ATTRIBUTE_HIDDEN = 0x2;
    public static final int FILE_ATTRIBUTE_NORMAL = 0x80;
    public static final int FILE_ATTRIBUTE_READONLY = 0x1;
    public static final int FILE_ATTRIBUTE_SYSTEM = 0x4;

    public static final int VK_SHIFT = 0x10;
    public static final int VK_CTRL = 0x11;
    public static final int VK_ALT = 0x12;

    private static native void init();
    public static native int SetWindowLong(int hWnd, int nIndex, int dwNewLong);
    public static native int GetWindowLong(int hWnd, int nIndex);
    public static native boolean SetLayeredWindowAttributes(int hWnd, int red, int green, int blue,
                                                            int bAlpha, int dwFlags);

    public static native String RegQueryStrValue(int hRootKey, String pszSubKey, String pszValue);

    public static native int GetDesktopWindow();

    public static native int HtmlHelp(int hwndCaller, String  pszFile, int uCommand, int dwData);

    public static native int GetUserDefaultLangID();

    public static native int GetUserDefaultUILanguage();

    public static native void ExtractHtmlHelp(String pszFile, String pszFolder, String[] tocAndIndex);

    public static native String[] GetPluginExports(String pszPluginFile);

    public static final native int SendMessage(int hWnd, int msg, int wParam, int lParam);

    public static final native int CallWindowProc(int lpWndProc, int hWnd, int Msg, int wParam, int lParam);

    public static final native boolean AreVisualStylesEnabled();

    public static final native void DrawWidgetThemeBackGround(int hWnd, int hDC, String theme, int partId, int stateId);

    public static final native void DrawWidgetThemeBorder(int hWnd, int hDC, String theme, int partId, int stateId);

    public static final native int GetSysColor(int index);

    public static final native int GetSystemMetrics (int nIndex);

    public static final native Object GetObjectFieldValue(Object object, String field, String signature);

    public static final native void SetIntFieldValue(Object object, String field, int value);

    public static final native String GetEnvironmentVariable(String name);

    public static final native String strftime(String format);

    public static final native String GetShellFolder(int id);

    public static final native String GetShortPathName(String longPathName);

    public static final native String[] RegGetSubKeys(int hRootKey, String pszSubKey);

    public static final native boolean RegKeyExists(int hRootKey, String pszSubKey);

    public static final native Authenticator getDefaultAuthenticator();

    public static final native boolean PlaySound(String pszFilename, int hModule, int dwFlags);

    public static final native int GetFileAttributes(String pszFilename);

    public static final native boolean SetFileAttributes(String pszFilename, int dwAttributes);

    public static final native short GetKeyState(int nVirtKey);

    public static final native boolean ValidateWildcard(String wildcard);

    public static final native int RegOpenKeyEx(int hKey, String lpSubKey, int ulOptions, int regSam);

    public static final native void RegCloseKey(int hKey);

    public static final native void RegQueryInfoKey(int hKey, int[] sizes);

    public static final native String RegEnumKeyEx(int hKey, int index, int subKeySize);

    public static final native String LoadResourceString(String pszFilename, int id, int lcid);

    public static final native int GetRegValuesCount(int hKey);

    public static final native boolean RegEnumValue(int hKey, int index, RegistryValue objRegValue);

    private WinAPI()
    {
    }
}
