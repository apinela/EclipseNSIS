/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
#ifndef _Included_WinAPI
#define _Included_WinAPI

#include "net_sf_eclipsensis_util_winapi_WinAPI.h"

#define WinAPI_GetUserDefaultLangID Java_net_sf_eclipsensis_util_winapi_WinAPI_GetUserDefaultLangID
#define WinAPI_GetUserDefaultUILanguage Java_net_sf_eclipsensis_util_winapi_WinAPI_GetUserDefaultUILanguage
#define WinAPI_ExtractHtmlHelp Java_net_sf_eclipsensis_util_winapi_WinAPI_ExtractHtmlHelp
#define WinAPI_GetPluginExports Java_net_sf_eclipsensis_util_winapi_WinAPI_GetPluginExports
#define WinAPI_AreVisualStylesEnabled Java_net_sf_eclipsensis_util_winapi_WinAPI_AreVisualStylesEnabled
#define WinAPI_GetSysColor Java_net_sf_eclipsensis_util_winapi_WinAPI_GetSysColor
#define WinAPI_GetSystemMetrics Java_net_sf_eclipsensis_util_winapi_WinAPI_GetSystemMetrics
#define WinAPI_GetObjectFieldValue Java_net_sf_eclipsensis_util_winapi_WinAPI_GetObjectFieldValue
#define WinAPI_SetIntFieldValue Java_net_sf_eclipsensis_util_winapi_WinAPI_SetIntFieldValue
#define WinAPI_GetEnvironmentVariable Java_net_sf_eclipsensis_util_winapi_WinAPI_GetEnvironmentVariable
#define WinAPI_GetShellFolder Java_net_sf_eclipsensis_util_winapi_WinAPI_GetShellFolder
#define WinAPI_GetShortPathName Java_net_sf_eclipsensis_util_winapi_WinAPI_GetShortPathName
#define WinAPI_GetFileAttributes Java_net_sf_eclipsensis_util_winapi_WinAPI_GetFileAttributes
#define WinAPI_SetFileAttributes Java_net_sf_eclipsensis_util_winapi_WinAPI_SetFileAttributes
#define WinAPI_GetKeyState Java_net_sf_eclipsensis_util_winapi_WinAPI_GetKeyState
#define WinAPI_ValidateWildcard Java_net_sf_eclipsensis_util_winapi_WinAPI_ValidateWildcard
#define WinAPI_SetRegView Java_net_sf_eclipsensis_util_winapi_WinAPI_SetRegView
#define WinAPI_GetRegView Java_net_sf_eclipsensis_util_winapi_WinAPI_GetRegView
#define WinAPI_LoadResourceString Java_net_sf_eclipsensis_util_winapi_WinAPI_LoadResourceString
#define WinAPI_Strftime Java_net_sf_eclipsensis_util_winapi_WinAPI_Strftime
#define WinAPI_GetDefaultAuthenticator Java_net_sf_eclipsensis_util_winapi_WinAPI_GetDefaultAuthenticator

#ifdef WIN64

#include "net_sf_eclipsensis_util_winapi_x64_WinAPI64.h"

#define WinAPI_init Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_init
#define WinAPI_SetWindowLong Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_SetWindowLong
#define WinAPI_GetWindowLong Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_GetWindowLong
#define WinAPI_SetWindowLongPtr Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_SetWindowLongPtr
#define WinAPI_GetWindowLongPtr Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_GetWindowLongPtr
#define WinAPI_SetLayeredWindowAttributes Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_SetLayeredWindowAttributes
#define WinAPI_GetDesktopWindow Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_GetDesktopWindow
#define WinAPI_HtmlHelp Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_HtmlHelp
#define WinAPI_SendMessage Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_SendMessage
#define WinAPI_CallWindowProc Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_CallWindowProc
#define WinAPI_DrawWidgetThemeBackGround Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_DrawWidgetThemeBackGround
#define WinAPI_DrawWidgetThemeBorder Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_DrawWidgetThemeBorder
#define WinAPI_PlaySound Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_PlaySound
#define WinAPI_RegQueryStrValue Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegQueryStrValue
#define WinAPI_RegGetSubKeys Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegGetSubKeys
#define WinAPI_RegKeyExists Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegKeyExists
#define WinAPI_RegOpenKeyEx Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegOpenKeyEx
#define WinAPI_RegCloseKey Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegCloseKey
#define WinAPI_RegQueryInfoKey Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegQueryInfoKey
#define WinAPI_RegEnumKeyEx Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegEnumKeyEx
#define WinAPI_GetRegValuesCount Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_GetRegValuesCount
#define WinAPI_RegEnumValue Java_net_sf_eclipsensis_util_winapi_x64_WinAPI64_RegEnumValue

#define jhandle jlong
#define jlongptr jlong

#else

#include "net_sf_eclipsensis_util_winapi_x86_WinAPI32.h"

#define WinAPI_init Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_init
#define WinAPI_SetWindowLong Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_SetWindowLong
#define WinAPI_GetWindowLong Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_GetWindowLong
#define WinAPI_SetWindowLongPtr Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_SetWindowLongPtr
#define WinAPI_GetWindowLongPtr Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_GetWindowLongPtr
#define WinAPI_SetLayeredWindowAttributes Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_SetLayeredWindowAttributes
#define WinAPI_GetDesktopWindow Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_GetDesktopWindow
#define WinAPI_HtmlHelp Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_HtmlHelp
#define WinAPI_SendMessage Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_SendMessage
#define WinAPI_CallWindowProc Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_CallWindowProc
#define WinAPI_DrawWidgetThemeBackGround Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_DrawWidgetThemeBackGround
#define WinAPI_DrawWidgetThemeBorder Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_DrawWidgetThemeBorder
#define WinAPI_PlaySound Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_PlaySound
#define WinAPI_RegQueryStrValue Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegQueryStrValue
#define WinAPI_RegGetSubKeys Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegGetSubKeys
#define WinAPI_RegKeyExists Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegKeyExists
#define WinAPI_RegOpenKeyEx Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegOpenKeyEx
#define WinAPI_RegCloseKey Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegCloseKey
#define WinAPI_RegQueryInfoKey Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegQueryInfoKey
#define WinAPI_RegEnumKeyEx Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegEnumKeyEx
#define WinAPI_GetRegValuesCount Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_GetRegValuesCount
#define WinAPI_RegEnumValue Java_net_sf_eclipsensis_util_winapi_x86_WinAPI32_RegEnumValue

#define jhandle jint
#define jlongptr jint

#endif

#endif