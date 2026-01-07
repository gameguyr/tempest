package com.tempest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for aggregated weather statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherStatsDTO {

    private Double minTemperature;
    private Double maxTemperature;
    private Double avgTemperature;
    private Double avgHumidity;
    private Double avgPressure;
    private Double totalRainfall;
    private Double maxWindSpeed;
    private Integer readingCount;
    private Integer periodHours;

    public static WeatherStatsDTO empty() {
        return WeatherStatsDTO.builder()
                .minTemperature(0.0)
                .maxTemperature(0.0)
                .avgTemperature(0.0)
                .avgHumidity(0.0)
                .avgPressure(0.0)
                .totalRainfall(0.0)
                .maxWindSpeed(0.0)
                .readingCount(0)
                .periodHours(0)
                .build();
    }
}

