package com.tempest.repository;

import com.tempest.entity.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing WeatherAlert entities.
 */
@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    /**
     * Find all enabled alerts.
     *
     * @return list of enabled alerts
     */
    List<WeatherAlert> findByIsEnabledTrue();

    /**
     * Find enabled alerts for a specific station.
     *
     * @param stationId the station ID to filter by
     * @return list of enabled alerts for the station
     */
    List<WeatherAlert> findByStationIdAndIsEnabledTrue(String stationId);

    /**
     * Find enabled alerts that apply to all stations (stationId is null).
     *
     * @return list of enabled global alerts
     */
    List<WeatherAlert> findByStationIdIsNullAndIsEnabledTrue();

    /**
     * Find all alerts by user email, ordered by creation date descending.
     *
     * @param userEmail the user's email address
     * @return list of alerts for the user
     */
    List<WeatherAlert> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    /**
     * Count the number of enabled alerts.
     *
     * @return count of enabled alerts
     */
    long countByIsEnabledTrue();
}
