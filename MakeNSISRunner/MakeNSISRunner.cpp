/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
#include <windows.h>
#include <tchar.h>
#include <process.h>
#include "htmlhelp.h"
#include "net_sf_eclipsensis_makensis_MakeNSISRunner.h"

enum {
  MAKENSIS_NOTIFY_SCRIPT,
  MAKENSIS_NOTIFY_WARNING,
  MAKENSIS_NOTIFY_ERROR,
  MAKENSIS_NOTIFY_OUTPUT
};

BOOL g_isInit = FALSE;
HWND g_hWnd = 0;
HANDLE g_hThread = 0;
JNIEnv* g_pEnv = NULL;
JavaVM* g_pJvm = NULL;
jclass g_arrayListClass = NULL;
jmethodID g_arrayListConstructor = NULL;
jmethodID g_arrayListAdd = NULL;

HINSTANCE hInstance = NULL;
TCHAR *sOutputFile = NULL;
TCHAR *sScript = NULL;
TCHAR **sErrors = NULL;
int lErrorsCount = 0;
TCHAR **sWarnings = NULL;
int lWarningsCount = 0;

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void RegisterWindowClass();
unsigned WINAPI CreateWndThread(LPVOID);

void ErrorHandler(LPCTSTR);
void throwException(JNIEnv*, TCHAR*, TCHAR*);
void freeArray(TCHAR***, int*);
void freeString(TCHAR**);
jobject makeArrayList(JNIEnv*, TCHAR**);
jstring makeString(JNIEnv*, TCHAR*);
void createString(TCHAR**,PCOPYDATASTRUCT);
void createAppendArray(TCHAR***, int*, PCOPYDATASTRUCT);

BOOL APIENTRY DllMain(HINSTANCE hinstDll, DWORD dwReasion, LPVOID lpReserved)
{
	if(dwReasion == DLL_PROCESS_ATTACH) {
		hInstance = hinstDll;
		RegisterWindowClass();
	}

	return TRUE;
}

void RegisterWindowClass() {
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX);
	wcex.style			= CS_HREDRAW | CS_VREDRAW;
	wcex.lpfnWndProc	= WndProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= hInstance;
	wcex.hIcon			= 0;
	wcex.hCursor		= 0;
	wcex.hbrBackground	= (HBRUSH)(COLOR_WINDOW + 1);
	wcex.lpszMenuName	= 0;
	wcex.lpszClassName	= _T("Hidden EclipseNSIS Window");
	wcex.hIconSm		= 0;

	RegisterClassEx(&wcex);
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
	if(Msg == WM_COPYDATA) {
		PCOPYDATASTRUCT cds = PCOPYDATASTRUCT(lParam);
		switch (cds->dwData) {
			case MAKENSIS_NOTIFY_SCRIPT:
				createString(&sScript, cds);

				break;
			case MAKENSIS_NOTIFY_WARNING:
				createAppendArray(&sWarnings, &lWarningsCount, cds);

				break;
			case MAKENSIS_NOTIFY_ERROR:
				createAppendArray(&sErrors, &lErrorsCount, cds);

				break;
			case MAKENSIS_NOTIFY_OUTPUT:
				createString(&sOutputFile, cds);

				break;
		}
	}
	else  {
		return DefWindowProc(hWnd, Msg, wParam, lParam);
	}

	return 0;
}

