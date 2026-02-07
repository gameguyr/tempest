# Weather Alert System - Backend Testing Guide

## Prerequisites

1. **Start the application**:
   ```bash
   ./mvnw.cmd spring-boot:run
   ```
   Or if using your IDE, run `TempestApplication.java`

2. **Verify the application started**:
   - Check console for: "Started TempestApplication"
   - Default port: http://localhost:8080

## Testing Steps

### 1. Verify Alert Endpoints Are Available

```bash
curl http://localhost:8080/api/alerts
```

Expected: Empty array `{"success":true,"message":"Success","data":[]}`

### 2. Create a Test Alert

**Temperature Alert for fractal station:**

```bash
curl -X POST http://localhost:8080/api/alerts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"High Temperature Alert\",\"description\":\"Alert when temperature exceeds 85F\",\"stationId\":\"fractal\",\"metric\":\"TEMPERATURE\",\"operator\":\"GREATER_THAN\",\"threshold\":85.0,\"userEmail\":\"jwlaprade@gmail.com\",\"notificationType\":\"EMAIL\",\"cooldownMinutes\":5}"
```

Expected: `{"success":true,"message":"Alert created successfully","data":{...}}`

**Note the `id` field in the response - you'll need it for other tests**

### 3. Verify Alert Was Created

```bash
curl http://localhost:8080/api/alerts
```

Expected: Array containing your alert

### 4. Get Alerts by User Email

```bash
curl http://localhost:8080/api/alerts/user/jwlaprade@gmail.com
```

Expected: Your alert(s) for this email

### 5. Trigger the Alert with a Weather Reading

**Post a reading that exceeds the threshold (temp > 85):**

```bash
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"fractal\",\"temperature\":90.5,\"humidity\":65.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"
```

Expected: Reading saved successfully

### 6. Check Alert History

Replace `{id}` with your alert ID from step 2:

```bash
curl http://localhost:8080/api/alerts/{id}/history
```

Expected: Paginated history showing the triggered alert

**Or get recent history across all alerts:**

```bash
curl http://localhost:8080/api/alerts/history/recent
```

### 7. Test Cooldown Period

**Immediately post another reading that meets criteria:**

```bash
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"fractal\",\"temperature\":88.0,\"humidity\":65.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"
```

**Check history again** - should still only show 1 trigger (cooldown active)

**Wait 5+ minutes, then post another reading:**

```bash
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"fractal\",\"temperature\":92.0,\"humidity\":65.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"
```

**Check history** - should now show 2 triggers

### 8. Test Alert Toggle (Enable/Disable)

**Disable the alert:**

```bash
curl -X POST "http://localhost:8080/api/alerts/{id}/toggle?enabled=false"
```

Expected: `{"success":true,"message":"Alert disabled","data":{...}}`

**Post a reading that would trigger it:**

```bash
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"fractal\",\"temperature\":95.0,\"humidity\":65.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"
```

**Check history** - should NOT have a new trigger (alert disabled)

**Re-enable the alert:**

```bash
curl -X POST "http://localhost:8080/api/alerts/{id}/toggle?enabled=true"
```

### 9. Test Update Alert

**Change threshold from 85 to 80:**

```bash
curl -X PUT http://localhost:8080/api/alerts/{id} ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"High Temperature Alert\",\"description\":\"Alert when temperature exceeds 80F\",\"stationId\":\"fractal\",\"metric\":\"TEMPERATURE\",\"operator\":\"GREATER_THAN\",\"threshold\":80.0,\"userEmail\":\"jwlaprade@gmail.com\",\"notificationType\":\"EMAIL\",\"cooldownMinutes\":5}"
```

Expected: `{"success":true,"message":"Alert updated successfully","data":{...}}`

### 10. Test Multi-Station Alert

**Create an alert for ALL stations (null stationId):**

```bash
curl -X POST http://localhost:8080/api/alerts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Low Humidity Alert\",\"description\":\"Alert when humidity drops below 30%%\",\"metric\":\"HUMIDITY\",\"operator\":\"LESS_THAN\",\"threshold\":30.0,\"userEmail\":\"jwlaprade@gmail.com\",\"notificationType\":\"EMAIL\",\"cooldownMinutes\":5}"
```

**Post readings from both stations:**

```bash
# fractal station
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"fractal\",\"temperature\":75.0,\"humidity\":25.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"

# RussMonsta station
curl -X POST http://localhost:8080/api/weather ^
  -H "Content-Type: application/json" ^
  -d "{\"stationId\":\"RussMonsta\",\"temperature\":75.0,\"humidity\":25.0,\"pressure\":1013.25,\"windSpeed\":15.0,\"rainfall\":0.0,\"uvIndex\":5.0,\"lightLevel\":50000.0,\"batteryVoltage\":3.7}"
```

**Check history** - should show triggers from both stations

### 11. Test Different Operators and Metrics

**Wind Speed Alert (LESS_THAN):**

```bash
curl -X POST http://localhost:8080/api/alerts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Calm Wind Alert\",\"description\":\"Alert when wind speed below 5 km/h\",\"stationId\":\"fractal\",\"metric\":\"WIND_SPEED\",\"operator\":\"LESS_THAN\",\"threshold\":5.0,\"userEmail\":\"jwlaprade@gmail.com\",\"notificationType\":\"EMAIL\",\"cooldownMinutes\":5}"
```

**Pressure Alert (EQUALS - within 0.01 tolerance):**

