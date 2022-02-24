; Java Launcher
;--------------

!include FileFunc.nsh
!insertmacro GetParameters
!include x64.nsh

Name "Quorum Studio"
Caption "Quorum Studio"
Icon "quorum.ico"
OutFile "QuorumStudio.exe"

RequestExecutionLevel user

SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
!define CLASSPATH "QuorumStudio.jar"
!define CLASS "quorum.Main"
 
Section
    ${If} ${RunningX64}
        SetRegView 64
    ${Else}
        SetRegView 32
    ${EndIf}  
SectionEnd

Section ""
    Call GetJRE
    Pop $R0

      ${GetParameters} $R1
    ; change for your purpose (-jar etc.)
    StrCpy $0 '"$R0" -jar "${CLASSPATH}" "$R1'
      ;MessageBox MB_OK "$R0"

      ;ExecWait 'javaw.exe -jar QuorumStudio.jar'
    SetOutPath $EXEDIR
    Exec $0
SectionEnd
 

;
;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH
Function GetJRE
    Push $R0
    Push $R1

    ; use javaw.exe to avoid dosbox.
    ; use java.exe to keep stdout/stderr
    !define JAVAEXE "javaw.exe"

    ClearErrors
    StrCpy $R0 "$EXEDIR\Java\jdk\bin\${JAVAEXE}"
    IfFileExists $R0 JreFound  ;; 1) found it locally
    StrCpy $R0 ""

    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors 0 JreFound  ;; 2) found it in JAVA_HOME

    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"

    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"

    IfErrors 0 JreFound  ;; 3) found it in the registry
    StrCpy $R0 "${JAVAEXE}"  ;; 4) wishing you good luck

    JreFound:
    Pop $R1
    Exch $R0
FunctionEnd