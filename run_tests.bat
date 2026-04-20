@echo off
setlocal enabledelayedexpansion
echo ============================================================
echo  Smart University Transport System — Sprint 3 Test Runner
echo ============================================================
echo.

REM === Set classpath ===
set CP=lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar;lib\mysql-connector-j-8.0.33.jar;out;test_out

REM === Compile source code first ===
echo [1/3] Compiling source code...
if not exist out mkdir out
set SOURCES=
for /r src %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" -d out %SOURCES%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  !! SOURCE COMPILATION FAILED !!
    pause
    exit /b 1
)
echo  Source compilation OK.
echo.

REM === Compile test code ===
echo [2/3] Compiling test classes...
if not exist test_out mkdir test_out
set TEST_SOURCES=
for /r test %%f in (*.java) do set TEST_SOURCES=!TEST_SOURCES! "%%f"
javac -encoding UTF-8 -cp "%CP%" -d test_out %TEST_SOURCES%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  !! TEST COMPILATION FAILED !!
    pause
    exit /b 1
)
echo  Test compilation OK.
echo.

REM === Run tests ===
echo [3/3] Running tests...
echo.
echo ============================================================
echo  Running: RouteFeeValidationTest (Black-Box)
echo ============================================================
java -cp "%CP%" org.junit.runner.JUnitCore test.RouteFeeValidationTest
echo.

echo ============================================================
echo  Running: FeeChallanControllerStateTest (White-Box)
echo ============================================================
java -cp "%CP%" org.junit.runner.JUnitCore test.FeeChallanControllerStateTest
echo.

echo ============================================================
echo  Running: FeeChallanDAOTest (White-Box + Integration)
echo  NOTE: Requires MySQL running on localhost with transport_db
echo ============================================================
java -cp "%CP%" org.junit.runner.JUnitCore dao.FeeChallanDAOTest
echo.

echo ============================================================
echo  ALL TEST SUITES EXECUTED
echo ============================================================
pause
