@echo off
setlocal enabledelayedexpansion

echo Java Environment Detector for Data Mining Project
echo ==============================================
echo.

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java is not found in PATH. Please install Java first.
    goto :end
)

echo Detecting Java version...
java -version 2>&1 | findstr "version"

echo.
echo Checking JAVA_HOME environment variable...
if defined JAVA_HOME (
    set "JH=%JAVA_HOME%"
    if "!JH:~-1!"=="\" set "JH=!JH:~0,-1!"
    echo JAVA_HOME is set to: !JH!
    
    echo.
    echo For settings.json, use these values:
    echo.
    set "JSON_PATH=!JH:\=\\!"
    echo "path": "!JSON_PATH!",
    echo "sources": "!JSON_PATH!\\lib\\src.zip",
    
    if exist "!JH!\lib\src.zip" (
        echo.
        echo [OK] src.zip found at expected location
    ) else (
        echo.
        echo [WARNING] src.zip not found at !JH!\lib\src.zip
        echo You may need to download JDK sources separately
    )
) else (
    echo JAVA_HOME is not set. Trying to detect Java location...
    
    for /f "tokens=*" %%i in ('where java') do (
        set "JAVA_PATH=%%i"
        goto :found_java
    )
    
    :found_java
    echo Found Java at: !JAVA_PATH!
    
    set "JAVA_BIN_DIR=!JAVA_PATH:\java.exe=!"
    set "POTENTIAL_JDK_DIR=!JAVA_BIN_DIR!\.."
    pushd "!POTENTIAL_JDK_DIR!"
    set "JDK_DIR=!CD!"
    popd
    
    if "!JDK_DIR:~-1!"=="\" set "JDK_DIR=!JDK_DIR:~0,-1!"
    
    echo.
    echo For settings.json, try these values:
    echo.
    set "JSON_PATH=!JDK_DIR:\=\\!"
    echo "path": "!JSON_PATH!",
    echo "sources": "!JSON_PATH!\\lib\\src.zip",
    
    if exist "!JDK_DIR!\lib\src.zip" (
        echo.
        echo [OK] src.zip found at expected location
    ) else (
        echo.
        echo [WARNING] src.zip not found at !JDK_DIR!\lib\src.zip
        echo You may need to download JDK sources separately
    )
)

echo.
echo Common Java installation locations:
echo   - C:\Program Files\Java\jdk*
echo   - C:\Program Files\Eclipse Adoptium\jdk*
echo   - C:\Program Files\Amazon Corretto\jdk*
echo   - C:\Program Files\Zulu\jdk*

:end
echo.
echo Press any key to exit...
pause >nul
endlocal