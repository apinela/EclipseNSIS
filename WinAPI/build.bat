@echo off
rem *******************************************************************************
rem * Copyright (c) 2004 Sunil Kamath (IcemanK).
rem * All rights reserved. This program and the accompanying materials 
rem * are made available under the terms of the Common Public License v1.0
rem * which is available at http://www.eclipse.org/legal/cpl-v10.html
rem * 
rem * Contributors:
rem *     Sunil Kamath (IcemanK) - initial API and implementation
rem *******************************************************************************
setlocal

if exist env.bat goto ENV
..\CreateEnv\CreateEnv

:ENV
call env.bat

if "X%JAVA_HOME%"=="X" (
    echo Java SDK 1.4 is required.
    goto END
)

if not "X%MS.NETDir%"=="X" (
    goto FOUND
)

if "X%MSVCDir%"=="X" (
    echo Microsoft .NET Framework 1.1 SDK or Microsoft Visual Studio 6.0 is required.
    goto END
)

:FOUND
if "X%HHW_HOME%"=="X" (
    echo HTML Help Workshop 1.3 is required.
    goto END
)

set lib=%lib%;%JAVA_HOME%\lib;%HHW_HOME%\lib
set include=%include%;%JAVA_HOME%\include;%JAVA_HOME%\include\win32;%HHW_HOME%\include
set NO_EXTERNAL_DEPS=1

:MAKE
if "X%1"=="X" goto MAKE_RELEASE
if /i "%1"=="debug" goto MAKE_DEBUG
if /i "%1"=="release" goto MAKE_RELEASE
echo Usage: %0 [debug|release] [targets]
goto END

:MAKE_DEBUG
echo Building Debug
nmake -f WinAPI.mak CFG="WinAPI - Win32 Debug" %2 %3 %4 %5 %6 %7 %8 %9
if %errorlevel% GTR 0 goto END
if not exist Debug\WinAPI.dll goto END
copy /Y Debug\WinAPI.dll ..\net.sf.eclipsensis\os\win32\x86\
goto END

:MAKE_RELEASE
echo Building Release
nmake -f WinAPI.mak CFG="WinAPI - Win32 Release" %2 %3 %4 %5 %6 %7 %8 %9
if %errorlevel% GTR 0 goto END
if not exist Release\WinAPI.dll goto END
copy /Y Release\WinAPI.dll ..\net.sf.eclipsensis\os\win32\x86\

:END
endlocal