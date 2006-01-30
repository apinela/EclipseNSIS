/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_sf_eclipsensis_util_WinAPI */

#ifndef _Included_net_sf_eclipsensis_util_WinAPI
#define _Included_net_sf_eclipsensis_util_WinAPI
#ifdef __cplusplus
extern "C" {
#endif
#undef net_sf_eclipsensis_util_WinAPI_GWL_EXSTYLE
#define net_sf_eclipsensis_util_WinAPI_GWL_EXSTYLE -20L
#undef net_sf_eclipsensis_util_WinAPI_GWL_STYLE
#define net_sf_eclipsensis_util_WinAPI_GWL_STYLE -16L
#undef net_sf_eclipsensis_util_WinAPI_GWL_WNDPROC
#define net_sf_eclipsensis_util_WinAPI_GWL_WNDPROC -4L
#undef net_sf_eclipsensis_util_WinAPI_WM_NCHITTEST
#define net_sf_eclipsensis_util_WinAPI_WM_NCHITTEST 132L
#undef net_sf_eclipsensis_util_WinAPI_WM_SETFOCUS
#define net_sf_eclipsensis_util_WinAPI_WM_SETFOCUS 7L
#undef net_sf_eclipsensis_util_WinAPI_WM_KEYDOWN
#define net_sf_eclipsensis_util_WinAPI_WM_KEYDOWN 256L
#undef net_sf_eclipsensis_util_WinAPI_WM_CHAR
#define net_sf_eclipsensis_util_WinAPI_WM_CHAR 258L
#undef net_sf_eclipsensis_util_WinAPI_WM_SYSCHAR
#define net_sf_eclipsensis_util_WinAPI_WM_SYSCHAR 262L
#undef net_sf_eclipsensis_util_WinAPI_LWA_COLORKEY
#define net_sf_eclipsensis_util_WinAPI_LWA_COLORKEY 1L
#undef net_sf_eclipsensis_util_WinAPI_LWA_ALPHA
#define net_sf_eclipsensis_util_WinAPI_LWA_ALPHA 2L
#undef net_sf_eclipsensis_util_WinAPI_WS_EX_LAYERED
#define net_sf_eclipsensis_util_WinAPI_WS_EX_LAYERED 524288L
#undef net_sf_eclipsensis_util_WinAPI_WS_EX_LAYOUTRTL
#define net_sf_eclipsensis_util_WinAPI_WS_EX_LAYOUTRTL 4194304L
#undef net_sf_eclipsensis_util_WinAPI_HH_DISPLAY_TOPIC
#define net_sf_eclipsensis_util_WinAPI_HH_DISPLAY_TOPIC 0L
#undef net_sf_eclipsensis_util_WinAPI_HTTRANSPARENT
#define net_sf_eclipsensis_util_WinAPI_HTTRANSPARENT -1L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_CLASSES_ROOT
#define net_sf_eclipsensis_util_WinAPI_HKEY_CLASSES_ROOT -2147483648L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_CURRENT_USER
#define net_sf_eclipsensis_util_WinAPI_HKEY_CURRENT_USER -2147483647L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_LOCAL_MACHINE
#define net_sf_eclipsensis_util_WinAPI_HKEY_LOCAL_MACHINE -2147483646L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_USERS
#define net_sf_eclipsensis_util_WinAPI_HKEY_USERS -2147483645L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_PERFORMANCE_DATA
#define net_sf_eclipsensis_util_WinAPI_HKEY_PERFORMANCE_DATA -2147483644L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_CURRENT_CONFIG
#define net_sf_eclipsensis_util_WinAPI_HKEY_CURRENT_CONFIG -2147483643L
#undef net_sf_eclipsensis_util_WinAPI_HKEY_DYN_DATA
#define net_sf_eclipsensis_util_WinAPI_HKEY_DYN_DATA -2147483642L
#undef net_sf_eclipsensis_util_WinAPI_BS_LEFTTEXT
#define net_sf_eclipsensis_util_WinAPI_BS_LEFTTEXT 32L
#undef net_sf_eclipsensis_util_WinAPI_CB_SHOWDROPDOWN
#define net_sf_eclipsensis_util_WinAPI_CB_SHOWDROPDOWN 335L
#undef net_sf_eclipsensis_util_WinAPI_CB_GETDROPPEDSTATE
#define net_sf_eclipsensis_util_WinAPI_CB_GETDROPPEDSTATE 343L
#undef net_sf_eclipsensis_util_WinAPI_WM_PRINT
#define net_sf_eclipsensis_util_WinAPI_WM_PRINT 791L
#undef net_sf_eclipsensis_util_WinAPI_PRF_NONCLIENT
#define net_sf_eclipsensis_util_WinAPI_PRF_NONCLIENT 2L
#undef net_sf_eclipsensis_util_WinAPI_PRF_CLIENT
#define net_sf_eclipsensis_util_WinAPI_PRF_CLIENT 4L
#undef net_sf_eclipsensis_util_WinAPI_PRF_ERASEBKGND
#define net_sf_eclipsensis_util_WinAPI_PRF_ERASEBKGND 8L
#undef net_sf_eclipsensis_util_WinAPI_PRF_CHILDREN
#define net_sf_eclipsensis_util_WinAPI_PRF_CHILDREN 16L
#undef net_sf_eclipsensis_util_WinAPI_EP_EDITTEXT
#define net_sf_eclipsensis_util_WinAPI_EP_EDITTEXT 1L
#undef net_sf_eclipsensis_util_WinAPI_ETS_NORMAL
#define net_sf_eclipsensis_util_WinAPI_ETS_NORMAL 1L
#undef net_sf_eclipsensis_util_WinAPI_ETS_DISABLED
#define net_sf_eclipsensis_util_WinAPI_ETS_DISABLED 4L
#undef net_sf_eclipsensis_util_WinAPI_ETS_READONLY
#define net_sf_eclipsensis_util_WinAPI_ETS_READONLY 6L
#undef net_sf_eclipsensis_util_WinAPI_LVP_LISTITEM
#define net_sf_eclipsensis_util_WinAPI_LVP_LISTITEM 1L
#undef net_sf_eclipsensis_util_WinAPI_LIS_NORMAL
#define net_sf_eclipsensis_util_WinAPI_LIS_NORMAL 1L
#undef net_sf_eclipsensis_util_WinAPI_LIS_DISABLED
#define net_sf_eclipsensis_util_WinAPI_LIS_DISABLED 4L
#undef net_sf_eclipsensis_util_WinAPI_COLOR_GRAYTEXT
#define net_sf_eclipsensis_util_WinAPI_COLOR_GRAYTEXT 17L
#undef net_sf_eclipsensis_util_WinAPI_COLOR_3DHILIGHT
#define net_sf_eclipsensis_util_WinAPI_COLOR_3DHILIGHT 20L
#undef net_sf_eclipsensis_util_WinAPI_WS_HSCROLL
#define net_sf_eclipsensis_util_WinAPI_WS_HSCROLL 1048576L
#undef net_sf_eclipsensis_util_WinAPI_WS_VSCROLL
#define net_sf_eclipsensis_util_WinAPI_WS_VSCROLL 2097152L
#undef net_sf_eclipsensis_util_WinAPI_LB_SETHORIZONTALEXTENT
#define net_sf_eclipsensis_util_WinAPI_LB_SETHORIZONTALEXTENT 404L
#undef net_sf_eclipsensis_util_WinAPI_SM_CXVSCROLL
#define net_sf_eclipsensis_util_WinAPI_SM_CXVSCROLL 2L
#undef net_sf_eclipsensis_util_WinAPI_SM_CYVSCROLL
#define net_sf_eclipsensis_util_WinAPI_SM_CYVSCROLL 20L
#undef net_sf_eclipsensis_util_WinAPI_SM_CYHSCROLL
#define net_sf_eclipsensis_util_WinAPI_SM_CYHSCROLL 3L
#undef net_sf_eclipsensis_util_WinAPI_SND_SYNC
#define net_sf_eclipsensis_util_WinAPI_SND_SYNC 0L
#undef net_sf_eclipsensis_util_WinAPI_SND_ASYNC
#define net_sf_eclipsensis_util_WinAPI_SND_ASYNC 1L
#undef net_sf_eclipsensis_util_WinAPI_SND_NODEFAULT
#define net_sf_eclipsensis_util_WinAPI_SND_NODEFAULT 2L
#undef net_sf_eclipsensis_util_WinAPI_SND_LOOP
#define net_sf_eclipsensis_util_WinAPI_SND_LOOP 8L
#undef net_sf_eclipsensis_util_WinAPI_SND_PURGE
#define net_sf_eclipsensis_util_WinAPI_SND_PURGE 64L
#undef net_sf_eclipsensis_util_WinAPI_SND_FILENAME
#define net_sf_eclipsensis_util_WinAPI_SND_FILENAME 131072L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_ARCHIVE
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_ARCHIVE 32L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_DIRECTORY
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_DIRECTORY 16L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_HIDDEN
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_HIDDEN 2L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_NORMAL
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_NORMAL 128L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_READONLY
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_READONLY 1L
#undef net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_SYSTEM
#define net_sf_eclipsensis_util_WinAPI_FILE_ATTRIBUTE_SYSTEM 4L
/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_init
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    SetWindowLong
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetWindowLong
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetWindowLong
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetWindowLong
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    SetLayeredWindowAttributes
 * Signature: (IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetLayeredWindowAttributes
  (JNIEnv *, jclass, jint, jint, jint, jint, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    RegQueryStrValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_RegQueryStrValue
  (JNIEnv *, jclass, jint, jstring, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetDesktopWindow
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetDesktopWindow
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    HtmlHelp
 * Signature: (ILjava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_HtmlHelp
  (JNIEnv *, jclass, jint, jstring, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetUserDefaultLangID
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetUserDefaultLangID
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetUserDefaultUILanguage
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetUserDefaultUILanguage
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    ExtractHtmlHelpAndTOC
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_ExtractHtmlHelpAndTOC
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetPluginExports
 * Signature: (Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetPluginExports
  (JNIEnv *, jclass, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    SendMessage
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_SendMessage
  (JNIEnv *, jclass, jint, jint, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    CallWindowProc
 * Signature: (IIIII)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_CallWindowProc
  (JNIEnv *, jclass, jint, jint, jint, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    AreVisualStylesEnabled
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_AreVisualStylesEnabled
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    DrawWidgetThemeBackGround
 * Signature: (IILjava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_DrawWidgetThemeBackGround
  (JNIEnv *, jclass, jint, jint, jstring, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetSysColor
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetSysColor
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetSystemMetrics
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetSystemMetrics
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    SetObjectFieldValue
 * Signature: (Ljava/lang/Object;Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetObjectFieldValue
  (JNIEnv *, jclass, jobject, jstring, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetEnvironmentVariable
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetEnvironmentVariable
  (JNIEnv *, jclass, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    strftime
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_strftime
  (JNIEnv *, jclass, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetShellFolder
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetShellFolder
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetShortPathName
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetShortPathName
  (JNIEnv *, jclass, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    RegGetSubKeys
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_eclipsensis_util_WinAPI_RegGetSubKeys
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    RegKeyExists
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_RegKeyExists
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    getDefaultAuthenticator
 * Signature: ()Ljava/net/Authenticator;
 */
JNIEXPORT jobject JNICALL Java_net_sf_eclipsensis_util_WinAPI_getDefaultAuthenticator
  (JNIEnv *, jclass);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    PlaySound
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_PlaySound
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    GetFileAttributes
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sf_eclipsensis_util_WinAPI_GetFileAttributes
  (JNIEnv *, jclass, jstring);

/*
 * Class:     net_sf_eclipsensis_util_WinAPI
 * Method:    SetFileAttributes
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_eclipsensis_util_WinAPI_SetFileAttributes
  (JNIEnv *, jclass, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif
