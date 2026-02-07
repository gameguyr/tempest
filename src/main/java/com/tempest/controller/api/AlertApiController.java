package com.tempest.controller.api;

import com.tempest.dto.ApiResponse;
import com.tempest.entity.AlertHistory;
import com.tempest.entity.WeatherAlert;
import com.tempest.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for managing weather alerts.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlertApiController {

    private final AlertService alertService;

    /**
     * GET all alerts.
     *
     * @return list of all alerts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeatherAlert>>> getAllAlerts() {
        List<WeatherAlert> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * GET alerts by user email.
     *
     * @param email the user's email address
     * @return list of alerts for the user
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<List<WeatherAlert>>> getAlertsByUser(@PathVariable String email) {
        List<WeatherAlert> alerts = alertService.getAlertsByUser(email);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * GET single alert by ID.
     *
     * @param id the alert ID
     * @return the alert if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeatherAlert>> getAlert(@PathVariable Long id) {
        return alertService.getAlertById(id)
                .map(alert -> ResponseEntity.ok(ApiResponse.success(alert)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Alert not found")));
    }

    /**
     * POST create new alert.
     *
     * @param alert the alert to create
     * @return the created alert
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeatherAlert>> createAlert(@Valid @RequestBody WeatherAlert alert) {
        try {
            WeatherAlert created = alertService.createAlert(alert);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Alert created successfully", created));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create alert: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating alert", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create alert"));
        }
    }

    /**
     * PUT update alert.
     *
     * @param id    the alert ID
     * @param alert the updated alert data
     * @return the updated alert
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeatherAlert>> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody WeatherAlert alert) {
        try {
            WeatherAlert updated = alertService.updateAlert(id, alert);
            return ResponseEntity.ok(ApiResponse.success("Alert updated successfully", updated));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update alert {}: {}", id, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating alert {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update alert"));
        }
    }

    /**
     * DELETE alert.
     *
     * @param id the alert ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAlert(@PathVariable Long id) {
        try {
            alertService.deleteAlert(id);
            return ResponseEntity.ok(ApiResponse.success("Alert deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting alert {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete alert"));
        }
    }

    /**
     * POST toggle alert enabled state.
     *
     * @param id      the alert ID
     * @param enabled true to enable, false to disable
     * @return the updated alert
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<WeatherAlert>> toggleAlert(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        try {
            WeatherAlert toggled = alertService.toggleAlert(id, enabled);
            String message = enabled ? "Alert enabled" : "Alert disabled";
            return ResponseEntity.ok(ApiResponse.success(message, toggled));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to toggle alert {}: {}", id, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error toggling alert {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to toggle alert"));
        }
    }

    /**
     * GET alert history.
     *
     * @param id   the alert ID
     * @param page page number (0-based)
     * @param size page size
     * @return page of alert history records
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<Page<AlertHistory>>> getAlertHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<AlertHistory> history = alertService.getAlertHistory(id, page, size);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            log.error("Error retrieving alert history for alert {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve alert history"));
        }
    }

    /**
     * GET recent alert history across all alerts.
     *
     * @return list of recent history records
     */
    @GetMapping("/history/recent")
    public ResponseEntity<ApiResponse<List<AlertHistory>>> getRecentHistory() {
        try {
            List<AlertHistory> history = alertService.getRecentHistory();
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            log.error("Error retrieving recent alert history", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve alert history"));
        }
    }
}
