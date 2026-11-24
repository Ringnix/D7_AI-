@echo off
setlocal
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-deploy.ps1"
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if "%EXIT_CODE%"=="0" (
    echo Build completed. The deploy directory is ready.
) else (
    echo Build failed. Check the output above.
)

pause
exit /b %EXIT_CODE%
