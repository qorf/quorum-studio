;NSIS Modern User Interface
;Quorum Studio Installer Script

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  ;Name and file
  !define VERSION "1.0 Beta 1"
  !define REGISTRY_KEY "Software\QuorumStudio"

  Name "Quorum Studio ${VERSION}"
  Caption "Quorum Studio"
  Icon "quorum.ico"
  OutFile "Quorum Studio ${VERSION}.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES64\QuorumStudio"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU ${REGISTRY_KEY} ""

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin

;--------------------------------
;Variables

  Var StartMenuFolder

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  
  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
  !define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGISTRY_KEY}
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
  
  !insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
  
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

XPStyle on

Function .onInit
	# the plugins dir is automatically deleted when the installer exits
	InitPluginsDir
	File /oname=$PLUGINSDIR\Quorum.bmp "..\Resources\Graphics\Interface\Quorum.bmp"

	splash::show 1000 $PLUGINSDIR\Quorum

	Pop $0 ; $0 has '1' if the user closed the splash screen early,
			; '0' if everything closed normally, and '-1' if some error occurred.
FunctionEnd

Section "Core" SecDummy

  SetOutPath "$INSTDIR"
  
  ;ADD YOUR OWN FILES HERE...
  File QuorumStudio.exe
  File quorum.ico
  File /nonfatal /r "..\Run\*.*"
  File /nonfatal /r "..\External"
  File /nonfatal /r "..\Library"
  File /nonfatal /r "..\Resources"
  File /nonfatal /r "..\Templates"

  ;Store installation folder
  WriteRegStr HKCU ${REGISTRY_KEY} "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Quorum Studio.lnk" "$INSTDIR\QuorumStudio.exe" "" "$INSTDIR\quorum.ico"
    CreateShortcut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuorumStudio" \
                 "DisplayName" "Quorum Studio 1.0 Beta 1"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuorumStudio" \
                 "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd

;--------------------------------
;Descriptions

    ;!define MUI_FINISHPAGE_RUN "$INSTDIR\QuorumStudio.exe"
    ;!insertmacro MUI_PAGE_FINISH

  ;Language strings
  LangString DESC_SecDummy ${LANG_ENGLISH} "Introduction."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} $(DESC_SecDummy)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END
  
;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...

  Delete "$INSTDIR\Uninstall.exe"

  RMDir /r "$INSTDIR"
  RMDir /r "$APPDATA\Quorum Studio"

  !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder
    
  Delete "$SMPROGRAMS\$StartMenuFolder\Quorum Studio.lnk"
  Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
  RMDir "$SMPROGRAMS\$StartMenuFolder"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuorumStudio"
  
  DeleteRegKey /ifempty HKCU ${REGISTRY_KEY}

SectionEnd