package com.tempest.service;

import com.tempest.dto.WeatherReadingDTO;
import com.tempest.dto.WeatherStatsDTO;
import com.tempest.entity.WeatherReading;
import com.tempest.entity.WeatherStation;
import com.tempest.repository.WeatherReadingRepository;
import com.tempest.repository.WeatherStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherReadingRepository readingRepository;
    private final WeatherStationRepository stationRepository;

    /**
     * Convert Celsius to Fahrenheit.
     */
    private Double celsiusToFahrenheit(Double celsius) {
        if (celsius == null) {
            return null;
        }
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    /**
     * Record a new weather reading from a station.
     */
    @Transactional
    public WeatherReading recordReading(WeatherReadingDTO dto) {
        log.debug("Recording weather reading from station: {}", dto.getStationId());

        WeatherReading reading = WeatherReading.builder()
                .stationId(dto.getStationId())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .temperature(dto.getTemperature())
                .humidity(dto.getHumidity())
                .pressure(dto.getPressure())
                .windSpeed(dto.getWindSpeed())
                .windDirection(dto.getWindDirection())
                .rainfall(dto.getRainfall())
                .uvIndex(dto.getUvIndex())
                .lightLevel(dto.getLightLevel())
                .batteryVoltage(dto.getBatteryVoltage())
                .build();

        WeatherReading saved = readingRepository.save(reading);

        // Get or create station and update last seen
        if (dto.getStationId() != null) {
            WeatherStation station = stationRepository.findByStationId(dto.getStationId())
                    .orElseGet(() -> {
                        WeatherStation newStation = WeatherStation.builder()
                                .stationId(dto.getStationId())
                                .name(dto.getStationId())
                                .isActive(true)
                                .build();
                        return stationRepository.save(newStation);
                    });
            station.setLastSeen(LocalDateTime.now());
            stationRepository.save(station);
        }

        log.info("Recorded reading ID {} from station {}", saved.getId(), dto.getStationId());
        return saved;
    }

    /**
     * Convert a WeatherReading's temperature to Fahrenheit.
     * Creates a new WeatherReading to avoid mutating the JPA-managed entity.
     */
    private WeatherReading convertToFahrenheit(WeatherReading reading) {
        if (reading == null) {
            return null;
        }
        return WeatherReading.builder()
                .id(reading.getId())
                .stationId(reading.getStationId())
                .timestamp(reading.getTimestamp())
                .temperature(celsiusToFahrenheit(reading.getTemperature()))
                .humidity(reading.getHumidity())
                .pressure(reading.getPressure())
                .windSpeed(reading.getWindSpeed())
                .windDirection(reading.getWindDirection())
                .rainfall(reading.getRainfall())
                .uvIndex(reading.getUvIndex())
                .lightLevel(reading.getLightLevel())
                .batteryVoltage(reading.getBatteryVoltage())
                .createdAt(reading.getCreatedAt())
                .build();
    }

    /**
     * Convert a list of WeatherReadings' temperatures to Fahrenheit.
     */
    private List<WeatherReading> convertListToFahrenheit(List<WeatherReading> readings) {
        return readings.stream()
                .map(this::convertToFahrenheit)
                .toList();
    }

    /**
     * Get the latest weather reading.
     */
    public Optional<WeatherReading> getLatestReading() {
        return readingRepository.findTopByOrderByTimestampDesc()
                .map(this::convertToFahrenheit);
    }

    /**
     * Get the latest reading for a specific station.
     */
    public Optional<WeatherReading> getLatestReadingForStation(String stationId) {
        return readingRepository.findTopByStationIdOrderByTimestampDesc(stationId)
                .map(this::convertToFahrenheit);
    }

    /**
     * Get readings from the last N hours (ascending order for charts).
     */
    public List<WeatherReading> getReadingsForLastHours(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return convertListToFahrenheit(readingRepository.findReadingsSince(since));
    }

    /**
     * Get readings from the last N hours (descending order for history view).
     */
    public List<WeatherReading> getReadingsForLastHoursDesc(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return convertListToFahrenheit(readingRepository.findReadingsSinceDesc(since));
    }

    /**
     * Get readings for a station from the last N hours.
     */
    public List<WeatherReading> getReadingsForStation(String stationId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return convertListToFahrenheit(readingRepository.findReadingsSinceForStation(stationId, since));
    }

    /**
     * Get paginated readings.
     */
    public Page<WeatherReading> getReadings(int page, int size) {
        return readingRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Get weather statistics for the last N hours.
     */
    public WeatherStatsDTO getStats(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<WeatherReading> readings = readingRepository.findReadingsSince(since);

        if (readings.isEmpty()) {
            return WeatherStatsDTO.empty();
        }

        return WeatherStatsDTO.builder()
                .minTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .min().orElse(0)))
                .maxTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .max().orElse(0)))
                .avgTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .average().orElse(0)))
                .avgHumidity(readings.stream()
                        .filter(r -> r.getHumidity() != null)
                        .mapToDouble(WeatherReading::getHumidity)
                        .average().orElse(0))
                .avgPressure(readings.stream()
                        .filter(r -> r.getPressure() != null)
                        .mapToDouble(WeatherReading::getPressure)
                        .average().orElse(0))
                .totalRainfall(readings.stream()
                        .filter(r -> r.getRainfall() != null)
                        .mapToDouble(WeatherReading::getRainfall)
                        .sum())
                .maxWindSpeed(readings.stream()
                        .filter(r -> r.getWindSpeed() != null)
                        .mapToDouble(WeatherReading::getWindSpeed)
                        .max().orElse(0))
                .readingCount(readings.size())
                .periodHours(hours)
                .build();
    }

    /**
     * Get weather statistics for a specific station for the last N hours.
     */
    public WeatherStatsDTO getStatsForStation(String stationId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<WeatherReading> readings = readingRepository.findReadingsSinceForStation(stationId, since);

        if (readings.isEmpty()) {
            return WeatherStatsDTO.empty();
        }

        return WeatherStatsDTO.builder()
                .minTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .min().orElse(0)))
                .maxTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .max().orElse(0)))
                .avgTemperature(celsiusToFahrenheit(readings.stream()
                        .filter(r -> r.getTemperature() != null)
                        .mapToDouble(WeatherReading::getTemperature)
                        .average().orElse(0)))
                .avgHumidity(readings.stream()
                        .filter(r -> r.getHumidity() != null)
                        .mapToDouble(WeatherReading::getHumidity)
                        .average().orElse(0))
                .avgPressure(readings.stream()
                        .filter(r -> r.getPressure() != null)
                        .mapToDouble(WeatherReading::getPressure)
                        .average().orElse(0))
                .totalRainfall(readings.stream()
                        .filter(r -> r.getRainfall() != null)
                        .mapToDouble(WeatherReading::getRainfall)
                        .sum())
                .maxWindSpeed(readings.stream()
                        .filter(r -> r.getWindSpeed() != null)
                        .mapToDouble(WeatherReading::getWindSpeed)
                        .max().orElse(0))
                .readingCount(readings.size())
                .periodHours(hours)
                .build();
    }

    // Station management methods

    public List<WeatherStation> getAllStations() {
        return stationRepository.findAll();
    }

    public List<WeatherStation> getActiveStations() {
        return stationRepository.findByIsActiveTrue();
    }

    public Optional<WeatherStation> getStation(String stationId) {
        return stationRepository.findByStationId(stationId);
    }

    @Transactional
    public WeatherStation createOrUpdateStation(WeatherStation station) {
        return stationRepository.save(station);
    }
}

