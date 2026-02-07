package com.tempest.entity;

/**
 * Types of notifications that can be sent for alerts.
 */
public enum NotificationType {
    /**
     * Send notification via email only.
     */
    EMAIL,

    /**
     * Send notification via SMS only.
     */
    SMS,

    /**
     * Send notification via both email and SMS.
     */
    BOTH
}
