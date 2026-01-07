package com.tempest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for incoming weather readings from the station.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherReadingDTO {

    @JsonProperty("station_id")
    private String stationId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Temperature in Celsius
    @JsonProperty("temp")
    private Double temperature;

    // Relative humidity percentage (0-100)
    @JsonProperty("humidity")
    private Double humidity;

    // Atmospheric pressure in hPa
    @JsonProperty("pressure")
    private Double pressure;

    // Wind speed in km/h
    @JsonProperty("wind_speed")
    private Double windSpeed;

    // Wind direction in degrees (0-360)
    @JsonProperty("wind_dir")
    private Double windDirection;

    // Rainfall in mm
    @JsonProperty("rain")
    private Double rainfall;

    // UV index
    @JsonProperty("uv")
    private Double uvIndex;

    // Light level in lux
    @JsonProperty("light")
    private Double lightLevel;

    // Battery voltage
    @JsonProperty("battery")
    private Double batteryVoltage;
}