```bash
curl -X POST http://localhost:8080/api/alerts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Standard Pressure Alert\",\"description\":\"Alert at standard pressure\",\"stationId\":\"fractal\",\"metric\":\"PRESSURE\",\"operator\":\"EQUALS\",\"threshold\":1013.25,\"userEmail\":\"jwlaprade@gmail.com\",\"notificationType\":\"EMAIL\",\"cooldownMinutes\":5}"
```

### 12. Test Alert Deletion

```bash
curl -X DELETE http://localhost:8080/api/alerts/{id}
```

Expected: `{"success":true,"message":"Alert deleted successfully","data":null}`

**Verify deletion:**

```bash
curl http://localhost:8080/api/alerts/{id}
```

Expected: 404 Not Found with error message

## Testing Email Notifications

To test email delivery, you need to configure SMTP settings:

1. **Set environment variables** before starting the app:

```bash
# For Gmail (requires App Password, not your regular password)
set SMTP_HOST=smtp.gmail.com
set SMTP_PORT=587
set SMTP_USERNAME=jwlaprade@gmail.com
set SMTP_PASSWORD=your-app-password-here
set NOTIFICATION_FROM_EMAIL=jwlaprade@gmail.com
set NOTIFICATION_FROM_NAME=Tempest Weather Alerts
```

2. **Create a Gmail App Password**:
   - Go to: https://myaccount.google.com/apppasswords
   - Create password for "Mail"
   - Use that password (not your regular Gmail password)

3. **Restart the application** with these environment variables set

4. **Trigger an alert** - you should receive an email

## Testing SMS Notifications

To test SMS delivery via Twilio:

1. **Sign up for Twilio**: https://www.twilio.com/try-twilio

2. **Get credentials**:
   - Account SID
   - Auth Token
   - Phone Number (from Twilio)

3. **Set environment variables**:

```bash
set TWILIO_ENABLED=true
set TWILIO_ACCOUNT_SID=your-account-sid
set TWILIO_AUTH_TOKEN=your-auth-token
set TWILIO_FROM_NUMBER=+1234567890
```

4. **Create alert with SMS notification**:

```bash
curl -X POST http://localhost:8080/api/alerts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"SMS Test Alert\",\"description\":\"Test SMS\",\"stationId\":\"fractal\",\"metric\":\"TEMPERATURE\",\"operator\":\"GREATER_THAN\",\"threshold\":85.0,\"userPhone\":\"+1234567890\",\"notificationType\":\"SMS\",\"cooldownMinutes\":5}"
```

5. **Trigger the alert** - you should receive an SMS

## Testing Scheduled Alert Checking

The scheduler runs every 5 minutes automatically. To test:

1. **Enable debug logging** in `application.yml`:

```yaml
logging:
  level:
    com.tempest.service.AlertSchedulerService: DEBUG
```

2. **Restart the application**

3. **Watch the logs** - every 5 minutes you should see:
   - "Starting scheduled alert check"
   - "Checking alerts for X stations with recent readings"
   - "Alert check completed successfully"

## Checking the H2 Database

1. **Access H2 Console**: http://localhost:8080/h2-console

2. **Login**:
   - JDBC URL: `jdbc:h2:mem:tempestdb`
   - Username: `sa`
   - Password: (leave empty)

3. **Query tables**:

```sql
-- View all alerts
SELECT * FROM WEATHER_ALERTS;

-- View alert history
SELECT * FROM ALERT_HISTORY;

-- View recent triggers with alert names
SELECT ah.*, wa.name AS alert_name
FROM ALERT_HISTORY ah
JOIN WEATHER_ALERTS wa ON ah.alert_id = wa.id
ORDER BY ah.triggered_at DESC;
```

## Troubleshooting

**Application won't start:**
- Check Java is installed: `java -version`
- Check for port conflicts (8080 already in use)
- Review console output for errors

**Alerts not triggering:**
- Verify alert is enabled: `isEnabled=true`
- Check cooldown period hasn't elapsed
- Verify threshold value is correct
- Check application logs for errors

**Email not sending:**
- Verify SMTP credentials are correct
- Check spam folder
- Look for errors in application logs
- Test SMTP connection manually

**SMS not sending:**
- Verify Twilio is enabled: `TWILIO_ENABLED=true`
- Check Twilio credentials
- Verify phone number format (+1234567890)
- Check Twilio console for delivery status
- Look for errors in application logs

**Scheduled checks not running:**
- Verify @EnableScheduling is present in TempestApplication
- Check debug logs for scheduler messages
- Verify recent readings exist (posted within last 10 minutes)

## Expected Log Output

When an alert triggers successfully, you should see:

```
INFO  c.t.service.AlertEvaluationService - Alert 'High Temperature Alert' triggered for station fractal: 90.5°F > 85.0°F
INFO  c.t.service.NotificationService - Email sent successfully to jwlaprade@gmail.com
```

When cooldown prevents triggering:

```
DEBUG c.t.service.AlertEvaluationService - Alert 'High Temperature Alert' is in cooldown period, skipping
```

## Success Criteria

✅ All REST endpoints respond correctly
✅ Alerts can be created, updated, deleted, and toggled
✅ Alerts trigger when conditions are met
✅ Alert history is recorded in database
✅ Cooldown period prevents spam
✅ Multi-station alerts work correctly
✅ Different operators (>, <, =, >=, <=) work
✅ Different metrics (temperature, humidity, etc.) work
✅ Email notifications are sent (if configured)
✅ SMS notifications are sent (if configured)
✅ Scheduled checking runs every 5 minutes
✅ Old history is cleaned up daily
