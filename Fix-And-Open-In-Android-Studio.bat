@echo off
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
set "STUDIO_EXE=C:\Program Files\Android\Android Studio\bin\studio64.exe"
set "STUDIO_SYSTEM_DIR=%LOCALAPPDATA%\Google\AndroidStudio2025.3.1"
set "PORT_FILE=%STUDIO_SYSTEM_DIR%\.port"

echo Fixing Android Studio startup lock...

powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*Android Studio*jbr*java.exe' } | Stop-Process -Force -ErrorAction SilentlyContinue"

if exist "%PORT_FILE%" (
    del /f /q "%PORT_FILE%" >nul 2>nul
)

if exist "%STUDIO_EXE%" (
    start "" "%STUDIO_EXE%" "%PROJECT_DIR%"
    exit /b 0
)

echo Android Studio was not found at:
echo %STUDIO_EXE%
echo.
echo Open Android Studio manually and choose this folder:
echo %PROJECT_DIR%
pause
