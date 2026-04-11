@echo off
REM PostgreSQL Database Creation Script
REM Run this to create the fixitdb database

echo ==================================
echo Creating FixIt Database
echo ==================================
echo.

REM Add PostgreSQL to PATH
set PATH=%PATH%;C:\Program Files\PostgreSQL\17\bin

echo Step 1: Testing PostgreSQL connection...
echo You will be prompted for the postgres user password.
echo.

psql -U postgres -c "SELECT version();"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Could not connect to PostgreSQL
    echo Please check:
    echo   1. PostgreSQL service is running
    echo   2. Password is correct
    pause
    exit /b 1
)

echo.
echo SUCCESS: Connected to PostgreSQL!
echo.

echo Step 2: Creating database 'fixitdb'...
psql -U postgres -c "CREATE DATABASE fixitdb;"

echo.
echo Step 3: Verifying database exists...
psql -U postgres -c "\l" | findstr fixitdb

echo.
echo Step 4: Testing connection to fixitdb...
psql -U postgres -d fixitdb -c "SELECT current_database();"

echo.
echo ==================================
echo Database Setup Complete!
echo ==================================
echo.
echo Next steps:
echo 1. Set environment variables (see below)
echo 2. Update application.properties
echo 3. Run: mvn spring-boot:run
echo.
echo Environment Variables (set these):
echo   DB_USERNAME=postgres
echo   DB_PASSWORD=your_postgres_password
echo.
echo To set for current session:
echo   set DB_USERNAME=postgres
echo   set DB_PASSWORD=your_password
echo.
pause
