@echo off
setlocal
cd /d "%~dp0"
title backend-demo-consumer-server

set "CONFIG_ERROR="
if not defined NACOS_ADDR (
    echo [ERROR] Required system variable NACOS_ADDR is not set.
    set "CONFIG_ERROR=1"
)
if not defined NACOS_NAMESPACE (
    echo [ERROR] Required system variable NACOS_NAMESPACE is not set.
    set "CONFIG_ERROR=1"
)
if not defined NACOS_USERNAME (
    echo [ERROR] Required system variable NACOS_USERNAME is not set.
    set "CONFIG_ERROR=1"
)
if not defined NACOS_PASSWORD (
    echo [ERROR] Required system variable NACOS_PASSWORD is not set.
    set "CONFIG_ERROR=1"
)
if defined CONFIG_ERROR (
    echo.
    echo Set the missing variables in Windows System variables, then open a new terminal.
    pause
    exit /b 1
)

if not exist "backend-demo-consumer-server.jar" (
    echo [ERROR] backend-demo-consumer-server.jar was not found.
    echo Run build-deploy.bat in the project root first.
    pause
    exit /b 1
)

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found. Check JAVA_HOME or PATH.
    pause
    exit /b 1
)

echo Starting backend-demo-consumer-server on port 8082...
echo Keep this window open while the service is running.
echo.

"%JAVA_EXE%" -jar "backend-demo-consumer-server.jar" --server.port=8082
set "EXIT_CODE=%ERRORLEVEL%"

echo.
echo backend-demo-consumer-server has stopped. Exit code: %EXIT_CODE%
pause
exit /b %EXIT_CODE%
