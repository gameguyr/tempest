package com.tempest.service;

import com.tempest.entity.WeatherReading;
import com.tempest.repository.AlertHistoryRepository;
import com.tempest.repository.WeatherReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for scheduled alert checking and maintenance tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertSchedulerService {

    private final WeatherReadingRepository readingRepository;
    private final AlertEvaluationService evaluationService;
    private final AlertHistoryRepository historyRepository;

    /**
     * Check alerts every 5 minutes.
     * Evaluates the latest readings against all active alerts.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void checkAlerts() {
        log.debug("Starting scheduled alert check");

        try {
            // Get latest readings from the last 10 minutes (to catch any we might have missed)
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            List<WeatherReading> recentReadings = readingRepository.findReadingsSince(since);

            if (recentReadings.isEmpty()) {
                log.debug("No recent readings found, skipping alert check");
                return;
            }

            // Group by station and get latest for each
            Map<String, WeatherReading> latestByStation = recentReadings.stream()
                    .collect(Collectors.toMap(
                            WeatherReading::getStationId,
                            Function.identity(),
                            (r1, r2) -> r1.getTimestamp().isAfter(r2.getTimestamp()) ? r1 : r2
                    ));

            log.info("Checking alerts for {} stations with recent readings", latestByStation.size());

            for (WeatherReading reading : latestByStation.values()) {
                evaluationService.evaluateReading(reading);
            }

            log.debug("Alert check completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled alert check", e);
        }
    }

    /**
     * Cleanup old alert history entries.
     * Runs daily at 2 AM to maintain database size.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldHistory() {
        log.info("Starting alert history cleanup");

        try {
            // Keep history for 90 days
            LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
            historyRepository.deleteByTriggeredAtBefore(cutoff);

            log.info("Alert history cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during alert history cleanup", e);
        }
    }
}
