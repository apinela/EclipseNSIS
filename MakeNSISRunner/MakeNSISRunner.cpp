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
#include <tchar.h>
#include <process.h>
#include "htmlhelp.h"
#include "MakeNSISRunner.h"
#include "unicode.h"

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
_TCHAR *sOutputFile = NULL;
_TCHAR *sScript = NULL;
_TCHAR **sErrors = NULL;
int lErrorsCount = 0;
_TCHAR **sWarnings = NULL;
int lWarningsCount = 0;

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void RegisterWindowClass();
unsigned WINAPI CreateWndThread(LPVOID);

void ErrorHandler(_TCHAR*);
void throwException(JNIEnv*, char*, char*);
void freeArray(_TCHAR***, int*);
void freeString(_TCHAR**);
jobject makeArrayList(JNIEnv*, _TCHAR**);
jstring makeString(JNIEnv*, _TCHAR*);
void createString(_TCHAR**,PCOPYDATASTRUCT);
void createAppendArray(_TCHAR***, int*, PCOPYDATASTRUCT);

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

JNIEXPORT jlong JNICALL MakeNSISRunner_init(JNIEnv *pEnv, jobject obj)
{
    if(!g_isInit) {
    	jclass arrayListClass = pEnv->FindClass("java/util/ArrayList");
    	if(arrayListClass == NULL) {
    		throwException(pEnv, "java/lang/ClassNotFoundException", "java.util.ArrayList");
    		return 0;
    	}

    	g_arrayListConstructor = pEnv->GetMethodID(arrayListClass, "<init>", "()V");
    	if(g_arrayListConstructor == NULL) {
    		throwException(pEnv, "java/lang/NoSuchMethodException", "java.util.ArrayList()");
    		return 0;
    	}

    	g_arrayListAdd = pEnv->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    	if(g_arrayListAdd == NULL) {
    		throwException(pEnv,"java/lang/NoSuchMethodException", "java.util.ArrayList.add(Object o)");
    		return 0;
    	}

    	g_hWnd = CreateWindow(_T("Hidden EclipseNSIS Window"), NULL, WS_OVERLAPPEDWINDOW,
    						CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
    						NULL, NULL, hInstance, NULL);
    	if(g_hWnd == NULL) {
    		throwException(pEnv, "java/lang/RuntimeException", "CreateWindow");
    		return 0;
    	}

        UINT uThreadId = 0;
    	g_hThread = (HANDLE)_beginthreadex(NULL, 0, &CreateWndThread, g_hWnd, 0, &uThreadId);
    	if(!g_hThread)
    	{
    		throwException(pEnv, "java/lang/RuntimeException", "_beginthreadex");
    		return 0;
    	}

        g_arrayListClass = (jclass)pEnv->NewGlobalRef(arrayListClass);
        pEnv->DeleteLocalRef(arrayListClass);

        g_isInit = TRUE;
    }
	return (jlong)g_hWnd;
}

JNIEXPORT void JNICALL MakeNSISRunner_destroy(JNIEnv *pEnv, jobject obj)
{
    if(g_isInit) {
        pEnv->DeleteGlobalRef(g_arrayListClass);
    	MakeNSISRunner_reset(pEnv, obj);
    	PostMessage(g_hWnd,WM_QUIT,0,0);
    	CloseHandle(g_hThread);
    	g_hWnd = 0;
    	g_hThread = 0;
        g_isInit = FALSE;
    }
}

JNIEXPORT void JNICALL MakeNSISRunner_reset(JNIEnv *pEnv, jobject obj)
{
	freeString(&sOutputFile);
	freeString(&sScript);
	freeArray(&sErrors, &lErrorsCount);
	freeArray(&sWarnings, &lWarningsCount);
}

JNIEXPORT jobject JNICALL MakeNSISRunner_getErrors(JNIEnv *pEnv, jobject obj)
{
	return makeArrayList(pEnv, sErrors);
}

JNIEXPORT jstring JNICALL MakeNSISRunner_getOutputFileName(JNIEnv *pEnv, jobject obj)
{
	return makeString(pEnv, sOutputFile);
}

JNIEXPORT jstring JNICALL MakeNSISRunner_getScriptFileName(JNIEnv *pEnv, jobject obj)
{
	return makeString(pEnv, sScript);
}

JNIEXPORT jobject JNICALL MakeNSISRunner_getWarnings(JNIEnv *pEnv, jobject obj)
{
	return makeArrayList(pEnv, sWarnings);
}

// Utility functions

void freeArray(_TCHAR*** array, int *count)
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

void freeString(_TCHAR **string)
{
	if(*string) {
		GlobalFree(*string);
		*string = NULL;
	}
}

jobject makeArrayList(JNIEnv *pEnv, _TCHAR **items)
{
	if(items) {
		jobject arrayListObject;

		arrayListObject = pEnv->NewObject(g_arrayListClass,g_arrayListConstructor);
		if(arrayListObject == NULL) {
			throwException(pEnv, "java/lang/RuntimeException", "java.util.ArrayList()");
			return NULL;
		}

		int i=0;
		while(items[i]) {
			pEnv->CallBooleanMethod(arrayListObject, g_arrayListAdd, NewJavaString(pEnv,items[i]));
			i++;
		}

		return arrayListObject;

	}
	else {
		return NULL;
	}
}

jstring makeString(JNIEnv *pEnv, _TCHAR* string)
{
	if(string) {
		return NewJavaString(pEnv, string);
	}
	else {
		return NULL;
	}
}

void createString(_TCHAR** string, PCOPYDATASTRUCT cds)
{
	freeString(string);
	*string = (_TCHAR *)GlobalAlloc(GPTR, (cds->cbData)*sizeof(_TCHAR));
	_tcscpy(*string,(_TCHAR *)cds->lpData);
}

void createAppendArray(_TCHAR*** array, int *count, PCOPYDATASTRUCT cds)
{
	HGLOBAL hMem;

	if(*array) {
		hMem = GlobalHandle(*array);
		GlobalUnlock(hMem);
		hMem = GlobalReAlloc(hMem, (*count+2)*sizeof(_TCHAR *), GMEM_MOVEABLE|GMEM_ZEROINIT);
	}
	else {
		hMem = GlobalAlloc(GMEM_MOVEABLE|GMEM_ZEROINIT, (*count+2)*sizeof(_TCHAR *));
	}
	*array = (_TCHAR **)GlobalLock(hMem);
	(*array)[*count] = (_TCHAR *)GlobalAlloc(GPTR, (cds->cbData)*sizeof(_TCHAR));
	_tcscpy((*array)[*count],(_TCHAR *)cds->lpData);
	(*count)++;
	(*array)[*count] = NULL;
}

void ErrorHandler(_TCHAR* pszErrorMessage) {
	MessageBox(NULL, pszErrorMessage, _T("Error"), MB_OK | MB_ICONERROR);
}

void throwException(JNIEnv *pEnv, char *exception, char *errMsg) {
    jclass failex = pEnv->FindClass(exception);
    if( failex != NULL ){
        pEnv->ThrowNew(failex, errMsg);
    }
}
