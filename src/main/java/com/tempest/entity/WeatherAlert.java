package com.tempest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a weather alert configuration.
 * Alerts trigger notifications when weather conditions meet specified thresholds.
 */
@Entity
@Table(name = "weather_alerts", indexes = {
        @Index(name = "idx_alert_station", columnList = "stationId"),
        @Index(name = "idx_alert_enabled", columnList = "isEnabled"),
        @Index(name = "idx_alert_user_email", columnList = "userEmail")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User-friendly name for the alert.
     */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /**
     * Optional description providing more context.
     */
    private String description;

    /**
     * Station ID to monitor. Null means monitor all stations.
     */
    @Column(name = "station_id")
    private String stationId;

    /**
     * Weather metric to monitor (temperature, humidity, etc.).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeatherMetric metric;

    /**
     * Comparison operator for threshold evaluation.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComparisonOperator operator;

    /**
     * Threshold value that triggers the alert.
     */
    @NotNull
    @Column(nullable = false)
    private Double threshold;

    /**
     * Email address for notifications.
     */
    @Column(name = "user_email")
    private String userEmail;

    /**
     * Phone number for SMS notifications (E.164 format, e.g., +15551234567).
     */
    @Column(name = "user_phone")
    private String userPhone;

    /**
     * Type of notification to send (EMAIL, SMS, or BOTH).
     */
    @NotNull
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    /**
     * Whether the alert is currently enabled.
     */
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    /**
     * Cooldown period in minutes to prevent notification spam.
     */
    @Column(name = "cooldown_minutes")
    @Builder.Default
    private Integer cooldownMinutes = 60;

    /**
     * Timestamp of when the alert was last triggered.
     */
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    /**
     * Number of times this alert has been triggered.
     */
    @Column(name = "trigger_count")
    @Builder.Default
    private Long triggerCount = 0L;

    /**
     * Timestamp of when the alert was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the alert was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the alert is currently in cooldown period.
     *
     * @return true if in cooldown, false otherwise
     */
    public boolean isInCooldown() {
        if (lastTriggeredAt == null) {
            return false;
        }
        LocalDateTime cooldownEnd = lastTriggeredAt.plusMinutes(cooldownMinutes);
        return LocalDateTime.now().isBefore(cooldownEnd);
    }
}
