@echo off
setlocal
title video-conference

if not defined JAVA_21_HOME (
    echo ERROR: JAVA_21_HOME is not set.
    exit /b 1
)

if not exist "%JAVA_21_HOME%\bin\java.exe" (
    echo ERROR: Java was not found under JAVA_21_HOME: "%JAVA_21_HOME%"
    exit /b 1
)

set "JAVA_HOME=%JAVA_21_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

"%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -Xms128m -Xmx128m -jar target\video-conference.jar
exit /b %ERRORLEVEL%
