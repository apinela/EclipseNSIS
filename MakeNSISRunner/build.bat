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
rem *****
rem JDK
rem *****
set JAVA_HOME=d:\jdk1.4

rem ********
rem MSVC 6.0
rem ********
call "D:\Program Files\Microsoft Visual Studio\VC98\Bin\vcvars32.bat"

rem ****** 
rem MS-SDK
rem ******
set Mssdk="D:\Program Files\Microsoft SDK"
call %mssdk%\setenv.bat

set lib=%lib%;%JAVA_HOME%\lib
set include=%include%;%JAVA_HOME%\include;%JAVA_HOME%\include\win32

:MAKE
if "X%1"=="X" goto MAKE_RELEASE
if /i "%1"=="debug" goto MAKE_DEBUG
if /i "%1"=="release" goto MAKE_RELEASE
echo Usage: %0 [debug|release] [targets]
goto END

:MAKE_DEBUG
echo Building Debug
nmake -f MakeNSISRunner.mak CFG="MakeNSISRunner - Win32 Debug" %2 %3 %4 %5 %6 %7 %8 %9
if %errorlevel% GTR 0 goto END
if not exist Debug\MakeNSISRunner.dll goto END
copy /Y Debug\MakeNSISRunner.dll ..\net.sf.eclipsensis\os\win32\x86\
goto END

:MAKE_RELEASE
echo Building Release
nmake -f MakeNSISRunner.mak CFG="MakeNSISRunner - Win32 Release" %2 %3 %4 %5 %6 %7 %8 %9
if %errorlevel% GTR 0 goto END
if not exist Release\MakeNSISRunner.dll goto END
copy /Y Release\MakeNSISRunner.dll ..\net.sf.eclipsensis\os\win32\x86\

:END
endlocal