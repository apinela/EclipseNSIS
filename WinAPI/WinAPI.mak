# Microsoft Developer Studio Generated NMAKE File, Based on WinAPI.dsp
!IF "$(CFG)" == ""
CFG=WinAPI - Win32 Debug
!MESSAGE No configuration specified. Defaulting to WinAPI - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "WinAPI - Win32 Release" && "$(CFG)" != "WinAPI - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "WinAPI.mak" CFG="WinAPI - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "WinAPI - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "WinAPI - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "WinAPI - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\WinAPI.dll"


CLEAN :
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\WinAPI.obj"
	-@erase "$(OUTDIR)\WinAPI.dll"
	-@erase "$(OUTDIR)\WinAPI.exp"
	-@erase "$(OUTDIR)\WinAPI.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "WINAPI_EXPORTS" /Fp"$(INTDIR)\WinAPI.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\WinAPI.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib htmlhelp.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\WinAPI.pdb" /machine:I386 /out:"$(OUTDIR)\WinAPI.dll" /implib:"$(OUTDIR)\WinAPI.lib" 
LINK32_OBJS= \
	"$(INTDIR)\WinAPI.obj"

"$(OUTDIR)\WinAPI.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "WinAPI - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\WinAPI.dll"


CLEAN :
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\WinAPI.obj"
	-@erase "$(OUTDIR)\WinAPI.dll"
	-@erase "$(OUTDIR)\WinAPI.exp"
	-@erase "$(OUTDIR)\WinAPI.ilk"
	-@erase "$(OUTDIR)\WinAPI.lib"
	-@erase "$(OUTDIR)\WinAPI.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "WINAPI_EXPORTS" /Fp"$(INTDIR)\WinAPI.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\WinAPI.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib htmlhelp.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\WinAPI.pdb" /debug /machine:I386 /out:"$(OUTDIR)\WinAPI.dll" /implib:"$(OUTDIR)\WinAPI.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\WinAPI.obj"

"$(OUTDIR)\WinAPI.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("WinAPI.dep")
!INCLUDE "WinAPI.dep"
!ELSE 
!MESSAGE Warning: cannot find "WinAPI.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "WinAPI - Win32 Release" || "$(CFG)" == "WinAPI - Win32 Debug"
SOURCE=.\WinAPI.cpp

"$(INTDIR)\WinAPI.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 
