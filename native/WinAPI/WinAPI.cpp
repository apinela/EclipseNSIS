/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
#include <windows.h>
#include <windowsx.h>
#include <tchar.h>
#include <time.h>
#include <shellapi.h>
#include <shlobj.h>
#include <objidl.h>
#include <mmsystem.h>
#include "htmlhelp.h"
#include "ITStorage.h"
#include "VisualStylesXP.h"
#include "WinAPI.h"

#define PACKVERSION(major,minor) MAKELONG(minor,major)
#define MAX_KEY_LENGTH 255

#ifndef DLLVERSIONINFO
typedef struct _DllVersionInfo
{
    DWORD cbSize;
    DWORD dwMajorVersion;
    DWORD dwMinorVersion;
    DWORD dwBuildNumber;
    DWORD dwPlatformID;
}DLLVERSIONINFO;

#endif

#ifndef DLLGETVERSIONPROC
typedef int (FAR WINAPI *DLLGETVERSIONPROC) (DLLVERSIONINFO *);
#endif


typedef BOOL (_stdcall *_tSetLayeredWindowAttributesProc)(HWND hwnd, // handle to the layered window
    COLORREF crKey,      // specifies the color key
    BYTE bAlpha,         // value for the blend function
    DWORD dwFlags        // action
);
_tSetLayeredWindowAttributesProc SetLayeredWindowAttributesProc;
BOOL isUnicode = FALSE;
BOOL isXP = FALSE;
BOOL isNT = FALSE;
BOOL is2K = FALSE;
BOOL isME = FALSE;
BOOL is9x = FALSE;
BOOL isCommCtrl6 = FALSE;
REGSAM RegView = 0;

DWORD GetDllVersion(LPCTSTR lpszDllName)
{
    HINSTANCE hinstDll;
    DWORD dwVersion = 0;

    /* For security purposes, LoadLibrary should be provided with a
       fully-qualified path to the DLL. The lpszDllName variable should be
       tested to ensure that it is a fully qualified path before it is used. */
    hinstDll = LoadLibrary(lpszDllName);

    if(hinstDll)
    {
        DLLGETVERSIONPROC pDllGetVersion;
        pDllGetVersion = (DLLGETVERSIONPROC)GetProcAddress(hinstDll,
                          "DllGetVersion");

        /* Because some DLLs might not implement this function, you
        must test for it explicitly. Depending on the particular
        DLL, the lack of a DllGetVersion function can be a useful
        indicator of the version. */

        if(pDllGetVersion)
        {
            DLLVERSIONINFO dvi;
            HRESULT hr;

            ZeroMemory(&dvi, sizeof(dvi));
            dvi.cbSize = sizeof(dvi);

            hr = (*pDllGetVersion)(&dvi);

            if(SUCCEEDED(hr))
            {
               dwVersion = PACKVERSION(dvi.dwMajorVersion, dvi.dwMinorVersion);
            }
        }

        FreeLibrary(hinstDll);
    }
    return dwVersion;
}

JNIEXPORT void JNICALL WinAPI_init(JNIEnv *pEnv, jclass jClass)
{
    OSVERSIONINFO osvi;
    osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    GetVersionEx (&osvi);

	isUnicode = (osvi.dwPlatformId == VER_PLATFORM_WIN32_NT);
    if(osvi.dwMajorVersion >= 5) {
        if((osvi.dwMajorVersion > 5) || (osvi.dwMajorVersion == 5 && osvi.dwMinorVersion >= 1)) {
            isXP = TRUE;
        }
        else {
            is2K = TRUE;
        }
        HANDLE user32 = GetModuleHandle(_T("user32"));
        SetLayeredWindowAttributesProc = (_tSetLayeredWindowAttributesProc) GetProcAddress((HINSTANCE)user32, "SetLayeredWindowAttributes");
    }
    else if(osvi.dwMajorVersion == 4) {
        if(osvi.dwMinorVersion == 0 && isUnicode) {
            isNT = TRUE;
        }
        else if(osvi.dwMinorVersion == 90) {
            isME = TRUE;
        }
        else {
            is9x = TRUE;
        }
        SetLayeredWindowAttributesProc = NULL;
    }
    isCommCtrl6 = (GetDllVersion(_T("comctl32.dll")) >= PACKVERSION(6,0));
}

JNIEXPORT jint JNICALL WinAPI_SetWindowLong(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint nIndex, jint dwNewLong)
{
    return (jint)SetWindowLong((HWND)hWnd, nIndex, (LONG)dwNewLong);
}

JNIEXPORT jint JNICALL WinAPI_GetWindowLong(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint nIndex)
{
    return (jint)GetWindowLong((HWND)hWnd, nIndex);
}

JNIEXPORT jlongptr JNICALL WinAPI_SetWindowLongPtr(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint nIndex, jlongptr dwNewLong)
{
    return (jlongptr)SetWindowLongPtr((HWND)hWnd, nIndex, (LONG_PTR)dwNewLong);
}

