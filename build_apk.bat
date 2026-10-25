@echo off
echo ===================================================
echo   Compilando Carniceria SGNIA VIP Pro v5.0 (APK)
echo ===================================================
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "Path=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%Path%"

call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo   [EXITO] APK GENERADO CORRECTAMENTE!
    echo   Ubicacion: app\build\outputs\apk\debug\app-debug.apk
    echo ===================================================
    echo Abriendo la carpeta en el Explorador de Windows...
    explorer.exe /select,"app\build\outputs\apk\debug\app-debug.apk"
) else (
    echo.
    echo [ERROR] Hubo un problema durante la compilacion.
)
