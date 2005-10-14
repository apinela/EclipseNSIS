/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
#include "htmlhelp.h"
#include "ITStorage.h"
#include "VisualStylesXP.h"
#include "net_sf_eclipsensis_util_WinAPI.h"

#define PACKVERSION(major,minor) MAKELONG(minor,major)

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
BOOL isUnicode;
BOOL isXP;
BOOL isCommCtrl6;


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

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_init(JNIEnv *pEnv, jclass jClass)
{
    OSVERSIONINFO osvi;
    osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    GetVersionEx (&osvi);
	
	isUnicode = (osvi.dwPlatformId == VER_PLATFORM_WIN32_NT);
    isXP = FALSE;
    if(osvi.dwMajorVersion >= 5) {
        if((osvi.dwMajorVersion > 5) || (osvi.dwMajorVersion == 5 && osvi.dwMinorVersion >= 1)) {
            isXP = TRUE;
        }
        HANDLE user32 = GetModuleHandle(_T("user32"));
        SetLayeredWindowAttributesProc = (_tSetLayeredWindowAttributesProc) GetProcAddress((HINSTANCE)user32, "SetLayeredWindowAttributes");
    }
    else {
        SetLayeredWindowAttributesProc = NULL;
    }
    isCommCtrl6 = (GetDllVersion(_T("comctl32.dll")) >= PACKVERSION(6,0));
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetWindowLong(JNIEnv *pEnv, jclass jClass, jint hWnd, jint nIndex, jint dwNewLong)
{
    return SetWindowLong((HWND)hWnd, nIndex, (LONG)dwNewLong);
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetWindowLong(JNIEnv *pEnv, jclass jClass, jint hWnd, jint nIndex)
{
    return GetWindowLong((HWND)hWnd, nIndex);
}

JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetLayeredWindowAttributes(JNIEnv *pEnv, jclass jClass, jint hWnd, jint crRed, jint crGreen, jint crBlue, jint bAlpha, jint dwFlags)
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

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_RegQueryStrValue(JNIEnv *pEnv, jclass jClass, jint hRootKey, jstring sSubKey, jstring sValue)
{
    jstring result = NULL;
    TCHAR *value = NULL;
    HKEY hKey;
    DWORD type;
    DWORD cbData;
	LONG rv;

    LPCSTR str1 = (LPCSTR)pEnv->GetStringUTFChars(sSubKey, 0);
    LPCSTR str2 = (LPCSTR)pEnv->GetStringUTFChars(sValue, 0);
    if(ERROR_SUCCESS == (rv = RegOpenKeyEx((HKEY)hRootKey,
                                      str1,0, KEY_QUERY_VALUE, &hKey))) {
        if(ERROR_SUCCESS == (rv = RegQueryValueEx(hKey, str2, 0, &type, NULL, &cbData))) {
            value = (TCHAR *)GlobalAlloc(GPTR, cbData*sizeof(TCHAR));
            if(ERROR_SUCCESS == (rv = RegQueryValueEx(hKey, str2, 0, &type, (LPBYTE)value, &cbData))) {
                result = pEnv->NewStringUTF(value);
            }
            GlobalFree(value);
        }
        rv = RegCloseKey(hKey);
    }
    pEnv->ReleaseStringUTFChars(sSubKey, str1);
    pEnv->ReleaseStringUTFChars(sValue, str2);

    return result;
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetDesktopWindow(JNIEnv *pEnv, jclass jClass)
{
    return (jint)GetDesktopWindow();
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_HtmlHelp(JNIEnv *pEnv, jclass jClass, jint hwndCaller, jstring pszFile, jint uCommand, jint dwData)
{
    jint result = 0;
    if(pszFile) {
        LPCSTR file = (LPCSTR)pEnv->GetStringUTFChars(pszFile, 0);
        result = (jint)HtmlHelp((HWND)hwndCaller, file, (UINT)uCommand, (DWORD)dwData);
        pEnv->ReleaseStringUTFChars(pszFile, file);
    }
    return result;
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetUserDefaultLangID(JNIEnv *pEnv, jclass jClass)
{
    return GetUserDefaultLangID();
}

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_ExtractHtmlHelpAndTOC(JNIEnv *pEnv, jclass jClass, jstring pszFile, jstring pszFolder)
{
    jstring result = NULL;
    HRESULT hr = CoInitialize(NULL);

    if(hr == S_OK || hr == S_FALSE) {
        TCHAR *tocFile = NULL;
		int length = MAX_PATH*sizeof(TCHAR);
        tocFile = (TCHAR *)GlobalAlloc(GPTR, (length+1)*sizeof(TCHAR));

        LPCWSTR str1 = (LPCWSTR)pEnv->GetStringChars(pszFile, 0);
        LPCSTR str2 = (LPCSTR)pEnv->GetStringUTFChars(pszFolder, 0);
        if(ExtractHtmlHelpAndTOC(str1, str2, tocFile) == S_OK) {
            result = pEnv->NewStringUTF(tocFile);
        }

        GlobalFree(tocFile);
        pEnv->ReleaseStringChars(pszFile, str1);
        pEnv->ReleaseStringUTFChars(pszFolder, str2);

        if(hr == S_OK) {
            CoUninitialize();
        }
    }

    return result;
}

JNIEXPORT jobjectArray JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetPluginExports(JNIEnv *pEnv, jclass jClass, jstring pszPluginFile)
{
    jobjectArray result = NULL;

    if(pszPluginFile) {
        LPCSTR pluginFile =  (LPCSTR)pEnv->GetStringUTFChars(pszPluginFile, 0);
        unsigned char* dlldata    = 0;
        long dlldatalen = 0;
        bool loaded = false;
        
        FILE* dll = fopen(pluginFile,_T("rb"));
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
                    if (NTHeaders->OptionalHeader.NumberOfRvaAndSizes > IMAGE_DIRECTORY_ENTRY_EXPORT) {
                
                        DWORD ExportDirVA = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].VirtualAddress;
                        DWORD ExportDirSize = NTHeaders->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].Size;
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

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_SendMessage(JNIEnv *pEnv, jclass jClass, jint hWnd, jint msg, jint wParam, jint lParam)
{
    return SendMessage((HWND)hWnd, msg, wParam, lParam);
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_CallWindowProc(JNIEnv *pEnv, jclass jClass, jint lpWndProc, jint hWnd, jint Msg, jint wParam, jint lParam)
{
    return CallWindowProc((WNDPROC)lpWndProc, (HWND)hWnd, Msg, wParam, lParam);
}

JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_AreVisualStylesEnabled (JNIEnv *pEnv, jclass jClass)
{
    if(isXP && isCommCtrl6) {
        if(g_xpStyle.IsAppThemed() && g_xpStyle.IsThemeActive()) {
            return TRUE;
        }
    }
    return FALSE;
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_DrawWidgetThemeBackGround(JNIEnv *pEnv, jclass jClass, jint hWnd, jint hDC, 
                                                                                     jstring theme, jint partId, jint stateId)
{
    if(Java_net_sf_eclipsensis_util_WinAPI_AreVisualStylesEnabled(pEnv,jClass)) {
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
        pEnv->ReleaseStringChars(theme, pszTheme);
    }
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetSysColor(JNIEnv *pEnv, jclass jClass, jint index)
{
    return GetSysColor(index);
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetSystemMetrics(JNIEnv *pEnv, jclass jClass, jint index)
{
    return GetSystemMetrics(index);
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetObjectFieldValue(JNIEnv *pEnv, jclass jClass, jobject object, jstring field, jint value)
{
    jclass clasz = pEnv->GetObjectClass(object);
    LPCSTR fieldName = (LPCSTR)pEnv->GetStringUTFChars(field, 0);
    jfieldID fieldId = pEnv->GetFieldID(clasz,fieldName,_T("I"));
    pEnv->ReleaseStringUTFChars(field, fieldName);
    pEnv->SetIntField(object, fieldId, value);
}

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetEnvironmentVariable(JNIEnv *pEnv, jclass jClass, jstring name)
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
    
    return result;
}