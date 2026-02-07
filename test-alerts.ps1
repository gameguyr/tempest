# Weather Alert System - Automated Backend Test Script
# Run this after starting the application with: ./mvnw.cmd spring-boot:run

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Email = "jwlaprade@gmail.com"
)

$ErrorActionPreference = "Continue"

Write-Host "[STARTING] Weather Alert System Backend Tests" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "Test Email: $Email" -ForegroundColor Gray
Write-Host ""

# Test 1: Check if API is reachable
Write-Host "[TEST 1] Checking if alert API is available..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/alerts" -Method Get
    Write-Host "[SUCCESS] API is reachable" -ForegroundColor Green
    Write-Host "Current alerts: $($response.data.Count)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] API is not reachable. Is the application running?" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Create a temperature alert
Write-Host "[TEST 2] Creating high temperature alert..." -ForegroundColor Yellow
$alertBody = @{
    name = "Test High Temperature Alert"
    description = "Automated test - Temperature exceeds 85F"
    stationId = "fractal"
    metric = "TEMPERATURE"
    operator = "GREATER_THAN"
    threshold = 85.0
    userEmail = $Email
    notificationType = "EMAIL"
    cooldownMinutes = 5
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts" -Method Post `
        -ContentType "application/json" -Body $alertBody
    $alertId = $createResponse.data.id
    Write-Host "[SUCCESS] Alert created with ID: $alertId" -ForegroundColor Green
    Write-Host "Alert Name: $($createResponse.data.name)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to create alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 3: Verify alert was created
Write-Host "[TEST 3] Verifying alert creation..." -ForegroundColor Yellow
try {
    $getResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId" -Method Get
    if ($getResponse.data.id -eq $alertId) {
        Write-Host "[SUCCESS] Alert retrieved successfully" -ForegroundColor Green
        Write-Host "Enabled: $($getResponse.data.isEnabled)" -ForegroundColor Gray
        Write-Host "Threshold: $($getResponse.data.threshold) $($getResponse.data.metric)" -ForegroundColor Gray
    }
} catch {
    Write-Host "[ERROR] Failed to retrieve alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 4: Get alerts by user email
Write-Host "[TEST 4] Getting alerts for user: $Email..." -ForegroundColor Yellow
try {
    $userAlerts = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/user/$Email" -Method Get
    Write-Host "[SUCCESS] Found $($userAlerts.data.Count) alert(s) for this user" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to get user alerts" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 5: Trigger the alert with a weather reading
Write-Host "[TEST 5] Triggering alert with high temperature reading (90.5F)..." -ForegroundColor Yellow
$readingBody = @{
    stationId = "fractal"
    temperature = 90.5
    humidity = 65.0
    pressure = 1013.25
    windSpeed = 15.0
    rainfall = 0.0
    uvIndex = 5.0
    lightLevel = 50000.0
    batteryVoltage = 3.7
} | ConvertTo-Json

try {
    $readingResponse = Invoke-RestMethod -Uri "$BaseUrl/api/weather" -Method Post `
        -ContentType "application/json" -Body $readingBody
    Write-Host "[SUCCESS] Weather reading posted" -ForegroundColor Green
    Write-Host "Reading ID: $($readingResponse.data.id)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to post weather reading" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 6: Check alert history
Write-Host "[TEST 6] Checking alert history..." -ForegroundColor Yellow
Start-Sleep -Seconds 2  # Give it a moment to process
try {
    $historyResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId/history" -Method Get
    Write-Host "[SUCCESS] Retrieved alert history" -ForegroundColor Green
    Write-Host "Total triggers: $($historyResponse.data.totalElements)" -ForegroundColor Gray
    if ($historyResponse.data.totalElements -gt 0) {
        $lastTrigger = $historyResponse.data.content[0]
        Write-Host "Last triggered: $($lastTrigger.triggeredAt)" -ForegroundColor Gray
        Write-Host "Actual value: $($lastTrigger.actualValue) $($getResponse.data.metric)" -ForegroundColor Gray
    }
} catch {
    Write-Host "[ERROR] Failed to get alert history" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 7: Test cooldown period
Write-Host "[TEST 7] Testing cooldown period (should not trigger again)..." -ForegroundColor Yellow
$readingBody2 = @{
    stationId = "fractal"
    temperature = 88.0
    humidity = 65.0
    pressure = 1013.25
    windSpeed = 15.0
    rainfall = 0.0
    uvIndex = 5.0
    lightLevel = 50000.0
    batteryVoltage = 3.7
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/weather" -Method Post `
        -ContentType "application/json" -Body $readingBody2 | Out-Null
    Start-Sleep -Seconds 2
    $historyCheck = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId/history" -Method Get
    if ($historyCheck.data.totalElements -eq 1) {
        Write-Host "[SUCCESS] Cooldown period working - no duplicate trigger" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Expected 1 trigger, found $($historyCheck.data.totalElements)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Failed cooldown test" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 8: Toggle alert (disable)
Write-Host "[TEST 8] Disabling alert..." -ForegroundColor Yellow
try {
    $toggleResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId/toggle?enabled=false" -Method Post
    Write-Host "[SUCCESS] Alert disabled" -ForegroundColor Green
    Write-Host "Enabled status: $($toggleResponse.data.isEnabled)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to disable alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 9: Verify disabled alert doesn't trigger
Write-Host "[TEST 9] Posting reading while alert disabled (should not trigger)..." -ForegroundColor Yellow
$readingBody3 = @{
    stationId = "fractal"
    temperature = 95.0
    humidity = 65.0
    pressure = 1013.25
    windSpeed = 15.0
    rainfall = 0.0
    uvIndex = 5.0
    lightLevel = 50000.0
    batteryVoltage = 3.7
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/weather" -Method Post `
        -ContentType "application/json" -Body $readingBody3 | Out-Null
    Start-Sleep -Seconds 2
    $historyCheck2 = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId/history" -Method Get
    if ($historyCheck2.data.totalElements -eq 1) {
        Write-Host "[SUCCESS] Disabled alert did not trigger" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Expected 1 trigger, found $($historyCheck2.data.totalElements)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Failed disabled alert test" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 10: Re-enable alert
Write-Host "[TEST 10] Re-enabling alert..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId/toggle?enabled=true" -Method Post | Out-Null
    Write-Host "[SUCCESS] Alert re-enabled" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to re-enable alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 11: Update alert
Write-Host "[TEST 11] Updating alert threshold..." -ForegroundColor Yellow
$updateBody = @{
    name = "Test High Temperature Alert (Updated)"
    description = "Updated threshold to 80F"
    stationId = "fractal"
    metric = "TEMPERATURE"
    operator = "GREATER_THAN"
    threshold = 80.0
    userEmail = $Email
    notificationType = "EMAIL"
    cooldownMinutes = 5
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId" -Method Put `
        -ContentType "application/json" -Body $updateBody
    Write-Host "[SUCCESS] Alert updated" -ForegroundColor Green
    Write-Host "New threshold: $($updateResponse.data.threshold)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to update alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 12: Create multi-station alert
Write-Host "[TEST 12] Creating multi-station alert (all stations)..." -ForegroundColor Yellow
$multiStationBody = @{
    name = "Low Humidity Alert (All Stations)"
    description = "Alert when humidity drops below 30%"
    metric = "HUMIDITY"
    operator = "LESS_THAN"
    threshold = 30.0
    userEmail = $Email
    notificationType = "EMAIL"
    cooldownMinutes = 5
} | ConvertTo-Json

try {
    $multiResponse = Invoke-RestMethod -Uri "$BaseUrl/api/alerts" -Method Post `
        -ContentType "application/json" -Body $multiStationBody
    $multiAlertId = $multiResponse.data.id
    Write-Host "[SUCCESS] Multi-station alert created with ID: $multiAlertId" -ForegroundColor Green
    Write-Host "Applies to: All Stations" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to create multi-station alert" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Test 13: Get recent history across all alerts
Write-Host "[TEST 13] Getting recent alert history..." -ForegroundColor Yellow
try {
    $recentHistory = Invoke-RestMethod -Uri "$BaseUrl/api/alerts/history/recent" -Method Get
    Write-Host "[SUCCESS] Retrieved recent history" -ForegroundColor Green
    Write-Host "Total recent triggers: $($recentHistory.data.Count)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] Failed to get recent history" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

# Cleanup option
Write-Host "[CLEANUP] Do you want to delete the test alerts? (Y/N)" -ForegroundColor Cyan
$cleanup = Read-Host
if ($cleanup -eq "Y" -or $cleanup -eq "y") {
    Write-Host "Deleting test alerts..." -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$alertId" -Method Delete | Out-Null
        Write-Host "[SUCCESS] Deleted alert ID: $alertId" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Failed to delete alert $alertId" -ForegroundColor Red
    }

    if ($multiAlertId) {
        try {
            Invoke-RestMethod -Uri "$BaseUrl/api/alerts/$multiAlertId" -Method Delete | Out-Null
            Write-Host "[SUCCESS] Deleted alert ID: $multiAlertId" -ForegroundColor Green
        } catch {
            Write-Host "[ERROR] Failed to delete alert $multiAlertId" -ForegroundColor Red
        }
    }
} else {
    Write-Host "[SKIP] Test alerts retained for manual inspection" -ForegroundColor Gray
    Write-Host "Alert IDs: $alertId, $multiAlertId" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[COMPLETE] All tests finished!" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Check H2 console: http://localhost:8080/h2-console" -ForegroundColor Gray
Write-Host "2. View alert history in database: SELECT * FROM ALERT_HISTORY;" -ForegroundColor Gray
Write-Host "3. Configure SMTP to test email notifications (see ALERT_TESTING_GUIDE.md)" -ForegroundColor Gray
Write-Host "4. Configure Twilio to test SMS notifications (see ALERT_TESTING_GUIDE.md)" -ForegroundColor Gray
