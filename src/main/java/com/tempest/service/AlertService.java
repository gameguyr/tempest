package com.tempest.service;

import com.tempest.entity.AlertHistory;
import com.tempest.entity.NotificationType;
import com.tempest.entity.WeatherAlert;
import com.tempest.repository.AlertHistoryRepository;
import com.tempest.repository.WeatherAlertRepository;
import com.tempest.repository.WeatherStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing weather alerts.
 * Handles CRUD operations, validation, and alert lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertService {

    private final WeatherAlertRepository alertRepository;
    private final AlertHistoryRepository historyRepository;
    private final WeatherStationRepository stationRepository;

    /**
     * Create a new alert.
     *
     * @param alert the alert to create
     * @return the created alert
     * @throws IllegalArgumentException if validation fails
     */
    public WeatherAlert createAlert(WeatherAlert alert) {
        validateAlert(alert);
        log.info("Creating alert: {} for metric: {}", alert.getName(), alert.getMetric());
        return alertRepository.save(alert);
    }

    /**
     * Update an existing alert.
     *
     * @param id          the ID of the alert to update
     * @param alertUpdate the updated alert data
     * @return the updated alert
     * @throws IllegalArgumentException if alert not found or validation fails
     */
    public WeatherAlert updateAlert(Long id, WeatherAlert alertUpdate) {
        WeatherAlert existing = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));

        validateAlert(alertUpdate);

        // Update fields
        existing.setName(alertUpdate.getName());
        existing.setDescription(alertUpdate.getDescription());
        existing.setStationId(alertUpdate.getStationId());
        existing.setMetric(alertUpdate.getMetric());
        existing.setOperator(alertUpdate.getOperator());
        existing.setThreshold(alertUpdate.getThreshold());
        existing.setUserEmail(alertUpdate.getUserEmail());
        existing.setUserPhone(alertUpdate.getUserPhone());
        existing.setNotificationType(alertUpdate.getNotificationType());
        existing.setCooldownMinutes(alertUpdate.getCooldownMinutes());

        log.info("Updated alert: {}", id);
        return alertRepository.save(existing);
    }

    /**
     * Delete an alert by ID.
     *
     * @param id the ID of the alert to delete
     */
    public void deleteAlert(Long id) {
        log.info("Deleting alert: {}", id);
        alertRepository.deleteById(id);
    }

    /**
     * Toggle an alert's enabled state.
     *
     * @param id      the ID of the alert
     * @param enabled true to enable, false to disable
     * @return the updated alert
     * @throws IllegalArgumentException if alert not found
     */
    public WeatherAlert toggleAlert(Long id, boolean enabled) {
        WeatherAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));

        alert.setIsEnabled(enabled);
        log.info("Alert {} {}", id, enabled ? "enabled" : "disabled");
        return alertRepository.save(alert);
    }

    /**
     * Get all alerts.
     *
     * @return list of all alerts
     */
    @Transactional(readOnly = true)
    public List<WeatherAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    /**
     * Get an alert by ID.
     *
     * @param id the alert ID
     * @return optional containing the alert if found
     */
    @Transactional(readOnly = true)
    public Optional<WeatherAlert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    /**
     * Get alerts by user email.
     *
     * @param userEmail the user's email address
     * @return list of alerts for the user
     */
    @Transactional(readOnly = true)
    public List<WeatherAlert> getAlertsByUser(String userEmail) {
        return alertRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    /**
     * Get alert history for a specific alert.
     *
     * @param alertId the alert ID
     * @param page    page number (0-based)
     * @param size    page size
     * @return page of alert history records
     */
    @Transactional(readOnly = true)
    public Page<AlertHistory> getAlertHistory(Long alertId, int page, int size) {
        return historyRepository.findByAlertIdOrderByTriggeredAtDesc(
                alertId, PageRequest.of(page, size));
    }

    /**
     * Get recent alert history across all alerts.
     *
     * @return list of recent history records
     */
    @Transactional(readOnly = true)
    public List<AlertHistory> getRecentHistory() {
        return historyRepository.findTop100ByOrderByTriggeredAtDesc();
    }

    /**
     * Validate alert configuration.
     *
     * @param alert the alert to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAlert(WeatherAlert alert) {
        // Name validation
        if (alert.getName() == null || alert.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Alert name is required");
        }

        // Metric validation
        if (alert.getMetric() == null) {
            throw new IllegalArgumentException("Metric is required");
        }

        // Operator validation
        if (alert.getOperator() == null) {
            throw new IllegalArgumentException("Operator is required");
        }

        // Threshold validation
        if (alert.getThreshold() == null) {
            throw new IllegalArgumentException("Threshold is required");
        }

        // Notification type validation
        if (alert.getNotificationType() == null) {
            throw new IllegalArgumentException("Notification type is required");
        }

        // Validate notification contacts based on type
        if (alert.getNotificationType() == NotificationType.EMAIL ||
                alert.getNotificationType() == NotificationType.BOTH) {
            if (alert.getUserEmail() == null || !isValidEmail(alert.getUserEmail())) {
                throw new IllegalArgumentException("Valid email is required for email notifications");
            }
        }

        if (alert.getNotificationType() == NotificationType.SMS ||
                alert.getNotificationType() == NotificationType.BOTH) {
            if (alert.getUserPhone() == null || !isValidPhone(alert.getUserPhone())) {
                throw new IllegalArgumentException("Valid phone number is required for SMS notifications (E.164 format, e.g., +15551234567)");
            }
        }

        // Validate station exists if specified
        if (alert.getStationId() != null && !alert.getStationId().trim().isEmpty()) {
            stationRepository.findByStationId(alert.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station not found: " + alert.getStationId()));
        }
    }

    /**
     * Validate email format.
     *
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate phone number format (E.164).
     *
     * @param phone the phone number to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPhone(String phone) {
        // E.164 format validation (e.g., +15551234567)
        return phone != null && phone.matches("^\\+[1-9]\\d{1,14}$");
    }
}