unsigned WINAPI CreateWndThread(LPVOID pThreadParam) {
	jint nSize = 1;
	jint nVms;
	MSG Msg;
	jint nStatus;

	HANDLE hWnd = (HANDLE)pThreadParam;

	nStatus = JNI_GetCreatedJavaVMs(&g_pJvm, nSize, &nVms);

	if(nStatus == 0) {
		nStatus = g_pJvm->AttachCurrentThreadAsDaemon((void **)&g_pEnv, NULL);
		if(nStatus != 0) ErrorHandler(_T("Cannot attach thread"));
	}
	else {
		ErrorHandler(_T("Cannot get the JVM"));
	}

	while(GetMessage(&Msg, 0, 0, 0)) {
		TranslateMessage(&Msg);
		DispatchMessage(&Msg);
	}
	return Msg.wParam;
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_init(JNIEnv *pEnv, jclass jcls)
{
    if(!g_isInit) {
    	jclass arrayListClass = pEnv->FindClass("java/util/ArrayList");
    	if(arrayListClass == NULL) {
    		throwException(pEnv, _T("java/lang/ClassNotFoundException"), _T("java.util.ArrayList"));
    		return 0;
    	}
    
    	g_arrayListConstructor = pEnv->GetMethodID(arrayListClass, "<init>", "()V");
    	if(g_arrayListConstructor == NULL) {
    		throwException(pEnv, _T("java/lang/NoSuchMethodException"), _T("java.util.ArrayList()"));
    		return 0;
    	}
    
    	g_arrayListAdd = pEnv->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    	if(g_arrayListAdd == NULL) {
    		throwException(pEnv, _T("java/lang/NoSuchMethodException"), _T("java.util.ArrayList.add(Object o)"));
    		return 0;
    	}
    
    	g_hWnd = CreateWindow(_T("Hidden EclipseNSIS Window"), NULL, WS_OVERLAPPEDWINDOW,
    						CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
    						NULL, NULL, hInstance, NULL);
    	if(g_hWnd == NULL) {
    		throwException(pEnv, _T("java/lang/RuntimeException"), _T("CreateWindow"));
    		return 0;
    	}
    
        UINT uThreadId = 0;
    	g_hThread = (HANDLE)_beginthreadex(NULL, 0, &CreateWndThread, g_hWnd, 0, &uThreadId);
    	if(!g_hThread)
    	{
    		throwException(pEnv, _T("java/lang/RuntimeException"), _T("_beginthreadex"));
    		return 0;
    	}
    
        g_arrayListClass = (jclass)pEnv->NewGlobalRef(arrayListClass);
        pEnv->DeleteLocalRef(arrayListClass);
        
        g_isInit = TRUE;
    }
	return (jlong)g_hWnd;
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_destroy(JNIEnv *pEnv, jclass jcls)
{
    if(g_isInit) {
        pEnv->DeleteGlobalRef(g_arrayListClass);
    	Java_net_sf_eclipsensis_makensis_MakeNSISRunner_reset(pEnv, jcls);
    	PostMessage(g_hWnd,WM_QUIT,0,0);
    	CloseHandle(g_hThread);
    	g_hWnd = 0;
    	g_hThread = 0;
        g_isInit = FALSE;
    }
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_reset(JNIEnv *pEnv, jclass jcls)
{
	freeString(&sOutputFile);
	freeString(&sScript);
	freeArray(&sErrors, &lErrorsCount);
	freeArray(&sWarnings, &lWarningsCount);
}

JNIEXPORT jobject JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_getErrors(JNIEnv *pEnv, jclass jcls)
{
	return makeArrayList(pEnv, sErrors);
}

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_getOutputFileName(JNIEnv *pEnv, jclass jcls)
{
	return makeString(pEnv, sOutputFile);
}

JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_getScriptFileName(JNIEnv *pEnv, jclass jcls)
{
	return makeString(pEnv, sScript);
}

JNIEXPORT jobject JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISRunner_getWarnings(JNIEnv *pEnv, jclass jcls)
{
	return makeArrayList(pEnv, sWarnings);
}

// Utility functions

void freeArray(TCHAR*** array, int *count)
{
	if(*array) {
		HGLOBAL hMem;
		int i=0;
		while((*array)[i]) {
			GlobalFree((*array)[i]);
			(*array)[i] = NULL;
			i++;
		}
		hMem = GlobalHandle(*array);
		GlobalUnlock(hMem);
		GlobalFree(hMem);
		*array = NULL;
		*count = 0;
	}
}

void freeString(TCHAR **string)
{
	if(*string) {
		GlobalFree(*string);
		*string = NULL;
	}
}

jobject makeArrayList(JNIEnv *pEnv, TCHAR **items)
{
	if(items) {
		jobject arrayListObject;

		arrayListObject = pEnv->NewObject(g_arrayListClass,g_arrayListConstructor);
		if(arrayListObject == NULL) {
			throwException(pEnv, _T("java/lang/RuntimeException"), _T("java.util.ArrayList()"));
			return NULL;
		}

		int i=0;
		while(items[i]) {
			pEnv->CallBooleanMethod(arrayListObject, g_arrayListAdd, pEnv->NewStringUTF(items[i]));
			i++;
		}

		return arrayListObject;

	}
	else {
		return NULL;
	}
}

jstring makeString(JNIEnv *pEnv, TCHAR* string)
{
	if(string) {
		return pEnv->NewStringUTF(string);
	}
	else {
		return NULL;
	}
}

void createString(TCHAR** string, PCOPYDATASTRUCT cds)
{
	freeString(string);
	*string = (TCHAR *)GlobalAlloc(GPTR, (cds->cbData)*sizeof(TCHAR));
	_tcscpy(*string,(TCHAR *)cds->lpData);
}

void createAppendArray(TCHAR*** array, int *count, PCOPYDATASTRUCT cds)
{
	HGLOBAL hMem;

	if(*array) {
		hMem = GlobalHandle(*array);
		GlobalUnlock(hMem);
		hMem = GlobalReAlloc(hMem, (*count+2)*sizeof(TCHAR *), GMEM_MOVEABLE|GMEM_ZEROINIT);
	}
	else {
		hMem = GlobalAlloc(GMEM_MOVEABLE|GMEM_ZEROINIT, (*count+2)*sizeof(TCHAR *));
	}
	*array = (TCHAR **)GlobalLock(hMem);
	(*array)[*count] = (TCHAR *)GlobalAlloc(GPTR, (cds->cbData)*sizeof(TCHAR));
	_tcscpy((*array)[*count],(TCHAR *)cds->lpData);
	(*count)++;
	(*array)[*count] = NULL;
}

void ErrorHandler(LPCTSTR pszErrorMessage) {
	MessageBox(NULL, pszErrorMessage, _T("Error"), MB_OK | MB_ICONERROR);
}

void throwException(JNIEnv *pEnv, TCHAR *exception, TCHAR *errMsg) {
    jclass failex = pEnv->FindClass(exception);
    if( failex != NULL ){
        pEnv->ThrowNew(failex, errMsg);
    }
}
