@echo off
setlocal

set OUT=out\production\Parking-Manager
set CP=lib\gson-2.11.0.jar

echo [1/2] Compiling...
if not exist "%OUT%" mkdir "%OUT%"

:: Collect all .java files into a temp list
dir /s /b src\*.java > .sources.tmp 2>nul

javac -cp "%CP%" -d "%OUT%" @.sources.tmp
del .sources.tmp

if %ERRORLEVEL% neq 0 (
    echo [!] Compilation failed. Aborting.
    exit /b 1
)

echo [2/2] Starting Parking Manager...
echo.
java -cp "%OUT%;%CP%" Main

endlocal
