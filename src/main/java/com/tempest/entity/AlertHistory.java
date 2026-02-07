package com.tempest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing the history of triggered alerts.
 * Provides an audit trail for debugging and monitoring.
 */
@Entity
@Table(name = "alert_history", indexes = {
        @Index(name = "idx_history_alert_id", columnList = "alertId"),
        @Index(name = "idx_history_triggered_at", columnList = "triggeredAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the alert that was triggered.
     */
    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    /**
     * Name of the alert at the time it was triggered.
     */
    @Column(name = "alert_name")
    private String alertName;

    /**
     * Station ID that triggered the alert.
     */
    @Column(name = "station_id")
    private String stationId;

    /**
     * ID of the weather reading that triggered the alert.
     */
    @Column(name = "reading_id")
    private Long readingId;

    /**
     * Metric that was being monitored.
     */
    @Enumerated(EnumType.STRING)
    private WeatherMetric metric;

    /**
     * Actual value of the metric when the alert triggered.
     */
    @Column(name = "actual_value")
    private Double actualValue;

    /**
     * Threshold value that was exceeded/met.
     */
    @Column(name = "threshold_value")
    private Double thresholdValue;

    /**
     * Operator used for comparison.
     */
    @Enumerated(EnumType.STRING)
    private ComparisonOperator operator;

    /**
     * Whether the notification was successfully sent.
     */
    @Column(name = "notification_sent")
    private Boolean notificationSent;

    /**
     * Error message if notification failed.
     */
    @Column(name = "notification_error", length = 500)
    private String notificationError;

    /**
     * Timestamp when the alert was triggered.
     */
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }
}
