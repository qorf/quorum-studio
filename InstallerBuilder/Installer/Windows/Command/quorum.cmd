@echo off
rem The quorum command: Quorum's command-line compiler, run on the Java that
rem Quorum's installer carries. The installer puts this in <Quorum>\bin
rem (Program Files\Quorum\bin) and adds that folder to the system PATH.
rem
rem   quorum Hello.quorum         compiles, as the compiler always has
rem   quorum -run Hello.quorum    compiles, then runs the program if it built
rem
rem Every other flag goes to the compiler as it is (quorum -help lists them).
rem -run compiles to Java bytecode and runs Run\<name>.jar on that Java, so
rem it can't be used with -javascript, -python or -errors. Arguments can't
rem contain double quotes.
setlocal EnableExtensions DisableDelayedExpansion

rem Quorum's files are the folder above this script's.
for %%I in ("%~dp0..") do set "APP=%%~fI"
set "JAVA=%APP%\Java\jdk\bin\java.exe"
set "COMPILER=%APP%\Quorum.jar"

if not exist "%JAVA%" goto missing
if not exist "%COMPILER%" goto missing

rem Take -run out, and note the program's name (-name) and any flag -run can't
rem be used with.
set "RUN=no"
set "NAME=Default"
set "PREVIOUS="
set "OTHER="
set "ARGUMENTS="

:next
if "%~1"=="" goto parsed
if /i "%~1"=="-run" (
    set "RUN=yes"
    shift
    goto next
)
if /i "%PREVIOUS%"=="-name" set "NAME=%~1"
if /i "%~1"=="-javascript" set "OTHER=%~1"
if /i "%~1"=="-python" set "OTHER=%~1"
if /i "%~1"=="-errors" set "OTHER=%~1"
set ARGUMENTS=%ARGUMENTS% "%~1"
set "PREVIOUS=%~1"
shift
goto next

:parsed
if "%RUN%"=="yes" goto run
"%JAVA%" -jar "%COMPILER%" -library "%APP%\Library" %ARGUMENTS%
exit /b %ERRORLEVEL%

:run
if defined OTHER goto other

rem The old program goes first, so a build that fails can't leave it behind to
rem run. The compiler also exits with a non-zero code when the build fails.
if exist "Run\%NAME%.jar" del "Run\%NAME%.jar"
"%JAVA%" -jar "%COMPILER%" -library "%APP%\Library" %ARGUMENTS%
if errorlevel 1 exit /b %ERRORLEVEL%

if not exist "Run\%NAME%.jar" goto noprogram
"%JAVA%" -jar "Run\%NAME%.jar"
exit /b %ERRORLEVEL%

:other
echo quorum: -run runs the program on Java, so it can't be used with %OTHER%. 1>&2
exit /b 2

:noprogram
echo quorum: there's no Run\%NAME%.jar to run. 1>&2
exit /b 1

:missing
echo quorum: Quorum's files aren't where they should be (%APP%). Reinstall Quorum. 1>&2
exit /b 1
