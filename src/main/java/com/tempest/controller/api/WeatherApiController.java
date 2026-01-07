package com.tempest.controller.api;

import com.tempest.dto.ApiResponse;
import com.tempest.dto.WeatherReadingDTO;
import com.tempest.dto.WeatherStatsDTO;
import com.tempest.entity.WeatherReading;
import com.tempest.service.WeatherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for weather data operations.
 * This endpoint receives data from the weather station.
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WeatherApiController {

    private final WeatherService weatherService;

    /**
     * POST endpoint to receive weather readings from the station.
     * Example payload:
     * {
     *   "station_id": "station-01",
     *   "temp": 22.5,
     *   "humidity": 65.0,
     *   "pressure": 1013.25,
     *   "wind_speed": 12.5,
     *   "wind_dir": 180,
     *   "rain": 0.0,
     *   "uv": 3.5,
     *   "light": 45000,
     *   "battery": 3.7
     * }
     */
    @PostMapping("/reading")
    public ResponseEntity<ApiResponse<WeatherReading>> postReading(
            @Valid @RequestBody WeatherReadingDTO reading) {
        log.info("Received weather reading from station: {}", reading.getStationId());
        
        try {
            WeatherReading saved = weatherService.recordReading(reading);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Reading recorded successfully", saved));
        } catch (Exception e) {
            log.error("Error recording reading", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to record reading: " + e.getMessage()));
        }
    }

    /**
     * GET the latest weather reading.
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<WeatherReading>> getLatestReading() {
        return weatherService.getLatestReading()
                .map(reading -> ResponseEntity.ok(ApiResponse.success(reading)))
                .orElse(ResponseEntity.ok(ApiResponse.error("No readings available")));
    }

    /**
     * GET the latest reading for a specific station.
     */
    @GetMapping("/latest/{stationId}")
    public ResponseEntity<ApiResponse<WeatherReading>> getLatestReadingForStation(
            @PathVariable String stationId) {
        return weatherService.getLatestReadingForStation(stationId)
                .map(reading -> ResponseEntity.ok(ApiResponse.success(reading)))
                .orElse(ResponseEntity.ok(ApiResponse.error("No readings for station: " + stationId)));
    }

    /**
     * GET readings for the last N hours.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<WeatherReading>>> getHistory(
            @RequestParam(defaultValue = "24") int hours) {
        List<WeatherReading> readings = weatherService.getReadingsForLastHours(hours);
        return ResponseEntity.ok(ApiResponse.success(readings));
    }

    /**
     * GET readings for a station for the last N hours.
     */
    @GetMapping("/history/{stationId}")
    public ResponseEntity<ApiResponse<List<WeatherReading>>> getStationHistory(
            @PathVariable String stationId,
            @RequestParam(defaultValue = "24") int hours) {
        List<WeatherReading> readings = weatherService.getReadingsForStation(stationId, hours);
        return ResponseEntity.ok(ApiResponse.success(readings));
    }

    /**
     * GET weather statistics for the last N hours.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<WeatherStatsDTO>> getStats(
            @RequestParam(defaultValue = "24") int hours) {
        WeatherStatsDTO stats = weatherService.getStats(hours);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Simple health check endpoint for the station to verify connectivity.
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}

