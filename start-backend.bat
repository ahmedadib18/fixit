@echo off
REM FixIt Backend Startup Script
REM This sets environment variables and starts the backend

echo ==================================
echo Starting FixIt Backend
echo ==================================
echo.

REM Set environment variables for this session
echo Setting environment variables...
set DB_USERNAME=postgres
set /p DB_PASSWORD="Enter your PostgreSQL password: "

echo.
echo Environment variables set:
echo   DB_USERNAME = %DB_USERNAME%
echo   DB_PASSWORD = ********
echo.

REM Add PostgreSQL to PATH
set PATH=%PATH%;C:\Program Files\PostgreSQL\17\bin

echo Building application...
call mvn clean install

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Starting backend...
echo Backend will be available at: http://localhost:8080
echo Admin login: admin@fixit.com / admin123
echo.
echo Press Ctrl+C to stop the server
echo.

call mvn spring-boot:run

pause
