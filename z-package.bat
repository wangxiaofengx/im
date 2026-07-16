@echo off
setlocal

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

call mvn clean package
exit /b %ERRORLEVEL%
