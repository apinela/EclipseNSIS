# Microsoft Developer Studio Generated NMAKE File, Based on MakeNSISRunner.dsp
!IF "$(CFG)" == ""
CFG=MakeNSISRunner - Win32 Debug
!MESSAGE No configuration specified. Defaulting to MakeNSISRunner - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "MakeNSISRunner - Win32 Release" && "$(CFG)" != "MakeNSISRunner - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "MakeNSISRunner.mak" CFG="MakeNSISRunner - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "MakeNSISRunner - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "MakeNSISRunner - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "MakeNSISRunner - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\MakeNSISRunner.dll"


CLEAN :
	-@erase "$(INTDIR)\MakeNSISRunner.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\MakeNSISRunner.dll"
	-@erase "$(OUTDIR)\MakeNSISRunner.exp"
	-@erase "$(OUTDIR)\MakeNSISRunner.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "MAKENSISRUNNER_EXPORTS" /Fp"$(INTDIR)\MakeNSISRunner.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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

MTL=midl.exe
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MakeNSISRunner.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib jvm.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\MakeNSISRunner.pdb" /machine:I386 /out:"$(OUTDIR)\MakeNSISRunner.dll" /implib:"$(OUTDIR)\MakeNSISRunner.lib" 
LINK32_OBJS= \
	"$(INTDIR)\MakeNSISRunner.obj"

"$(OUTDIR)\MakeNSISRunner.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "MakeNSISRunner - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\MakeNSISRunner.dll"


CLEAN :
	-@erase "$(INTDIR)\MakeNSISRunner.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\MakeNSISRunner.dll"
	-@erase "$(OUTDIR)\MakeNSISRunner.exp"
	-@erase "$(OUTDIR)\MakeNSISRunner.ilk"
	-@erase "$(OUTDIR)\MakeNSISRunner.lib"
	-@erase "$(OUTDIR)\MakeNSISRunner.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "MAKENSISRUNNER_EXPORTS" /Fp"$(INTDIR)\MakeNSISRunner.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

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

MTL=midl.exe
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MakeNSISRunner.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib jvm.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\MakeNSISRunner.pdb" /debug /machine:I386 /out:"$(OUTDIR)\MakeNSISRunner.dll" /implib:"$(OUTDIR)\MakeNSISRunner.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\MakeNSISRunner.obj"

"$(OUTDIR)\MakeNSISRunner.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("MakeNSISRunner.dep")
!INCLUDE "MakeNSISRunner.dep"
!ELSE 
!MESSAGE Warning: cannot find "MakeNSISRunner.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "MakeNSISRunner - Win32 Release" || "$(CFG)" == "MakeNSISRunner - Win32 Debug"
SOURCE=.\MakeNSISRunner.cpp

"$(INTDIR)\MakeNSISRunner.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

