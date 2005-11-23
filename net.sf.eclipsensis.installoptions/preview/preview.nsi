!ifdef PREVIEW_MUI
!include "MUI.nsh"
!endif
XPStyle on
!ifdef PREVIEW_NAME
Name "${PREVIEW_NAME}"
!else
Name "InstallOptions Preview"
!endif
OutFile "preview.exe"

ReserveFile "${NSISDIR}\Plugins\InstallOptions.dll"
ReserveFile "${PREVIEW_INI}"
!ifdef PREVIEW_MUI
!ifdef PREVIEW_LANG
!insertmacro MUI_LANGUAGE "${PREVIEW_LANG}"
!else
!insertmacro MUI_LANGUAGE "English"
!endif
!else
!ifdef PREVIEW_LANG
LoadLanguageFile "${NSISDIR}\Contrib\Language files\${PREVIEW_LANG}.nlf"
!else
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
!endif
!endif

;Order of pages
Page custom dummy1 "" ""
Page custom Preview "" ""
Page custom dummy2 "" ""

!ifndef PREVIEW_MUI
!ifdef PREVIEW_BRANDING
BrandingText "${PREVIEW_BRANDING}"
!else
BrandingText "Click any button to close"
!endif  
 
Function .onGUIInit
  GetDlgItem $0 $HWNDPARENT 1028
  EnableWindow $0 1
FunctionEnd
!endif

Section
SectionEnd

Function Preview
  InitPluginsDir
  Push $R0
  File /oname=$PLUGINSDIR\preview.ini "${PREVIEW_INI}"
!ifdef PREVIEW_MUI
!ifdef PREVIEW_TITLE
  !define IO_TITLE "${PREVIEW_TITLE}"
!else
  !define IO_TITLE "InstallOptions Preview"
!endif  
!ifdef PREVIEW_SUBTITLE
  !define IO_SUBTITLE "${PREVIEW_SUBTITLE}"
!else
  !define IO_SUBTITLE "Click any button to close"
!endif  
  !insertmacro MUI_HEADER_TEXT "${IO_TITLE}" "${IO_SUBTITLE}"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "preview.ini"
!else
  InstallOptions::dialog "$PLUGINSDIR\preview.ini"
!endif
  Pop $R0
  StrCmp $R0 "success" done
  StrCmp $R0 "back" done
  StrCmp $R0 "cancel" done
  MessageBox MB_OK $R0
done:
  Pop $R0
FunctionEnd

Function dummy1
FunctionEnd
Function dummy2
FunctionEnd
