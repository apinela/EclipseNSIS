/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Modified version of Win32Process_md.c
 * Copyright (c) 2003 Sun Microsystems, Inc. All rights reserved.
 *******************************************************************************/
#include <windows.h>
#include <stdio.h>
#include <io.h>
#include <tchar.h>
#include <process.h>
#include "net_sf_eclipsensis_makensis_MakeNSISProcess.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void * JNICALL JVM_GetThreadInterruptEvent();

#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */

#define PIPE_SIZE 512
BOOL g_isNT = FALSE;
BOOL g_isJRE5 = FALSE;
jfieldID g_fileDescriptorFD;

void throwException(JNIEnv *, TCHAR *, TCHAR *);

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_init(JNIEnv *pEnv, jclass jClass, jclass fdClass, jstring vmName, 
        jint vmMajorVersion, jint vmMinorVersion)
{
    OSVERSIONINFO ver;

    ver.dwOSVersionInfoSize = sizeof(ver);
    GetVersionEx(&ver);
    if (ver.dwPlatformId == VER_PLATFORM_WIN32_NT) {
        g_isNT = TRUE;
    }

    if(vmMajorVersion > 1 || (vmMajorVersion == 1 && vmMinorVersion >= 5)) {
        g_isJRE5 = TRUE;
    }
    if(g_isJRE5) {
    	g_fileDescriptorFD = pEnv->GetFieldID(fdClass,"handle","J");
    }
    else {
    	g_fileDescriptorFD = pEnv->GetFieldID(fdClass,"fd","I");
    }
	if(g_fileDescriptorFD == NULL) {
        if(g_isJRE5) {
    		throwException(pEnv,_T("java/lang/RuntimeException"),_T("Could not find field handle for java.io.FileDescriptor class"));
        }
        else {
    		throwException(pEnv,_T("java/lang/RuntimeException"),_T("Could not find field fd for java.io.FileDescriptor class"));
    	}
	}
}

