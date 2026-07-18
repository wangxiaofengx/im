@echo off
setlocal

if not defined JAVA_21_HOME (
    echo ERROR: JAVA_21_HOME is not set.
    exit /b 1
)

if not exist "%JAVA_21_HOME%\bin\jpackage.exe" (
    echo ERROR: jpackage.exe was not found under JAVA_21_HOME: "%JAVA_21_HOME%"
    exit /b 1
)

set "INPUT_DIR=%~dp0target\jpackage-input"
set "IMAGE_DIR=%~dp0target\jpackage-app-image"
set "INSTALLER_DIR=%~dp0dist\installer"
set "APP_IMAGE=%IMAGE_DIR%\VideoConferenceManager"
set "LOCAL_WIX=%~dp0.tools\wix314"

tasklist /FI "IMAGENAME eq VideoConferenceManager.exe" /NH 2>nul | find /I "VideoConferenceManager.exe" >nul
if not errorlevel 1 (
    echo ERROR: VideoConferenceManager is still running.
    echo Exit it from the system tray before rebuilding.
    exit /b 1
)

if exist "%IMAGE_DIR%" (
    attrib -R "%IMAGE_DIR%\*" /S /D >nul 2>nul
    rmdir /S /Q "%IMAGE_DIR%"
)
if exist "%IMAGE_DIR%" (
    echo ERROR: The temporary app image could not be removed: "%IMAGE_DIR%"
    echo Close any VideoConferenceManager process started from target and try again.
    exit /b 1
)
if exist "%INSTALLER_DIR%" (
    attrib -R "%INSTALLER_DIR%\*" /S /D >nul 2>nul
    rmdir /S /Q "%INSTALLER_DIR%"
)
if exist "%INSTALLER_DIR%" (
    echo ERROR: The previous installer could not be removed: "%INSTALLER_DIR%"
    echo Close the installer window and try again.
    exit /b 1
)

call "%~dp0z-package.bat"
if errorlevel 1 exit /b %ERRORLEVEL%

mkdir "%INPUT_DIR%" 2>nul
copy /y "%~dp0target\video-conference.jar" "%INPUT_DIR%\video-conference.jar" >nul

echo Creating the self-contained Windows application...
"%JAVA_21_HOME%\bin\jpackage.exe" ^
    --type app-image ^
    --name VideoConferenceManager ^
    --app-version 1.0.0 ^
    --vendor "Video Conference Manager" ^
    --description "Video conference service manager" ^
    --input "%INPUT_DIR%" ^
    --main-jar video-conference.jar ^
    --icon "%~dp0src\main\resources\static\favicon.ico" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "-Xms128m" ^
    --java-options "-Xmx128m" ^
    --dest "%IMAGE_DIR%"
if errorlevel 1 exit /b %ERRORLEVEL%

if exist "%LOCAL_WIX%\candle.exe" set "PATH=%LOCAL_WIX%;%PATH%"
where candle.exe >nul 2>nul
if errorlevel 1 goto :missing_wix
where light.exe >nul 2>nul
if errorlevel 1 goto :missing_wix

mkdir "%INSTALLER_DIR%" 2>nul
echo Creating the Windows EXE installer...
"%JAVA_21_HOME%\bin\jpackage.exe" ^
    --type exe ^
    --name VideoConferenceManager ^
    --app-version 1.0.0 ^
    --vendor "Video Conference Manager" ^
    --description "Video conference service manager" ^
    --app-image "%APP_IMAGE%" ^
    --icon "%~dp0src\main\resources\static\favicon.ico" ^
    --win-per-user-install ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "Video Conference Manager" ^
    --win-shortcut ^
    --dest "%INSTALLER_DIR%"
if errorlevel 1 exit /b %ERRORLEVEL%

echo.
echo Installer created under: "%INSTALLER_DIR%"
exit /b 0

:missing_wix
echo.
echo The temporary runnable application was created under: "%APP_IMAGE%"
echo WiX Toolset 3 is required to create the EXE installer.
echo Put WiX Toolset 3.14 binaries under ".tools\wix314", or add WiX bin to PATH.
echo Then run this script again.
exit /b 2
