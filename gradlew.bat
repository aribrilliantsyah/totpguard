@echo off
REM Gradle wrapper script for Windows

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

set GRADLE_WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
set GRADLE_USER_HOME=%DIRNAME%\.gradle

if not exist "%GRADLE_WRAPPER_JAR%" (
    echo ERROR: Gradle wrapper jar not found at "%GRADLE_WRAPPER_JAR%"
    exit /b 1
)

java -jar "%GRADLE_WRAPPER_JAR%" %*