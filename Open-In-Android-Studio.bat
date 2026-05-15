@echo off
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
set "STUDIO_EXE=C:\Program Files\Android\Android Studio\bin\studio64.exe"

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
