# Secure Vault Application Launcher
Write-Host "Starting Secure Vault Application..." -ForegroundColor Green
Write-Host ""

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    Write-Host "Java found: $($javaVersion[0])" -ForegroundColor Yellow
} catch {
    Write-Host "Error: Java is not installed or not found in PATH." -ForegroundColor Red
    Write-Host "Please install Java 17 or higher and try again." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if Maven is installed
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-Host "Maven found: $mavenVersion" -ForegroundColor Yellow
} catch {
    Write-Host "Error: Maven is not installed or not found in PATH." -ForegroundColor Red
    Write-Host "Please install Apache Maven and try again." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Build the application if JAR doesn't exist
if (-not (Test-Path "target\VaultApp-1.0.0.jar")) {
    Write-Host "Building application..." -ForegroundColor Yellow
    mvn clean package -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to build the application." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host "Build completed successfully." -ForegroundColor Green
}

# Run the application
Write-Host "Starting Vault Application..." -ForegroundColor Green
Write-Host ""
Write-Host "Default login credentials:" -ForegroundColor Cyan
Write-Host "Username: admin" -ForegroundColor White
Write-Host "Password: admin123" -ForegroundColor White
Write-Host ""

java -jar target\VaultApp-1.0.0.jar

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Failed to start the application." -ForegroundColor Red
    Read-Host "Press Enter to exit"
}
