package com.tempest.config;

import com.tempest.entity.WeatherReading;
import com.tempest.entity.WeatherStation;
import com.tempest.repository.WeatherReadingRepository;
import com.tempest.repository.WeatherStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Initializes sample data for development purposes.
 * Only runs in 'dev' profile or when no profile is set.
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final WeatherStationRepository stationRepository;
    private final WeatherReadingRepository readingRepository;

    @Override
    public void run(String... args) {
        if (stationRepository.count() == 0) {
            log.info("Initializing sample weather data...");
            initializeSampleData();
            log.info("Sample data initialization complete.");
        }
    }

    private void initializeSampleData() {
        // Create a sample station
        WeatherStation station = WeatherStation.builder()
                .stationId("station-01")
                .name("Backyard Weather Station")
                .location("Garden")
                .latitude(40.7128)
                .longitude(-74.0060)
                .altitude(10.0)
                .apiKey(UUID.randomUUID().toString())
                .isActive(true)
                .lastSeen(LocalDateTime.now())
                .build();
        
        stationRepository.save(station);
        log.info("Created sample station: {}", station.getName());

        // Generate sample readings for the last 24 hours
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 48; i++) { // One reading every 30 minutes
            LocalDateTime timestamp = now.minusMinutes(i * 30L);
            
            // Simulate realistic temperature variation (warmer during day)
            int hour = timestamp.getHour();
            double baseTemp = 18 + 8 * Math.sin((hour - 6) * Math.PI / 12);
            double temp = baseTemp + (random.nextDouble() - 0.5) * 3;
            
            WeatherReading reading = WeatherReading.builder()
                    .stationId("station-01")
                    .timestamp(timestamp)
                    .temperature(Math.round(temp * 10.0) / 10.0)
                    .humidity(50 + random.nextDouble() * 40)
                    .pressure(1010 + random.nextDouble() * 10)
                    .windSpeed(random.nextDouble() * 20)
                    .windDirection(random.nextDouble() * 360)
                    .rainfall(random.nextDouble() < 0.1 ? random.nextDouble() * 2 : 0)
                    .uvIndex(hour >= 6 && hour <= 18 ? random.nextDouble() * 8 : 0)
                    .lightLevel(hour >= 6 && hour <= 18 ? 10000 + random.nextDouble() * 50000 : random.nextDouble() * 100)
                    .batteryVoltage(3.5 + random.nextDouble() * 0.7)
                    .build();
            
            readingRepository.save(reading);
        }
        
        log.info("Created 48 sample weather readings");
    }
}