JNIEXPORT jlongptr JNICALL WinAPI_GetWindowLongPtr(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint nIndex)
{
    return (jlongptr)GetWindowLongPtr((HWND)hWnd, nIndex);
}

JNIEXPORT jboolean JNICALL WinAPI_SetLayeredWindowAttributes(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint crRed, jint crGreen, jint crBlue, jint bAlpha, jint dwFlags)
{
    if(SetLayeredWindowAttributesProc) {
        if ( dwFlags == net_sf_eclipsensis_util_winapi_WinAPI_LWA_COLORKEY ) {
            return (jboolean)( SetLayeredWindowAttributesProc((HWND)hWnd, (COLORREF)RGB(crRed,crGreen,crBlue),
                                               (BYTE)bAlpha, (DWORD)dwFlags ));
        }
        else {
            return (jboolean)( SetLayeredWindowAttributesProc((HWND)hWnd, NULL, (BYTE)bAlpha, (DWORD)dwFlags ));
        }
    }
    else {
        return JNI_TRUE;
    }
}

JNIEXPORT jint JNICALL WinAPI_GetRegView(JNIEnv *pEnv, jobject jObject)
{
	return (jint)RegView;
}

JNIEXPORT void JNICALL WinAPI_SetRegView(JNIEnv *pEnv, jobject jObject, jint regView)
{
	switch(regView)
	{
	case KEY_WOW64_32KEY:
		RegView = (REGSAM)regView;
		break;
	default:
		RegView = KEY_WOW64_64KEY;
	}
}

TCHAR* RegQueryStrValue(HKEY hRootKey, LPCSTR subKey, LPCSTR valueName)
{
    HKEY hKey;
    DWORD type;
    DWORD cbData;
	LONG rv;
	TCHAR *result = NULL;

    if(ERROR_SUCCESS == (rv = RegOpenKeyEx((HKEY)hRootKey,
                                      subKey,0, KEY_QUERY_VALUE|RegView, &hKey))) {
        rv = RegQueryValueEx(hKey, valueName, 0, &type, NULL, &cbData);
        if(ERROR_SUCCESS == rv && (type == REG_SZ || type == REG_EXPAND_SZ)) {
            TCHAR *value = (TCHAR *)GlobalAlloc(GPTR, cbData*sizeof(TCHAR));
            if(ERROR_SUCCESS == (rv = RegQueryValueEx(hKey, valueName, 0, &type, (LPBYTE)value, &cbData))) {
            	if(type == REG_EXPAND_SZ) {
            		cbData = ExpandEnvironmentStrings(value, NULL, 0);
            		if(cbData > 0) {
            			TCHAR *value2 = (TCHAR *)GlobalAlloc(GPTR, cbData*sizeof(TCHAR));
            			ExpandEnvironmentStrings(value, (LPTSTR)value2, cbData);
            			GlobalFree(value);
            			value = value2;
            		}
            	}
                result = value;
            }
            else {
                GlobalFree(value);
            }
        }
        rv = RegCloseKey(hKey);
    }

    return result;
}

JNIEXPORT jstring JNICALL WinAPI_RegQueryStrValue(JNIEnv *pEnv, jobject jObject, jhandle hRootKey, jstring sSubKey, jstring sValue)
{
    jstring result = NULL;

    LPCSTR str1 = (LPCSTR)pEnv->GetStringUTFChars(sSubKey, 0);
    LPCSTR str2 = (LPCSTR)pEnv->GetStringUTFChars(sValue, 0);
    TCHAR *value = RegQueryStrValue((HKEY)hRootKey, str1, str2);
    if(value) {
        result = pEnv->NewStringUTF(value);
        GlobalFree(value);
    }
    pEnv->ReleaseStringUTFChars(sSubKey, str1);
    pEnv->ReleaseStringUTFChars(sValue, str2);

    return result;
}

JNIEXPORT jhandle JNICALL WinAPI_GetDesktopWindow(JNIEnv *pEnv, jobject jObject)
{
    return (jhandle)GetDesktopWindow();
}

JNIEXPORT jhandle JNICALL WinAPI_HtmlHelp(JNIEnv *pEnv, jobject jObject, jhandle hwndCaller, jstring pszFile, jint uCommand, jint dwData)
{
    jhandle result = 0;
    if(pszFile) {
        LPCSTR file = (LPCSTR)pEnv->GetStringUTFChars(pszFile, 0);
        result = (jhandle)HtmlHelp((HWND)hwndCaller, file, (UINT)uCommand, (DWORD)dwData);
        pEnv->ReleaseStringUTFChars(pszFile, file);
    }
    return result;
}

JNIEXPORT jint JNICALL WinAPI_GetUserDefaultLangID(JNIEnv *pEnv, jobject jObject)
{
    return (jint)GetUserDefaultLangID();
}

