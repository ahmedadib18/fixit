# PostgreSQL Setup Script
# This script will help you create the database and set environment variables

Write-Host "=================================="
Write-Host "PostgreSQL Setup for FixIt"
Write-Host "=================================="
Write-Host ""

# Add PostgreSQL to PATH for this session
$env:Path += ";C:\Program Files\PostgreSQL\17\bin"

Write-Host "Step 1: Testing PostgreSQL connection..."
Write-Host "You will be prompted for the postgres user password."
Write-Host ""

# Test connection
$testConnection = Read-Host "Do you want to test the connection? (y/n)"
if ($testConnection -eq "y") {
    Write-Host "Connecting to PostgreSQL..."
    Write-Host "Command: psql -U postgres -c 'SELECT version();'"
    Write-Host ""
    & psql -U postgres -c "SELECT version();"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Connection successful!"
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "❌ Connection failed. Please check your password."
        exit 1
    }
}

Write-Host "Step 2: Creating database 'fixitdb'..."
Write-Host ""

$createDb = Read-Host "Create database fixitdb? (y/n)"
if ($createDb -eq "y") {
    & psql -U postgres -c "CREATE DATABASE fixitdb;"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Database 'fixitdb' created successfully!"
    } else {
        Write-Host "⚠️  Database might already exist or creation failed."
        Write-Host "Checking if database exists..."
        & psql -U postgres -c "\l" | Select-String "fixitdb"
    }
}

Write-Host ""
Write-Host "Step 3: Verifying database..."
& psql -U postgres -d fixitdb -c "SELECT current_database();"

Write-Host ""
Write-Host "=================================="
Write-Host "Step 4: Setting Environment Variables"
Write-Host "=================================="
Write-Host ""
Write-Host "You need to set these environment variables:"
Write-Host "  DB_USERNAME=postgres"
Write-Host "  DB_PASSWORD=your_postgres_password"
Write-Host ""

$setEnvVars = Read-Host "Set environment variables for this session? (y/n)"
if ($setEnvVars -eq "y") {
    $dbPassword = Read-Host "Enter your PostgreSQL password" -AsSecureString
    $dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword))
    
    $env:DB_USERNAME = "postgres"
    $env:DB_PASSWORD = $dbPasswordPlain
    
    Write-Host ""
    Write-Host "✅ Environment variables set for this session!"
    Write-Host "   DB_USERNAME = postgres"
    Write-Host "   DB_PASSWORD = ********"
    Write-Host ""
    Write-Host "⚠️  NOTE: These are session variables. They will be lost when you close this terminal."
    Write-Host ""
}

Write-Host "=================================="
Write-Host "Setup Complete!"
Write-Host "=================================="
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Update application.properties: spring.profiles.active=prod"
Write-Host "2. Run: mvn clean install"
Write-Host "3. Run: mvn spring-boot:run"
Write-Host ""
Write-Host "Important URLs:"
Write-Host "  Backend: http://localhost:8080"
Write-Host "  Frontend: http://localhost:5173"
Write-Host "  Admin login: admin@fixit.com / admin123"
Write-Host ""
