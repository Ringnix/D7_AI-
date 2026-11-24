@echo off
setlocal
cd /d "%~dp0"

start "backend-demo-server" /D "%~dp0" cmd /k start-a.bat
timeout /t 5 /nobreak >nul
start "backend-demo-consumer-server" /D "%~dp0" cmd /k start-b.bat

exit /b 0
