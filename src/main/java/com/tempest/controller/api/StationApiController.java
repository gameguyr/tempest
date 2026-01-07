package com.tempest.controller.api;

import com.tempest.dto.ApiResponse;
import com.tempest.entity.WeatherStation;
import com.tempest.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API controller for weather station management.
 */
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@Slf4j
public class StationApiController {

    private final WeatherService weatherService;

    /**
     * GET all registered stations.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeatherStation>>> getAllStations() {
        List<WeatherStation> stations = weatherService.getAllStations();
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * GET all active stations.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WeatherStation>>> getActiveStations() {
        List<WeatherStation> stations = weatherService.getActiveStations();
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * GET a specific station.
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<ApiResponse<WeatherStation>> getStation(@PathVariable String stationId) {
        return weatherService.getStation(stationId)
                .map(station -> ResponseEntity.ok(ApiResponse.success(station)))
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Station not found: " + stationId)));
    }

    /**
     * POST to register a new station.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeatherStation>> createStation(
            @RequestBody WeatherStation station) {
        log.info("Registering new station: {}", station.getName());
        
        // Generate API key if not provided
        if (station.getApiKey() == null || station.getApiKey().isEmpty()) {
            station.setApiKey(UUID.randomUUID().toString());
        }
        
        WeatherStation saved = weatherService.createOrUpdateStation(station);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Station registered successfully", saved));
    }

    /**
     * PUT to update a station.
     */
    @PutMapping("/{stationId}")
    public ResponseEntity<ApiResponse<WeatherStation>> updateStation(
            @PathVariable String stationId,
            @RequestBody WeatherStation station) {
        
        return weatherService.getStation(stationId)
                .map(existing -> {
                    existing.setName(station.getName());
                    existing.setLocation(station.getLocation());
                    existing.setLatitude(station.getLatitude());
                    existing.setLongitude(station.getLongitude());
                    existing.setAltitude(station.getAltitude());
                    existing.setIsActive(station.getIsActive());
                    
                    WeatherStation updated = weatherService.createOrUpdateStation(existing);
                    return ResponseEntity.ok(ApiResponse.success("Station updated", updated));
                })
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Station not found: " + stationId)));
    }
}

