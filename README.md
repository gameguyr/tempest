# 🌪️ Tempest

A modern weather station application built with Spring Boot. Tempest collects weather data from homemade weather stations and displays it in a beautiful, responsive dashboard.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![License](https://img.shields.io/badge/License-MIT-blue)

## Features

- **📊 Real-time Dashboard** - Beautiful, responsive UI showing current conditions and historical data
- **📡 REST API** - Simple API for your weather station to POST readings
- **📈 Historical Data** - Charts and tables showing temperature, humidity, pressure trends
- **🔌 Multi-Station Support** - Connect multiple weather stations
- **💾 Persistent Storage** - H2 for development, PostgreSQL for production

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+

### Run the Application

```bash
# Clone the repository
git clone <your-repo-url>
cd tempest

# Build and run
./mvnw spring-boot:run
```

The application will start at **http://localhost:8080**

### Sample Data

In development mode, the app automatically creates:
- A sample weather station
- 48 sample readings (last 24 hours)

## API Reference

### Post a Weather Reading

```bash
curl -X POST http://localhost:8080/api/weather/reading \
  -H "Content-Type: application/json" \
  -d '{
    "station_id": "station-01",
    "temp": 22.5,
    "humidity": 65.0,
    "pressure": 1013.25,
    "wind_speed": 12.5,
    "wind_dir": 180,
    "rain": 0.0,
    "uv": 3.5,
    "light": 45000,
    "battery": 3.7
  }'
```

### Get Latest Reading

```bash
curl http://localhost:8080/api/weather/latest
```

### Get Historical Data

```bash
# Last 24 hours (default)
curl http://localhost:8080/api/weather/history

# Last 48 hours
curl http://localhost:8080/api/weather/history?hours=48
```

### Get Statistics

```bash
# 24-hour stats
curl http://localhost:8080/api/weather/stats

# 7-day stats
curl http://localhost:8080/api/weather/stats?hours=168
```

### Register a Station

```bash
curl -X POST http://localhost:8080/api/stations \
  -H "Content-Type: application/json" \
  -d '{
    "stationId": "my-station",
    "name": "Rooftop Station",
    "location": "Building A",
    "latitude": 40.7128,
    "longitude": -74.0060
  }'
```

## Configuration

### Development (Default)

Uses H2 in-memory database. Data persists in `./data/tempest`.

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/tempest`
- Username: `sa`
- Password: (empty)

### Production

Set environment variables and use the `prod` profile:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=tempest
export DB_USERNAME=tempest
export DB_PASSWORD=your-secure-password

java -jar tempest.jar --spring.profiles.active=prod
```

## Sensor Data Format

Your weather station should POST JSON with these fields:

| Field | Type | Unit | Description |
|-------|------|------|-------------|
| `station_id` | string | - | Unique station identifier |
| `timestamp` | string | ISO 8601 | Reading timestamp (optional) |
| `temp` | number | °C | Temperature |
| `humidity` | number | % | Relative humidity (0-100) |
| `pressure` | number | hPa | Atmospheric pressure |
| `wind_speed` | number | km/h | Wind speed |
| `wind_dir` | number | degrees | Wind direction (0-360) |
| `rain` | number | mm | Rainfall |
| `uv` | number | index | UV index |
| `light` | number | lux | Light level |
| `battery` | number | V | Battery voltage |

All fields except `station_id` are optional.

## Building for Production

```bash
# Build the JAR
./mvnw clean package -DskipTests

# The JAR will be at target/tempest-0.0.1-SNAPSHOT.jar
java -jar target/tempest-0.0.1-SNAPSHOT.jar
```

## Project Structure

```
src/
├── main/
│   ├── java/com/tempest/
│   │   ├── TempestApplication.java    # Main entry point
│   │   ├── config/                    # Configuration classes
│   │   ├── controller/
│   │   │   ├── api/                   # REST API controllers
│   │   │   └── web/                   # Web UI controllers
│   │   ├── dto/                       # Data transfer objects
│   │   ├── entity/                    # JPA entities
│   │   ├── repository/                # Data repositories
│   │   └── service/                   # Business logic
│   └── resources/
│       ├── application.yml            # Configuration
│       ├── static/css/                # Stylesheets
│       └── templates/                 # Thymeleaf templates
```

## Roadmap

- [ ] User authentication
- [ ] Alert notifications
- [ ] Data export (CSV, JSON)
- [ ] Weather forecasting integration
- [ ] Mobile-responsive improvements
- [ ] MQTT support for real-time updates

## License

MIT License - feel free to use this for your own weather station projects!

---

Built with ☕ and 🌧️

