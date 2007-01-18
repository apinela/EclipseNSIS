###############################################################################
# Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
# All rights reserved. 
# This program is made available under the terms of the Common Public License 
# v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
#
# Contributors:
#     Sunil Kamath (IcemanK) - initial API and implementation
###############################################################################
Name CreateEnv
OutFile CreateEnv.exe
SetCompressor lzma
SilentInstall silent

!macro STRIP_TRAILING_SLASH DIRNAME
    Push $R1
    StrCpy $R1 ${DIRNAME} 1 -1
    StrCmp $R1 \ +3
    StrCpy $R1 ${DIRNAME}
    GoTo +2
    StrCpy $R1 ${DIRNAME} -1
    Exch $R1
!macroend

Section
    Push $R0
    Push $R1
    Push $R2
    FileOpen $R1 env.bat w

;Find the JAVA_HOME
;
;Sun
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\JavaSoft\Java Development Kit\1.4" JavaHome
    StrCmp $R0 "" ibmsdk
    StrCpy $R2 "Sun"
    GoTo javahome

;IBM
ibmsdk:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\IBM\Java Development Kit\1.4" JavaHome
    StrCmp $R0 "" jrockit
    StrCpy $R2 "IBM"
    GoTo javahome

;BEA JRockit
jrockit:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\JRockit\Java Development Kit\1.4" JavaHome
    StrCmp $R0 "" dotnet
    StrCpy $R2 "BEA JRockit"

javahome:
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem *******$\r$\n"
    FileWrite $R1 "rem $R2 JDK 1.4$\r$\n"
    FileWrite $R1 "rem *******$\r$\n"
    FileWrite $R1 "set JAVA_HOME=$R0$\r$\n"
    FileWrite $R1 "set INCLUDE=%JAVA_HOME%\include;%JAVA_HOME%\include\win32;%INCLUDE%$\r$\n"
    FileWrite $R1 "set LIB=%JAVA_HOME%\lib;%LIB%$\r$\n"
    FileWrite $R1 "$\r$\n"

;msvc:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\DevStudio\6.0\Products\Microsoft Visual C++" ProductDir
    StrCmp $R0 "" 0 +3
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\VisualStudio\6.0\Setup\Microsoft Visual C++" ProductDir
    StrCmp $R0 "" dotnet
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem ************************$\r$\n"
    FileWrite $R1 "rem Microsoft Visual C++ 6.0$\r$\n"
    FileWrite $R1 "rem ************************$\r$\n"
    FileWrite $R1 "call $\"$R0\Bin\vcvars32.bat$\"$\r$\n"
    FileWrite $R1 "$\r$\n"
    goto psdk

dotnet:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\.NETFramework" sdkInstallRootv1.1
    StrCmp $R0 "" psdk
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem ********************************$\r$\n"
    FileWrite $R1 "rem Microsoft .NET Framework SDK 1.1$\r$\n"
    FileWrite $R1 "rem ********************************$\r$\n"
    FileWrite $R1 "set MS.NETDir=$R0$\r$\n"
    FileWrite $R1 "call $\"$R0\Bin\sdkvars.bat$\"$\r$\n"
    FileWrite $R1 "$\r$\n"
    ExpandEnvStrings $R0 "%VCToolkitInstallDir%"
    StrCmp $R0 "" psdk
    FileWrite $R1 "rem *********************************$\r$\n"
    FileWrite $R1 "rem Microsoft Visual C++ Toolkit 2003$\r$\n"
    FileWrite $R1 "rem *********************************$\r$\n"
    FileWrite $R1 "call $\"$R0\vcvars32.bat$\"$\r$\n"
    FileWrite $R1 "$\r$\n"

psdk:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Win32SDK\Directories" "Install Dir"
    StrCmp $R0 "" hhw
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem ***************************$\r$\n"
    FileWrite $R1 "rem Microsoft Platform SDK 2003$\r$\n"
    FileWrite $R1 "rem ***************************$\r$\n"
    FileWrite $R1 "call $\"$R0\setenv.bat$\"$\r$\n"
    FileWrite $R1 "$\r$\n"

hhw:
    ReadRegStr $R0 HKEY_CURRENT_USER "Software\Microsoft\HTML Help Workshop" InstallDir
    StrCmp $R0 "" done
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem ****************************$\r$\n"
    FileWrite $R1 "rem Microsoft HTML Help Workshop$\r$\n"
    FileWrite $R1 "rem ****************************$\r$\n"
    FileWrite $R1 "set HHW_HOME=$R0$\r$\n"
    FileWrite $R1 "set INCLUDE=%HHW_HOME%\include;%INCLUDE%$\r$\n"
    FileWrite $R1 "set LIB=%HHW_HOME%\lib;%LIB%$\r$\n"

done:
    FileClose $R1
    Pop $R2
    Pop $R1
    Pop $R0
SectionEnd
