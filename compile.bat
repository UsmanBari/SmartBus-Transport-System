@echo off
echo Compiling Smart University Transport Management System...
if not exist out mkdir out
for /r src %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
setlocal enabledelayedexpansion
set SOURCES=
for /r src %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" -d out %SOURCES%
if %ERRORLEVEL%==0 (
    echo ==============================================
    echo  COMPILATION SUCCESSFUL
    echo ==============================================
) else (
    echo ==============================================
    echo  COMPILATION FAILED
    echo ==============================================
)
pause
