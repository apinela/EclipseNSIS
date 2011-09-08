###############################################################################
# Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
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

;jdk
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\JavaSoft\Java Development Kit\1.5" JavaHome
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem *********$\r$\n"
    FileWrite $R1 "rem Sun JDK 5$\r$\n"
    FileWrite $R1 "rem *********$\r$\n"
    FileWrite $R1 "set JAVA_HOME=$R0$\r$\n"
    FileWrite $R1 "set INCLUDE=%JAVA_HOME%\include;%JAVA_HOME%\include\win32;%INCLUDE%$\r$\n"
    FileWrite $R1 "set LIB=%JAVA_HOME%\lib;%LIB%$\r$\n"
    FileWrite $R1 "$\r$\n"

;ms vc++ 2005:
    ReadRegStr $R0 HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\VisualStudio\8.0\Setup\VC" ProductDir
    StrCmp $R0 "" vcexp
!insertmacro STRIP_TRAILING_SLASH $R0
    Pop $R0
    FileWrite $R1 "rem ********************************$\r$\n"
    FileWrite $R1 "rem Microsoft Studio 2005 Visual C++$\r$\n"
    FileWrite $R1 "rem ********************************$\r$\n"
    FileWrite $R1 "set VCVARS_BAT=$R0\Bin\vcvarsall.bat$\r$\n"
    FileWrite $R1 "$\r$\n"
    goto psdk

vcexp:
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
