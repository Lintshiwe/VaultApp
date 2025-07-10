@echo off
echo Starting Secure Vault Application...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not found in PATH.
    echo Please install Java 17 or higher and try again.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not found in PATH.
    echo Please install Apache Maven and try again.
    pause
    exit /b 1
)

REM Build the application if JAR doesn't exist
if not exist "target\VaultApp-1.0.0.jar" (
    echo Building application...
    mvn clean package -q
    if %errorlevel% neq 0 (
        echo Error: Failed to build the application.
        pause
        exit /b 1
    )
)

REM Run the application
echo Starting Vault Application...
java -jar target\VaultApp-1.0.0.jar

if %errorlevel% neq 0 (
    echo Error: Failed to start the application.
    pause
)
