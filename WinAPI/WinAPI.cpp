/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
#include <windows.h>
#include <windowsx.h>
#include <tchar.h>
#include "htmlhelp.h"
#include "net_sf_eclipsensis_util_WinAPI.h"

typedef BOOL (_stdcall *_tSetLayeredWindowAttributesProc)(HWND hwnd, // handle to the layered window
  COLORREF crKey,      // specifies the color key
  BYTE bAlpha,         // value for the blend function
  DWORD dwFlags        // action
);
_tSetLayeredWindowAttributesProc SetLayeredWindowAttributesProc;

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_init(JNIEnv *pEnv, jclass jClass)
{
	if(LOBYTE(LOWORD(GetVersion())) >= 5) {
		HANDLE user32 = GetModuleHandle("user32");
		SetLayeredWindowAttributesProc = (_tSetLayeredWindowAttributesProc) GetProcAddress((HINSTANCE)user32, "SetLayeredWindowAttributes");
	}
	else {
		SetLayeredWindowAttributesProc = NULL;
	}
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetWindowLong(JNIEnv *pEnv, jclass jClass, jlong hWnd, jint nIndex, jlong dwNewLong)
{
	return SetWindowLong((HWND)hWnd, nIndex, (LONG)dwNewLong);
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetWindowLong(JNIEnv *pEnv, jclass jClass, jlong hWnd, jint nIndex)
{
	return GetWindowLong((HWND)hWnd, nIndex);
}

JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetLayeredWindowAttributes(JNIEnv *pEnv, jclass jClass, jlong hWnd, jint crRed, jint crGreen, jint crBlue, jint bAlpha, jlong dwFlags)
{
	if(SetLayeredWindowAttributesProc) {
		if ( dwFlags == net_sf_eclipsensis_util_WinAPI_LWA_COLORKEY ) {
			return( SetLayeredWindowAttributesProc((HWND)hWnd, (COLORREF)RGB(crRed,crGreen,crBlue), 
											   (BYTE)bAlpha, (DWORD)dwFlags ));
		} 
		else {
			return( SetLayeredWindowAttributesProc((HWND)hWnd, NULL, (BYTE)bAlpha, (DWORD)dwFlags ));
		}
	}
	else {
		return TRUE;
	}
}

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_RegQueryStrValue(JNIEnv *pEnv, jclass jClass, jlong hRootKey, jstring sSubKey, jstring sValue)
{
    jstring result = NULL;
    TCHAR *value = NULL;
    HKEY hKey;
    DWORD type;
    DWORD cbData;
    
    if(ERROR_SUCCESS == RegOpenKeyEx((HKEY)hRootKey, 
                                     _T((char *)pEnv->GetStringUTFChars(sSubKey, 0)),0, KEY_QUERY_VALUE, &hKey)) {
        if(ERROR_SUCCESS == RegQueryValueEx(hKey, _T(""), 0, &type, NULL, &cbData)) {
            value = (TCHAR *)GlobalAlloc(GPTR, cbData*sizeof(TCHAR));
            if(ERROR_SUCCESS == RegQueryValueEx(hKey, _T(""), 0, &type, (LPBYTE)value, &cbData)) {
                result = pEnv->NewStringUTF(value);
            }
    		GlobalFree(value);
        }
        RegCloseKey(hKey);
	}
    
    return result;    
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetDesktopWindow(JNIEnv *pEnv, jclass jClass)
{
	return (jlong)GetDesktopWindow();
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_util_WinAPI_HtmlHelp(JNIEnv *pEnv, jclass jClass, jlong hwndCaller, jstring pszFile, jint uCommand, jlong dwData)
{
	if(pszFile) {
		TCHAR *file = _T((char *)pEnv->GetStringUTFChars(pszFile, 0));
		return (jlong)HtmlHelp((HWND)hwndCaller, file, (UINT)uCommand, (DWORD)dwData) ;
	}
	else {
	    return 0;
	}
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetUserDefaultLangID(JNIEnv *pEnv, jclass jClass)
{
    return GetUserDefaultLangID();
}