LANGID GetRegistryLangId(HKEY rootKey, LPCSTR subKey, LPCSTR valueName)
{
    LANGID langId = NULL;
    TCHAR *value = RegQueryStrValue(rootKey, subKey, valueName);
    if(value) {
        DWORD dwLangID;
        int nFields = _stscanf_s( value, _T("%x"), &dwLangID );
        if( nFields == 1 ) {
            langId = LANGID( dwLangID );
        }

        GlobalFree(value);
    }
    return langId;
}

JNIEXPORT jint JNICALL WinAPI_GetUserDefaultUILanguage(JNIEnv *pEnv, jobject jObject)
{
    LANGID (WINAPI *GUDUIL)();
    static const TCHAR* guduil = _T("GetUserDefaultUILanguage");
    static const TCHAR* dll = _T("KERNEL32.dll");

    HMODULE hModule = GetModuleHandle(dll);
    if (!hModule) {
        hModule = LoadLibrary(dll);
    }
    if (!hModule) {
        GUDUIL = NULL;
    }
    else {
        GUDUIL = (LANGID (WINAPI *)())GetProcAddress(hModule, guduil);
    }

    if (GUDUIL)
    {
        // Windows ME/2000+
        return (jint)GUDUIL();
    }
    else
    {
        LANGID langId = NULL;
        if(is9x) {
            // Windows 9x
            static const TCHAR* reg9xLocaleKey = _T("Control Panel\\Desktop\\ResourceLocale");
            langId = GetRegistryLangId(HKEY_CURRENT_USER, (LPCSTR)reg9xLocaleKey, NULL);
        }

        if (!langId) {
            // Windows NT
            // This key exists on 9x as well, so it's only read if ResourceLocale wasn't found
            static const TCHAR* regNtLocaleKey = _T(".DEFAULT\\Control Panel\\International");
            static const TCHAR* regNtLocaleVal = _T("Locale");
            langId = GetRegistryLangId(HKEY_USERS, (LPCSTR)regNtLocaleKey, (LPCSTR)regNtLocaleVal);
        }
        return (jint)langId;
    }
}

JNIEXPORT void JNICALL WinAPI_ExtractHtmlHelp(JNIEnv *pEnv, jobject jObject, jstring pszFile, jstring pszFolder, jobjectArray tocAndIndex)
{
    HRESULT hr = CoInitialize(NULL);

    if(hr == S_OK || hr == S_FALSE) {
        TCHAR *tocFile = NULL;
        TCHAR *indexFile = NULL;
        tocFile = (TCHAR *)GlobalAlloc(GPTR, (MAX_PATH+1)*sizeof(TCHAR));
        _tcscpy_s(tocFile,(MAX_PATH+1),_T(""));
        indexFile = (TCHAR *)GlobalAlloc(GPTR, (MAX_PATH+1)*sizeof(TCHAR));
        _tcscpy_s(indexFile,(MAX_PATH+1),_T(""));

        LPCWSTR str1 = (LPCWSTR)pEnv->GetStringChars(pszFile, 0);
        LPCSTR str2 = (LPCSTR)pEnv->GetStringUTFChars(pszFolder, 0);
        if(ExtractHtmlHelp(str1, str2, tocFile, indexFile) == S_OK) {
            pEnv->SetObjectArrayElement(tocAndIndex, 0, pEnv->NewStringUTF(tocFile));
            pEnv->SetObjectArrayElement(tocAndIndex, 1, pEnv->NewStringUTF(indexFile));
        }
        else {
            pEnv->SetObjectArrayElement(tocAndIndex, 0, NULL);
            pEnv->SetObjectArrayElement(tocAndIndex, 1, NULL);
        }

        GlobalFree(tocFile);
        GlobalFree(indexFile);
        pEnv->ReleaseStringChars(pszFile, (const jchar *)str1);
        pEnv->ReleaseStringUTFChars(pszFolder, (const char *)str2);

        if(hr == S_OK) {
            CoUninitialize();
        }
    }
}

#ifdef WIN64
BOOL GetExportDirInfo32(PIMAGE_NT_HEADERS32 NTHeaders, LPDWORD pExportDirVA, LPDWORD pExportDirSize)
{
    if (NTHeaders->OptionalHeader.NumberOfRvaAndSizes > IMAGE_DIRECTORY_ENTRY_EXPORT) {

        *pExportDirVA = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].VirtualAddress;
        *pExportDirSize = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].Size;

		return TRUE;
	}
	return FALSE;
}
#endif

