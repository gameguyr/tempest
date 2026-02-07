package com.tempest.service;

import com.tempest.entity.*;
import com.tempest.repository.AlertHistoryRepository;
import com.tempest.repository.WeatherAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for evaluating weather readings against alert conditions.
 * This is the core alert checking logic that determines when to trigger notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationService {

    private final WeatherAlertRepository alertRepository;
    private final AlertHistoryRepository historyRepository;
    private final NotificationService notificationService;

    /**
     * Evaluate a weather reading against all applicable alerts.
     *
     * @param reading the weather reading to evaluate
     */
    @Transactional
    public void evaluateReading(WeatherReading reading) {
        log.debug("Evaluating reading {} from station {}", reading.getId(), reading.getStationId());

        // Get enabled alerts for this station
        List<WeatherAlert> stationAlerts =
                alertRepository.findByStationIdAndIsEnabledTrue(reading.getStationId());

        // Get enabled alerts that apply to all stations
        List<WeatherAlert> globalAlerts =
                alertRepository.findByStationIdIsNullAndIsEnabledTrue();

        List<WeatherAlert> allAlerts = new ArrayList<>();
        allAlerts.addAll(stationAlerts);
        allAlerts.addAll(globalAlerts);

        for (WeatherAlert alert : allAlerts) {
            evaluateAlertForReading(alert, reading);
        }
    }

    /**
     * Evaluate a single alert against a reading.
     *
     * @param alert   the alert to evaluate
     * @param reading the weather reading
     */
    private void evaluateAlertForReading(WeatherAlert alert, WeatherReading reading) {
        // Skip if in cooldown
        if (alert.isInCooldown()) {
            log.debug("Alert {} is in cooldown, skipping", alert.getId());
            return;
        }

        // Extract metric value from reading
        Double actualValue = extractMetricValue(alert.getMetric(), reading);
        if (actualValue == null) {
            log.debug("Metric {} not available in reading {}", alert.getMetric(), reading.getId());
            return;
        }

        // Evaluate condition
        boolean triggered = alert.getOperator().evaluate(actualValue, alert.getThreshold());

        if (triggered) {
            handleAlertTriggered(alert, reading, actualValue);
        }
    }

    /**
     * Handle an alert being triggered.
     *
     * @param alert       the triggered alert
     * @param reading     the weather reading that triggered it
     * @param actualValue the actual value that exceeded the threshold
     */
    private void handleAlertTriggered(WeatherAlert alert, WeatherReading reading, Double actualValue) {
        log.info("Alert triggered: {} - {} {} {} (actual: {})",
                alert.getName(), alert.getMetric(), alert.getOperator().getSymbol(),
                alert.getThreshold(), actualValue);

        // Update alert state
        alert.setLastTriggeredAt(LocalDateTime.now());
        alert.setTriggerCount(alert.getTriggerCount() + 1);
        alertRepository.save(alert);

        // Send notification
        boolean notificationSent = false;
        String notificationError = null;

        try {
            notificationService.sendAlertNotification(alert, reading, actualValue);
            notificationSent = true;
        } catch (Exception e) {
            log.error("Failed to send notification for alert {}", alert.getId(), e);
            notificationError = e.getMessage();
        }

        // Record history
        AlertHistory history = AlertHistory.builder()
                .alertId(alert.getId())
                .alertName(alert.getName())
                .stationId(reading.getStationId())
                .readingId(reading.getId())
                .metric(alert.getMetric())
                .actualValue(actualValue)
                .thresholdValue(alert.getThreshold())
                .operator(alert.getOperator())
                .notificationSent(notificationSent)
                .notificationError(notificationError)
                .build();

        historyRepository.save(history);
    }

    /**
     * Extract metric value from reading based on metric type.
     *
     * @param metric  the metric to extract
     * @param reading the weather reading
     * @return the metric value, or null if not available
     */
    private Double extractMetricValue(WeatherMetric metric, WeatherReading reading) {
        return switch (metric) {
            case TEMPERATURE -> reading.getTemperature();
            case HUMIDITY -> reading.getHumidity();
            case PRESSURE -> reading.getPressure();
            case WIND_SPEED -> reading.getWindSpeed();
            case RAINFALL -> reading.getRainfall();
            case UV_INDEX -> reading.getUvIndex();
            case LIGHT_LEVEL -> reading.getLightLevel();
            case BATTERY_VOLTAGE -> reading.getBatteryVoltage();
        };
    }
}
