@echo off
title Soviet Industry Mod Build

echo ========================================================
echo       BUILDING MOD: Soviet Industry (Forge 1.7.10)
echo ========================================================
echo.

where java >nul 2>nul
if %errorlevel% neq 0 goto NoJava

echo [OK] Java detected:
java -version
echo.

if not exist "gradle\wrapper\gradle-wrapper.jar" goto NoWrapper

echo [0/2] Checking Mojang caches and downloads...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0PreDownload.ps1"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Generate32x32Textures.ps1"
echo.

echo [1/2] Setting up Forge workspace...
call gradlew.bat setupDecompWorkspace
if %errorlevel% neq 0 goto BuildFail

echo.
echo [2/2] Building JAR with 3D Models...
call gradlew.bat build
if %errorlevel% neq 0 goto BuildFail

echo.
echo ========================================================
echo [SUCCESS] Mod build complete!
echo Output jar:
dir /b build\libs\SovietIndustry*.jar
echo.

:: Автоматичне копіювання в папку mods
if exist "%appdata%\.tlauncher\legacy\Minecraft\game\mods" (
    copy /y "build\libs\SovietIndustry-1.0.0.jar" "%appdata%\.tlauncher\legacy\Minecraft\game\mods\" >nul
    echo [OK] Автоматично оновлено мод у папці: .tlauncher\...\mods\
)
if exist "%appdata%\.minecraft\mods" (
    copy /y "build\libs\SovietIndustry-1.0.0.jar" "%appdata%\.minecraft\mods\" >nul
    echo [OK] Автоматично оновлено мод у папці: .minecraft\mods\
)

echo ========================================================
pause
exit /b 0

:NoJava
echo [ERROR] Java 8 (JDK) is not installed or not in PATH!
echo Forge 1.7.10 requires Java 8.
echo Download Eclipse Temurin JDK 8:
echo https://adoptium.net/temurin/releases/?version=8
echo.
pause
exit /b 1

:NoWrapper
echo [ERROR] gradle\wrapper\gradle-wrapper.jar was not found!
pause
exit /b 1

:BuildFail
echo [ERROR] Build failed! Check the error messages above.
pause
exit /b 1
