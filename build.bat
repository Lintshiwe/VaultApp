@echo off
echo Building Secure Vault Application...
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not found in PATH.
    echo Please install Apache Maven and try again.
    pause
    exit /b 1
)

REM Clean and build
echo Cleaning previous build...
mvn clean

echo Building application...
mvn package

if %errorlevel% equ 0 (
    echo.
    echo Build completed successfully!
    echo You can now run the application using run.bat or:
    echo java -jar target\VaultApp-1.0.0.jar
) else (
    echo.
    echo Build failed! Please check the error messages above.
)

pause
