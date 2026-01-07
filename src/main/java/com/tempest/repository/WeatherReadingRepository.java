package com.tempest.repository;

import com.tempest.entity.WeatherReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherReadingRepository extends JpaRepository<WeatherReading, Long> {

    /**
     * Find the latest reading for a specific station.
     */
    Optional<WeatherReading> findTopByStationIdOrderByTimestampDesc(String stationId);

    /**
     * Find the latest reading across all stations.
     */
    Optional<WeatherReading> findTopByOrderByTimestampDesc();

    /**
     * Find readings within a time range.
     */
    List<WeatherReading> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    /**
     * Find readings for a station within a time range.
     */
    List<WeatherReading> findByStationIdAndTimestampBetweenOrderByTimestampAsc(
            String stationId, LocalDateTime start, LocalDateTime end);

    /**
     * Find all readings for a station with pagination.
     */
    Page<WeatherReading> findByStationIdOrderByTimestampDesc(String stationId, Pageable pageable);

    /**
     * Get readings from the last N hours.
     */
    @Query("SELECT r FROM WeatherReading r WHERE r.timestamp >= :since ORDER BY r.timestamp ASC")
    List<WeatherReading> findReadingsSince(@Param("since") LocalDateTime since);

    /**
     * Get readings from the last N hours for a specific station.
     */
    @Query("SELECT r FROM WeatherReading r WHERE r.stationId = :stationId AND r.timestamp >= :since ORDER BY r.timestamp ASC")
    List<WeatherReading> findReadingsSinceForStation(
            @Param("stationId") String stationId,
            @Param("since") LocalDateTime since);

    /**
     * Get average readings grouped by hour for the last N days.
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('hour', timestamp) as hour,
            AVG(temperature) as avg_temp,
            AVG(humidity) as avg_humidity,
            AVG(pressure) as avg_pressure,
            AVG(wind_speed) as avg_wind_speed,
            SUM(rainfall) as total_rainfall
        FROM weather_readings 
        WHERE timestamp >= :since
        GROUP BY DATE_TRUNC('hour', timestamp)
        ORDER BY hour ASC
        """, nativeQuery = true)
    List<Object[]> getHourlyAverages(@Param("since") LocalDateTime since);

    /**
     * Count readings per station.
     */
    @Query("SELECT r.stationId, COUNT(r) FROM WeatherReading r GROUP BY r.stationId")
    List<Object[]> countByStation();

    /**
     * Get min/max values for temperature in a time range.
     */
    @Query("""
        SELECT MIN(r.temperature), MAX(r.temperature), AVG(r.temperature)
        FROM WeatherReading r 
        WHERE r.timestamp >= :since
        """)
    Object[] getTemperatureStats(@Param("since") LocalDateTime since);
}

