# AttendAI Cloud - Quick Run Script
# This script will start both the backend and frontend.

# --- 1. Load AWS Credentials ---
# Create a file named 'aws_credentials.env' with your keys if you want to skip manual input.
if (Test-Path "aws_credentials.env") {
    Get-Content "aws_credentials.env" | ForEach-Object {
        if ($_ -match "^(?<name>[^=]+)=(?<value>.*)$") {
            [System.Environment]::SetEnvironmentVariable($Matches.name, $Matches.value, "Process")
        }
    }
} else {
    Write-Host "--- AWS Credentials Required ---" -ForegroundColor Cyan
    Write-Host "Note: Provide fake credentials if you just want to see the UI compile." -ForegroundColor DarkGray
    $env:AWS_ACCESS_KEY_ID = Read-Host "Enter AWS Access Key ID"
    $env:AWS_SECRET_ACCESS_KEY = Read-Host "Enter AWS Secret Access Key"
    $env:AWS_REGION = Read-Host "Enter AWS Region (e.g., us-east-1)"
    Write-Host "Credentials set for this session.`n" -ForegroundColor Green
}

# --- 2. Check for Local Maven ---
$MAVEN_PATH = (Resolve-Path ".\maven\apache-maven-3.9.6\bin\mvn.cmd").Path

# --- 3. Start Backend ---
Write-Host "Starting Backend (Spring Boot)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; & '$MAVEN_PATH' spring-boot:run" -WindowStyle Normal

# --- 4. Start Frontend ---
Write-Host "Starting Frontend (React/Vite)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev" -WindowStyle Normal

Write-Host "`nAttendAI Cloud is starting up!" -ForegroundColor Green
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend:  http://localhost:8080"
Write-Host "`nKeep the separate terminal windows open to see logs."
