@echo off
setlocal

@rem
@rem
set "JAR_PATH=%~dp0..\..\protoc-gen\build\libs\protoc-gen-1.0.jar"

if not exist "%JAR_PATH%" (
    echo [ERROR] JAR file not found at: %JAR_PATH% >&2
    exit /b 1
)

java -jar "%JAR_PATH%"
exit /b %ERRORLEVEL%