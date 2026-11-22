; Unicode, so NSIS builds a Unicode installer and reads paths with any letters.
Unicode true

;Quorum's installer for Windows: the command-line compiler on its own, with
;no Studio. It installs the quorum command (bin\quorum.cmd), the compiler
;(Quorum.jar) and the library jars beside it, the library, and a Windows
;JDK, and puts bin on the system PATH.

  !include "MUI2.nsh"
  !include "LogicLib.nsh"
  !include "WinMessages.nsh"

;--------------------------------
;What InstallerBuilder gives this script, with makensis -D options (such as
;-DVERSION=14.5.0). Each has a default, for building it by hand from this
;folder, with quorum-language beside quorum-studio.
;
;  QUORUM           quorum-language's Quorum folder, with Run and Library
;  QUORUM_JAR       the command-line compiler (Run\Default.jar, built with
;                   Main.quorum as the start), installed as Quorum.jar
;  JDK              the Windows JDK, installed as Java\jdk
;  OUTPUT           where the installer goes
;  VERSION          Quorum's version, such as 14.5.0
;  PRODUCT_VERSION  the version with four numbers, such as 14.5.0.0
;  SIGN             a command that signs a program named after it. Given
;                   this, the uninstaller and then the installer are signed
;                   as they're made (NSIS 3.08 or later).

    !ifndef QUORUM
        !define QUORUM "..\..\..\..\quorum-language\Quorum"
    !endif
    !ifndef QUORUM_JAR
        !define QUORUM_JAR "${QUORUM}\Run\Default.jar"
    !endif
    !ifndef JDK
        !define JDK "..\..\..\QuorumStudio\Java\jdk"
    !endif
    !ifndef OUTPUT
        !define OUTPUT "..\..\Build\Quorum-Windows.exe"
    !endif
    !ifndef VERSION
        !define VERSION "14.5.0"
    !endif
    !ifndef PRODUCT_VERSION
        !define PRODUCT_VERSION "${VERSION}.0"
    !endif
    !define REGISTRY_KEY "Software\Quorum"
    !define UNINSTALL_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\Quorum"
    ;Windows PowerShell, which edits the PATH (QuorumPath.ps1). The PATH is
    ;under HKLM\SYSTEM, which 32-bit and 64-bit programs share, so either
    ;PowerShell will do.
    !define POWERSHELL "$SYSDIR\WindowsPowerShell\v1.0\powershell.exe"

    VIProductVersion "${PRODUCT_VERSION}"
    VIFileVersion "${PRODUCT_VERSION}"
    VIAddVersionKey "FileVersion" "${VERSION}"
    VIAddVersionKey "ProductVersion" "${VERSION}"
    VIAddVersionKey "ProductName" "Quorum"
    VIAddVersionKey "LegalCopyright" "(C) Andreas Stefik and William Allee"
    VIAddVersionKey "FileDescription" "Quorum's command-line compiler, with the quorum command."

  Name "Quorum"
  Caption "Quorum ${VERSION} Installer"
  Icon "..\quorum.ico"
  OutFile "${OUTPUT}"

  ;The uninstaller is made inside the installer, so it's signed as it's
  ;made, and the installer once it's written. A sign that fails stops the
  ;build.
  !ifdef SIGN
    !uninstfinalize '${SIGN} "%1"' = 0
    !finalize '${SIGN} "%1"' = 0
  !endif

  InstallDir "$PROGRAMFILES64\Quorum"
  InstallDirRegKey HKCU ${REGISTRY_KEY} ""

  ;Admin, to install in Program Files and change the system PATH.
  RequestExecutionLevel admin

;--------------------------------
;Pages

  !define MUI_ABORTWARNING
  !insertmacro MUI_PAGE_LICENSE "..\License.txt"
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES

  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer

Section "Quorum"

  SetOutPath "$INSTDIR"

  ;A new library, jars and JDK replace the old ones entirely.
  RMDir /r "$INSTDIR\Library"
  RMDir /r "$INSTDIR\libraries"
  RMDir /r "$INSTDIR\Java"

  ;Each folder is copied as folder\*.* into its own place, as in Studio's
  ;installer. A missing file stops the build.
  File "/oname=Quorum.jar" "${QUORUM_JAR}"
  File "${QUORUM}\Run\QuorumStandardLibrary.jar"
  File "${QUORUM}\Run\QuorumStandardPlugins.jar"
  File "QuorumPath.ps1"
  File "..\quorum.ico"
  ;The jars QuorumStandardPlugins.jar's manifest names, such as ASM, which
  ;the compiler writes bytecode with.
  SetOutPath "$INSTDIR\libraries"
  File /r /x .DS_Store "${QUORUM}\Run\libraries\*.*"
  SetOutPath "$INSTDIR\Library"
  File /r /x .DS_Store "${QUORUM}\Library\*.*"
  SetOutPath "$INSTDIR\Java\jdk"
  File /r "${JDK}\*.*"
  ;The command in its own folder, so the PATH picks up only it.
  SetOutPath "$INSTDIR\bin"
  File "quorum.cmd"
  SetOutPath "$INSTDIR"

  WriteRegStr HKCU ${REGISTRY_KEY} "" $INSTDIR
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "${UNINSTALL_KEY}" "DisplayName" "Quorum ${VERSION}"
  WriteRegStr HKLM "${UNINSTALL_KEY}" "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM "${UNINSTALL_KEY}" "DisplayIcon" "$INSTDIR\quorum.ico"
  WriteRegStr HKLM "${UNINSTALL_KEY}" "UninstallString" "$\"$INSTDIR\Uninstall.exe$\""

  ;Put the bin folder on the system PATH, then tell running programs the
  ;environment changed, so new consoles see it.
  nsExec::ExecToLog '"${POWERSHELL}" -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "$INSTDIR\QuorumPath.ps1" -Action Add -Folder "$INSTDIR\bin"'
  Pop $0
  ${If} $0 != 0
    DetailPrint "Could not add $INSTDIR\bin to the PATH ($0). The quorum command can still be run from there."
  ${EndIf}
  SendMessage ${HWND_BROADCAST} ${WM_SETTINGCHANGE} 0 "STR:Environment" /TIMEOUT=5000

SectionEnd

;--------------------------------
;Uninstaller

Section "Uninstall"

  ;Take bin off the system PATH first, while the script that does it is
  ;still installed.
  nsExec::ExecToLog '"${POWERSHELL}" -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "$INSTDIR\QuorumPath.ps1" -Action Remove -Folder "$INSTDIR\bin"'
  Pop $0
  SendMessage ${HWND_BROADCAST} ${WM_SETTINGCHANGE} 0 "STR:Environment" /TIMEOUT=5000

  RMDir /r "$INSTDIR"
  DeleteRegKey HKLM "${UNINSTALL_KEY}"
  DeleteRegKey /ifempty HKCU ${REGISTRY_KEY}

SectionEnd
