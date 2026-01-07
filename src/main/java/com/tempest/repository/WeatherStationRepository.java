package com.tempest.repository;

import com.tempest.entity.WeatherStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {

    Optional<WeatherStation> findByStationId(String stationId);

    Optional<WeatherStation> findByApiKey(String apiKey);

    List<WeatherStation> findByIsActiveTrue();

    boolean existsByStationId(String stationId);
}