BOOL GetExportDirInfo(PIMAGE_NT_HEADERS NTHeaders, LPDWORD pExportDirVA, LPDWORD pExportDirSize)
{
#ifdef WIN64
	if (NTHeaders->FileHeader.Characteristics & IMAGE_FILE_MACHINE_I386) {
		// 32-bit DLL
		return GetExportDirInfo32((PIMAGE_NT_HEADERS32)NTHeaders, pExportDirVA, pExportDirSize);
	}
#endif

    if (NTHeaders->OptionalHeader.NumberOfRvaAndSizes > IMAGE_DIRECTORY_ENTRY_EXPORT) {

        *pExportDirVA = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].VirtualAddress;
        *pExportDirSize = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].Size;

		return TRUE;
	}
	return FALSE;
}

JNIEXPORT jobjectArray JNICALL WinAPI_GetPluginExports(JNIEnv *pEnv, jobject jObject, jstring pszPluginFile)
{
    jobjectArray result = NULL;

    if(pszPluginFile) {
        LPCSTR pluginFile =  (LPCSTR)pEnv->GetStringUTFChars(pszPluginFile, 0);
        unsigned char* dlldata    = 0;
        long dlldatalen = 0;
        bool loaded = false;

        FILE* dll;
		fopen_s(&dll, pluginFile,_T("rb"));
        if (dll) {
            fseek(dll,0,SEEK_END);
            dlldatalen = ftell(dll);
            fseek(dll,0,SEEK_SET);
            if (dlldatalen > 0) {
                dlldata = new unsigned char[dlldatalen];
                if (dlldata)
                {
                    size_t bytesread = fread((void*)dlldata,1,dlldatalen,dll);
                    if ((long)bytesread == dlldatalen) {
                        loaded = true;
                    }
                }
            }
            fclose(dll);
        }

        if (loaded) {
            PIMAGE_NT_HEADERS NTHeaders = PIMAGE_NT_HEADERS(dlldata + PIMAGE_DOS_HEADER(dlldata)->e_lfanew);
            if (NTHeaders->Signature == IMAGE_NT_SIGNATURE) {
                if (NTHeaders->FileHeader.Characteristics & IMAGE_FILE_DLL) {
					DWORD ExportDirVA;
                    DWORD ExportDirSize;
                    if (GetExportDirInfo(NTHeaders, &ExportDirVA, &ExportDirSize)) {
                        PIMAGE_SECTION_HEADER sections = IMAGE_FIRST_SECTION(NTHeaders);

                        for (int i = 0; i < NTHeaders->FileHeader.NumberOfSections; i++) {
                            if (sections[i].VirtualAddress <= ExportDirVA
                                && sections[i].VirtualAddress+sections[i].Misc.VirtualSize >= ExportDirVA+ExportDirSize) {
                                PIMAGE_EXPORT_DIRECTORY exports = PIMAGE_EXPORT_DIRECTORY(dlldata + sections[i].PointerToRawData + ExportDirVA - sections[i].VirtualAddress);
                                unsigned long *names = (unsigned long*)((unsigned long)exports + (char *)exports->AddressOfNames - ExportDirVA);

                                jclass stringClass = pEnv->FindClass("java/lang/String");
                                result = pEnv->NewObjectArray(exports->NumberOfNames, stringClass, NULL);

                                for (unsigned long j = 0; j < exports->NumberOfNames; j++) {
                                    char *name = (char *)exports + names[j] - ExportDirVA;
                                    pEnv->SetObjectArrayElement(result, j, pEnv->NewStringUTF((const char*)name));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (dlldata) {
            delete[] dlldata;
        }

        pEnv->ReleaseStringUTFChars(pszPluginFile, pluginFile);
    }
    return result;
}

JNIEXPORT jlongptr JNICALL WinAPI_SendMessage(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jint msg, jlongptr wParam, jlongptr lParam)
{
    return (jlongptr)SendMessage((HWND)hWnd, (UINT)msg, (WPARAM)wParam, (LPARAM)lParam);
}

JNIEXPORT jlongptr JNICALL WinAPI_CallWindowProc(JNIEnv *pEnv, jobject jObject, jlongptr lpWndProc, jhandle hWnd, jint Msg, jlongptr wParam, jlongptr lParam)
{
    return (jlongptr)CallWindowProcA((WNDPROC)lpWndProc, (HWND)hWnd, (UINT)Msg, (WPARAM)wParam, (LPARAM)lParam);
}

JNIEXPORT jboolean JNICALL WinAPI_AreVisualStylesEnabled (JNIEnv *pEnv, jobject jObject)
{
    if(isXP && isCommCtrl6) {
        if(g_xpStyle.IsAppThemed() && g_xpStyle.IsThemeActive()) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

JNIEXPORT void JNICALL WinAPI_DrawWidgetThemeBackGround(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jhandle hDC,
                                                                         jstring theme, jint partId, jint stateId)
{
    if(WinAPI_AreVisualStylesEnabled(pEnv,jObject)) {
        LPCWSTR pszTheme =  (LPCWSTR)pEnv->GetStringChars(theme, 0);
        HTHEME hTheme = g_xpStyle.OpenThemeData((HWND)hWnd, pszTheme);

		RECT rect;

        GetWindowRect((HWND)hWnd, &rect);
        OffsetRect(&rect, -rect.left, -rect.top);

		g_xpStyle.DrawThemeParentBackground((HWND)hWnd, (HDC)hDC, &rect);
		g_xpStyle.DrawThemeBackground(hTheme, (HDC)hDC, partId, stateId, &rect, NULL);

        g_xpStyle.CloseThemeData(hTheme);
        pEnv->ReleaseStringChars(theme, (const jchar *)pszTheme);
    }
}

JNIEXPORT void JNICALL WinAPI_DrawWidgetThemeBorder(JNIEnv *pEnv, jobject jObject, jhandle hWnd, jhandle hDC,
                                                                     jstring theme, jint partId, jint stateId)
{
    if(WinAPI_AreVisualStylesEnabled(pEnv,jObject)) {
        LPCWSTR pszTheme =  (LPCWSTR)pEnv->GetStringChars(theme, 0);
        HTHEME hTheme = g_xpStyle.OpenThemeData((HWND)hWnd, pszTheme);

		RECT rect, clipRect;

        GetWindowRect((HWND)hWnd, &rect);
        rect.right -= rect.left;
        rect.left = 0;
        rect.bottom -= rect.top;
        rect.top = 0;

		// draw the left border
		clipRect.left = rect.left;
		clipRect.top = rect.top;
		clipRect.right = rect.left + 2;
		clipRect.bottom = rect.bottom;
		g_xpStyle.DrawThemeBackground(hTheme, (HDC)hDC, partId, stateId, &rect, &clipRect);

		// draw the top border
		clipRect.left = rect.left;
		clipRect.top = rect.top;
		clipRect.right = rect.right;
		clipRect.bottom = rect.top + 2;
		g_xpStyle.DrawThemeBackground(hTheme, (HDC)hDC, partId, stateId, &rect, &clipRect);

		// draw the right border
		clipRect.left = rect.right - 2;
		clipRect.top = rect.top;
		clipRect.right = rect.right;
		clipRect.bottom = rect.bottom;
		g_xpStyle.DrawThemeBackground(hTheme, (HDC)hDC, partId, stateId, &rect, &clipRect);

		// draw the bottom border
		clipRect.left = rect.left;
		clipRect.top = rect.bottom - 2;
		clipRect.right = rect.right;
		clipRect.bottom = rect.bottom;
		g_xpStyle.DrawThemeBackground(hTheme, (HDC)hDC, partId, stateId, &rect, &clipRect);

        g_xpStyle.CloseThemeData(hTheme);
        pEnv->ReleaseStringChars(theme, (const jchar *)pszTheme);
    }
}

JNIEXPORT jint JNICALL WinAPI_GetSysColor(JNIEnv *pEnv, jobject jObject, jint index)
{
    return GetSysColor(index);
}

JNIEXPORT jint JNICALL WinAPI_GetSystemMetrics(JNIEnv *pEnv, jobject jObject, jint index)
{
    return GetSystemMetrics(index);
}

JNIEXPORT void JNICALL WinAPI_SetIntFieldValue(JNIEnv *pEnv, jobject jObject, jobject object, jstring field, jint value)
{
    jclass clasz = pEnv->GetObjectClass(object);
    LPCSTR fieldName = (LPCSTR)pEnv->GetStringUTFChars(field, 0);
    jfieldID fieldId = pEnv->GetFieldID(clasz,fieldName,_T("I"));
    pEnv->ReleaseStringUTFChars(field, fieldName);
    pEnv->SetIntField(object, fieldId, value);
}

JNIEXPORT jobject JNICALL WinAPI_GetObjectFieldValue(JNIEnv *pEnv, jobject jObject, jobject object, jstring field, jstring signature)
{
    jclass clasz = pEnv->GetObjectClass(object);
    LPCSTR fieldName = (LPCSTR)pEnv->GetStringUTFChars(field, 0);
    LPCSTR sign = (LPCSTR)pEnv->GetStringUTFChars(signature, 0);
    jfieldID fieldId = pEnv->GetFieldID(clasz,fieldName,sign);
    pEnv->ReleaseStringUTFChars(field, fieldName);
    pEnv->ReleaseStringUTFChars(signature, sign);
    return pEnv->GetObjectField(object, fieldId);
}

JNIEXPORT jstring JNICALL WinAPI_GetEnvironmentVariable(JNIEnv *pEnv, jobject jObject, jstring name)
{
    jstring result = NULL;
    TCHAR *value = NULL;
	LONG rv;

    LPCSTR lpName = (LPCSTR)pEnv->GetStringUTFChars(name, 0);
    rv = GetEnvironmentVariable(lpName, NULL, 0);
    if(rv) {
        value = (TCHAR *)GlobalAlloc(GPTR, rv*sizeof(TCHAR));
        if(value) {
            rv = GetEnvironmentVariable(lpName, (LPTSTR)value, rv);
            if(rv) {
                result = pEnv->NewStringUTF(value);
            }
            GlobalFree(value);
        }
    }
    pEnv->ReleaseStringUTFChars(name, lpName);

    return result;
}

JNIEXPORT jstring JNICALL WinAPI_strftime(JNIEnv *pEnv, jobject jObject, jstring format)
{
    TCHAR *szFormat = (TCHAR *)pEnv->GetStringUTFChars(format, 0);
    char datebuf[256];

    time_t rawtime;
    time(&rawtime);

    datebuf[0]=0;
	struct tm localtime;
	localtime_s(&localtime, &rawtime);
    size_t s=strftime(datebuf,sizeof(datebuf),szFormat,&localtime);

    if (s < 0) {
        datebuf[0]=0;
    }
    else {
        datebuf[max(s,sizeof(datebuf)-1)]=0;
    }
    pEnv->ReleaseStringUTFChars(format, szFormat);

    return pEnv->NewStringUTF(datebuf);
}

JNIEXPORT jstring JNICALL WinAPI_GetShellFolder(JNIEnv *pEnv, jobject jObject, jint id)
{
    LPITEMIDLIST idl;
    TCHAR buf[2*MAX_PATH+2*+sizeof(TCHAR)];
    if (!SHGetSpecialFolderLocation(NULL, id, &idl))
    {
        BOOL res = SHGetPathFromIDList(idl, buf);
        IMalloc *m;
        SHGetMalloc(&m);
        if (m)
        {
            m->Free(idl);
            m->Release();
        }
        if (res)
        {
            return pEnv->NewStringUTF(buf);
        }
    }

    return NULL;
}

JNIEXPORT jstring JNICALL WinAPI_GetShortPathName(JNIEnv *pEnv, jobject jObject, jstring longPathName)
{
    LPCTSTR lpName = (LPCTSTR)pEnv->GetStringUTFChars(longPathName, 0);
    TCHAR buf[2*MAX_PATH+2];
    DWORD rv = GetShortPathName(lpName,buf,sizeof(buf));
    pEnv->ReleaseStringUTFChars(longPathName, lpName);
    if(rv) {
        return pEnv->NewStringUTF(buf);
    }

    return NULL;
}

JNIEXPORT jobjectArray JNICALL WinAPI_RegGetSubKeys(JNIEnv *pEnv, jobject jObject, jhandle hRootKey, jstring sSubKey)
{
    HKEY hKey;
    TCHAR achKey[MAX_KEY_LENGTH+1];   // buffer for subkey name
    DWORD cSubKeys=0;               // number of subkeys
    DWORD cbMaxSubKey;              // longest subkey size
    DWORD cbName;                   // size of name string
    DWORD i;
    BOOL openedKey = FALSE;
    jobjectArray result = NULL;

    if(sSubKey == NULL) {
        hKey = (HKEY)hRootKey;
    }
    else {
        LPCSTR str1 = (LPCSTR)pEnv->GetStringUTFChars(sSubKey, 0);
        DWORD rv = RegOpenKeyEx((HKEY)hRootKey, str1,0, KEY_READ|RegView, &hKey);
        pEnv->ReleaseStringUTFChars(sSubKey,str1);
        if(ERROR_SUCCESS != rv) {
            return NULL;
        }
        openedKey = TRUE;
    }

    if(ERROR_SUCCESS == RegQueryInfoKey(
        hKey,                    // key handle
        NULL, NULL, NULL,
        &cSubKeys,               // number of subkeys
        &cbMaxSubKey,            // longest subkey size
        NULL, NULL, NULL, NULL, NULL, NULL)) {

        jclass stringClass = pEnv->FindClass("java/lang/String");
        result = pEnv->NewObjectArray(cSubKeys, stringClass, NULL);

        // Enumerate the subkeys, until RegEnumKeyEx fails.
        if(cSubKeys) {
            for(i=0; i<cSubKeys; i++) {
                cbName = MAX_KEY_LENGTH;
                ZeroMemory(achKey,sizeof(achKey));
                if(ERROR_SUCCESS == RegEnumKeyEx(hKey, i,
                   achKey, &cbName, NULL, NULL, NULL, NULL)) {
                    pEnv->SetObjectArrayElement(result, i, pEnv->NewStringUTF(achKey));
                }
            }
        }
    }

    if(openedKey) {
        RegCloseKey(hKey);
    }

    return result;
}

JNIEXPORT jboolean JNICALL WinAPI_RegKeyExists(JNIEnv *pEnv, jobject jObject, jhandle hRootKey, jstring sSubKey)
{
    HKEY hKey;
    DWORD rv;

    if(sSubKey == NULL) {
        rv = RegOpenKeyEx((HKEY)hRootKey, NULL,0, KEY_READ|RegView, &hKey);
    }
    else {
        LPCSTR str1 = (LPCSTR)pEnv->GetStringUTFChars(sSubKey, 0);
        rv = RegOpenKeyEx((HKEY)hRootKey, str1,0, KEY_READ|RegView, &hKey);
        pEnv->ReleaseStringUTFChars(sSubKey,str1);
    }
    if(ERROR_SUCCESS != rv) {
        return JNI_FALSE;
    }
    RegCloseKey(hKey);
    return JNI_TRUE;
}

JNIEXPORT jobject JNICALL WinAPI_getDefaultAuthenticator(JNIEnv *pEnv, jobject jObject)
{
    jobject result = NULL;

    jclass clasz = pEnv->FindClass("java/net/Authenticator");
    if(clasz != NULL) {
        jfieldID field = pEnv->GetStaticFieldID(clasz,"theAuthenticator","Ljava/net/Authenticator;");
        if(field != NULL) {
            result = pEnv->GetStaticObjectField(clasz, field);
        }
    }

    return result;
}

JNIEXPORT jboolean JNICALL WinAPI_PlaySound(JNIEnv *pEnv, jobject jObject, jstring pszFilename, jhandle hModule, jint dwFlags)
{
    if(pszFilename) {
        TCHAR filename[MAX_PATH+1];
        TCHAR* szFilename = (TCHAR*)pEnv->GetStringUTFChars(pszFilename, 0);
        _tcscpy_s(filename, MAX_PATH+1, szFilename);
        pEnv->ReleaseStringUTFChars(pszFilename,szFilename);
        return (jboolean)PlaySound(filename, (HINSTANCE)hModule, dwFlags);
    }
    else {
        return (jboolean)PlaySound(NULL, (HINSTANCE)hModule, dwFlags);
    }
}

JNIEXPORT jint JNICALL WinAPI_GetFileAttributes(JNIEnv *pEnv, jobject jObject, jstring pszFilename)
{
    DWORD result = 0;
    if(pszFilename) {
        LPCSTR filename = (LPCSTR)pEnv->GetStringUTFChars(pszFilename, 0);
        result = GetFileAttributes(filename);
        pEnv->ReleaseStringUTFChars(pszFilename,filename);
    }
    return result;
}

JNIEXPORT jboolean JNICALL WinAPI_SetFileAttributes(JNIEnv *pEnv, jobject jObject, jstring pszFilename, jint dwAttributes)
{
    jboolean result = JNI_FALSE;
    if(pszFilename) {
        LPCSTR filename = (LPCSTR)pEnv->GetStringUTFChars(pszFilename, 0);
        if(SetFileAttributes(filename, dwAttributes)) {
            result = JNI_TRUE;
        }
        pEnv->ReleaseStringUTFChars(pszFilename,filename);
    }
    return result;
}

JNIEXPORT jshort JNICALL WinAPI_GetKeyState(JNIEnv *pEnv, jobject jObject, jint nVirtKey)
{
    return GetKeyState(nVirtKey);
}

JNIEXPORT jboolean JNICALL WinAPI_ValidateWildcard(JNIEnv *pEnv, jobject jObject, jstring wildcard)
{
    jboolean result = JNI_FALSE;
    if(wildcard) {
        LPCSTR lpWildcard = (LPCSTR)pEnv->GetStringUTFChars(wildcard, 0);
        WIN32_FIND_DATA FindFileData;
        HANDLE hFind;

        hFind = FindFirstFile(lpWildcard, &FindFileData);
        if (hFind != INVALID_HANDLE_VALUE) {
            result = JNI_TRUE;
            FindClose(hFind);
        }
        pEnv->ReleaseStringUTFChars(wildcard,lpWildcard);
    }
    return result;
}

JNIEXPORT jhandle JNICALL WinAPI_RegOpenKeyEx(JNIEnv *pEnv, jobject jObject, jhandle hKey, jstring lpSubKey, jint ulOptions, jint regSam)
{
    HKEY hSubKey;

    LPCSTR subKey = (LPCSTR)pEnv->GetStringUTFChars(lpSubKey, 0);

    if(ERROR_SUCCESS != RegOpenKeyEx((HKEY)hKey,subKey,ulOptions,regSam|RegView,&hSubKey)) {
    	hSubKey = 0;
    }
    pEnv->ReleaseStringUTFChars(lpSubKey, subKey);
    return (jhandle)hSubKey;
}

JNIEXPORT void JNICALL WinAPI_RegCloseKey(JNIEnv *pEnv, jobject jObject, jhandle hKey)
{
	RegCloseKey((HKEY)hKey);
}

JNIEXPORT void JNICALL WinAPI_RegQueryInfoKey(JNIEnv *pEnv, jobject jObject, jhandle hKey, jintArray sizes)
{
	jint newSizes[] = {0, 0};

	if(ERROR_SUCCESS == RegQueryInfoKey((HKEY)hKey, NULL, NULL, NULL, (LPDWORD)&(newSizes[0]), (LPDWORD)&(newSizes[1]), NULL, NULL, NULL, NULL, NULL, NULL)) {
		pEnv->SetIntArrayRegion(sizes, 0, 2, newSizes);
	}
}

JNIEXPORT jstring JNICALL WinAPI_RegEnumKeyEx(JNIEnv *pEnv, jobject jObject, jhandle hKey, jint index, jint subKeySize)
{
	if(!isME && !is9x) {
		subKeySize++;
	}
	TCHAR *subKey = (TCHAR *)GlobalAlloc(GPTR, subKeySize*sizeof(TCHAR));

	jstring result = NULL;
	if(ERROR_SUCCESS == RegEnumKeyEx((HKEY)hKey, index, subKey, (LPDWORD)&subKeySize, NULL, NULL, NULL, NULL)) {
		result = pEnv->NewStringUTF(subKey);
	}
	GlobalFree(subKey);

	return result;
}

JNIEXPORT jstring JNICALL WinAPI_LoadResourceString(JNIEnv *pEnv, jobject jObject, jstring pszFilename, jint id, jint lcid)
{
	jstring result = NULL;
    LPCSTR filename = (LPCSTR)pEnv->GetStringUTFChars(pszFilename, 0);
    LCID locale = (LCID)lcid;
    LCID oldLocale = (LCID)0;

    if(locale) {
        oldLocale = GetThreadLocale();
        if(oldLocale != locale) {
            if(!SetThreadLocale(locale)) {
                locale = (LCID)0;
            }
        }
    }

	BOOL shouldFree = FALSE;
	HMODULE hModule = GetModuleHandle((LPCTSTR)filename);
	if(!hModule) {
		hModule = LoadLibraryEx(filename, NULL, LOAD_LIBRARY_AS_DATAFILE);
		shouldFree = TRUE;
	}
	if(hModule) {
		TCHAR* buf = (TCHAR *)GlobalAlloc(GPTR, 2000*sizeof(TCHAR));
		int n = LoadString(hModule, (UINT)id, buf, 2000);
		if(n) {
			result = pEnv->NewStringUTF(buf);
		}
		GlobalFree(buf);
		if(shouldFree) {
			FreeLibrary(hModule);
		}
	}

    pEnv->ReleaseStringUTFChars(pszFilename, filename);

    if(locale && oldLocale) {
        SetThreadLocale(oldLocale);
    }

    return result;
}

JNIEXPORT jint JNICALL WinAPI_GetRegValuesCount(JNIEnv *pEnv, jobject jObject, jhandle hKey)
{
	DWORD cValues;

	if(ERROR_SUCCESS == RegQueryInfoKey((HKEY)hKey, NULL, NULL, NULL, NULL, NULL, NULL, &cValues, NULL, NULL, NULL, NULL)) {
		return (jint)cValues;
	}
	return (jint)0;
}

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    RegEnumValue
 * Signature: (IILnet/sf/eclipsensis/dialogs/RegistryValueSelectionDialog$RegistryValue;)V
 */
JNIEXPORT jboolean JNICALL WinAPI_RegEnumValue(JNIEnv *pEnv, jobject jObject, jhandle hKey, jint index, jobject objRegValue)
{
	jclass clasz = pEnv->GetObjectClass(objRegValue);
	jmethodID setMethod = pEnv->GetMethodID(clasz,_T("set"),_T("(Ljava/lang/String;I[B)V"));
	if(setMethod != NULL) {
		LONG rv;
		TCHAR lpValueName[16384];
		DWORD cValueName = sizeof(lpValueName);
		DWORD type;
		LPBYTE  data = NULL;
		DWORD cbData = 0;
        rv = RegEnumValue((HKEY)hKey,(DWORD)index,(LPTSTR)lpValueName, &cValueName, NULL, &type, NULL, &cbData);
		if(rv == ERROR_SUCCESS) {
			jstring value = pEnv->NewStringUTF(lpValueName);
            jbyteArray bytes = NULL;
            if(cbData > 0) {
			    data = (LPBYTE)GlobalAlloc(GPTR, cbData*sizeof(BYTE));
                RegEnumValue((HKEY)hKey,(DWORD)index,(LPTSTR)lpValueName, &cValueName, NULL, &type, data, &cbData);
			    bytes = pEnv->NewByteArray(cbData);
			    pEnv->SetByteArrayRegion(bytes, 0, cbData, (jbyte *)data);
			    GlobalFree(data);
            }
			pEnv->CallVoidMethod(objRegValue, setMethod, value, (jint)type, bytes);
			return JNI_TRUE;
		}
	}

	return JNI_FALSE;
}