JNIEXPORT jlong JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_create(JNIEnv *pEnv, jobject object, jstring command,
                                                                                jstring env, jstring workingDir, jobject stdinFD,
                                                                                jobject stdoutFD, jobject stderrFD)
{
    jlong ret = 0;
    HANDLE hread[3], hwrite[3];
    SECURITY_ATTRIBUTES sa={sizeof(sa),};
    SECURITY_DESCRIPTOR sd={0,};
    PROCESS_INFORMATION pi={0,};
    STARTUPINFO si;
    TCHAR *cmd, *cwd = NULL, *cmdenv = NULL;
    DWORD processFlag;

    if (!command) {
        throwException(pEnv, _T("java/lang/NullPointerException"), _T(""));
        return 0;
    }

    if (g_isNT) {
        InitializeSecurityDescriptor(&sd,SECURITY_DESCRIPTOR_REVISION);
        SetSecurityDescriptorDacl(&sd,true,NULL,false);
        sa.lpSecurityDescriptor = &sd;
    }
    else {
        sa.lpSecurityDescriptor = NULL;
    }
    sa.bInheritHandle = TRUE;

    memset(hread, 0, sizeof(hread));
    memset(hwrite, 0, sizeof(hwrite));
    if (!(CreatePipe(&hread[0], &hwrite[0], &sa, PIPE_SIZE) &&
          CreatePipe(&hread[1], &hwrite[1], &sa, PIPE_SIZE) &&
          CreatePipe(&hread[2], &hwrite[2], &sa, PIPE_SIZE))) {
        CloseHandle(hread[0]);
        CloseHandle(hread[1]);
        CloseHandle(hread[2]);
        CloseHandle(hwrite[0]);
        CloseHandle(hwrite[1]);
        CloseHandle(hwrite[2]);
        throwException(pEnv, _T("java/io/IOException"), _T("CreatePipe"));
        return 0;
    }

    memset(&si, 0, sizeof(si));
    si.cb = sizeof(si);
    si.dwFlags = STARTF_USESTDHANDLES|STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;
    si.hStdInput  = hread[0];
    si.hStdOutput = hwrite[1];
    si.hStdError  = hwrite[2];

    SetHandleInformation(hwrite[0], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[1],  HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[2],  HANDLE_FLAG_INHERIT, FALSE);

    if (g_isNT) {
        processFlag = CREATE_NO_WINDOW;
    }
    else {
        processFlag = DETACHED_PROCESS;
    }

    cmd = (TCHAR*)pEnv->GetStringUTFChars(command, 0);
    if (env) {
        cmdenv = (TCHAR*)pEnv->GetStringUTFChars(env, 0);
    }
    if (workingDir) { 
        cwd = (TCHAR*)pEnv->GetStringUTFChars(workingDir, 0);
    }

    ret = CreateProcess(0,                /* executable name */
                        cmd,              /* command line */
                        0,                /* process security attribute */
                        0,                /* thread security attribute */
                        TRUE,             /* inherits system handles */
                        processFlag,      /* selected based on exe type */ 
                        cmdenv,           /* environment block */
                        cwd,              /* change to the new current directory */
                        &si,              /* (in)  startup information */
                        &pi);             /* (out) process information */

    CloseHandle(hread[0]);
    CloseHandle(hwrite[1]);
    CloseHandle(hwrite[2]);

    if (!ret) {
        TCHAR msg[1024];
        CloseHandle(hwrite[0]);
        CloseHandle(hread[1]);
        CloseHandle(hread[2]);
        _stprintf(msg, _T("CreateProcess: %s error=%d"), cmd, GetLastError());
        throwException(pEnv, _T("java/io/IOException"), msg);
    }
    else {
        CloseHandle(pi.hThread);
        ret = (jlong)pi.hProcess;
        if(g_isJRE5) {
            pEnv->SetLongField(stdinFD, g_fileDescriptorFD, (jlong)hwrite[0]);
            pEnv->SetLongField(stdoutFD, g_fileDescriptorFD, (jlong)hread[1]);
            pEnv->SetLongField(stderrFD, g_fileDescriptorFD, (jlong)hread[2]);
        }
        else {
            pEnv->SetIntField(stdinFD, g_fileDescriptorFD, _open_osfhandle((long)hwrite[0], 0));
            pEnv->SetIntField(stdoutFD, g_fileDescriptorFD, _open_osfhandle((long)hread[1], 0));
            pEnv->SetIntField(stderrFD, g_fileDescriptorFD, _open_osfhandle((long)hread[2], 0));
        }
    }
    pEnv->ReleaseStringUTFChars(command, cmd);
    if (env) {
        pEnv->ReleaseStringUTFChars(env, cmdenv);
    }
    if (workingDir) {
        pEnv->ReleaseStringUTFChars(workingDir, cwd);
    }
    return ret;
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_exitValue(JNIEnv *pEnv, jobject object, jlong handle)
{
    jint exit_code;

    GetExitCodeProcess((void *)handle, (LPDWORD)&exit_code);
    if (exit_code == STILL_ACTIVE) {
        throwException(pEnv, _T("java/lang/IllegalThreadStateException"), _T("Process has not exited"));
        return -1;
    }
    return exit_code;
}

JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_waitFor(JNIEnv *pEnv, jobject object, jlong handle)
{
    long exit_code;
    int event;
    HANDLE events[2];

    events[0] = (void *)handle;
    events[1] = JVM_GetThreadInterruptEvent();

    event = WaitForMultipleObjects(2, events, FALSE, INFINITE);
    if (event == 0) {
        GetExitCodeProcess((void *)handle, (LPDWORD)&exit_code);
    } 
    else {
        throwException(pEnv, _T("java/lang/InterruptedException"), _T(""));
    }

    return exit_code;
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_destroy(JNIEnv *pEnv, jobject object, jlong handle)
{
    TerminateProcess((void *)handle, 1);
}

JNIEXPORT void JNICALL Java_net_sf_eclipsensis_makensis_MakeNSISProcess_close(JNIEnv *pEnv, jobject object, jlong handle)
{
    CloseHandle((void *)handle);
}
