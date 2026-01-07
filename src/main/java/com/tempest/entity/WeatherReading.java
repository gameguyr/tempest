package com.tempest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a single weather reading from the station.
 */
@Entity
@Table(name = "weather_readings", indexes = {
    @Index(name = "idx_reading_timestamp", columnList = "timestamp"),
    @Index(name = "idx_station_id", columnList = "stationId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id")
    private String stationId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Temperature in Celsius
    private Double temperature;

    // Relative humidity percentage (0-100)
    private Double humidity;

    // Atmospheric pressure in hPa (hectopascals)
    private Double pressure;

    // Wind speed in km/h
    private Double windSpeed;

    // Wind direction in degrees (0-360)
    private Double windDirection;

    // Rainfall in mm
    private Double rainfall;

    // UV index
    private Double uvIndex;

    // Light level in lux
    private Double lightLevel;

    // Battery voltage of the station
    private Double batteryVoltage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

