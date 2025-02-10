@echo off
setlocal
:: THIS SCRIPT DOESNT COMPILE THE BACKEND TO JAR FILE
:: COMPILING THE BACKEND WITH MAVEN AND MOVING THE JAR UNDER dev/Frontend/resources IS NECESSARY

:: Get the current script location
set "scriptLocation=%~dp0"

:: Define the file URLs (to be updated later)
set "file1URL=https://download.oracle.com/java/23/latest/jdk-23_windows-x64_bin.exe"
set "file2URL=https://aka.ms/vs/17/release/vc_redist.x64.exe"
set "file3URL=https://www.scipopt.org/download/release/SCIPOptSuite-9.2.1-win64.exe"

:: Define the target directory for downloading
set "downloadDirectory=%scriptLocation%\..\dev\Frontend\installers\windows"

:: Create the target directory if it doesn't exist
if not exist "%downloadDirectory%" mkdir "%downloadDirectory%"

:: Download the files if they don't already exist
if not exist "%downloadDirectory%\jdk-windows.exe" (
    echo Downloading file1...
    powershell -Command "Invoke-WebRequest -Uri %file1URL% -OutFile %downloadDirectory%\jdk-windows.exe"
)

if not exist "%downloadDirectory%\vc_redist.x64.exe" (
    echo Downloading file2...
    powershell -Command "Invoke-WebRequest -Uri %file2URL% -OutFile %downloadDirectory%\vc_redist.x64.exe"
)

if not exist "%downloadDirectory%\SCIPOptSuite-installer.exe" (
    echo Downloading file3...
    powershell -Command "Invoke-WebRequest -Uri %file3URL% -OutFile %downloadDirectory%\SCIPOptSuite-installer.exe"
)

:: Run the electron build command
echo Running npm electron build...
cd /d "%scriptLocation%\..\dev\Frontend"
npm run electron-build-win

:: Delete the Distribution directory if it exists
if exist "%scriptLocation%\..\Distribution" (
    echo Deleting old Distribution directory...
    rmdir /s /q "%scriptLocation%\..\Distribution"
)

:: Move the new Distribution directory to the script location
if exist "%scriptLocation%\..\dev\Frontend\Distribution" (
    echo Moving Distribution directory...
    move "%scriptLocation%\..\dev\Frontend\Distribution" "%scriptLocation%\.."
)

echo Script completed successfully.

endlocal
pause
