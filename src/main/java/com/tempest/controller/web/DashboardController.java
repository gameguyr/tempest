package com.tempest.controller.web;

import com.tempest.entity.WeatherReading;
import com.tempest.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Web controller for the dashboard UI.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final WeatherService weatherService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String station, Model model) {
        // Get active stations for toggle UI
        model.addAttribute("stations", weatherService.getActiveStations());
        model.addAttribute("selectedStation", station);

        if (station != null && !station.isBlank()) {
            // Station-specific view
            weatherService.getLatestReadingForStation(station).ifPresent(reading ->
                model.addAttribute("current", reading)
            );

            // Get readings for the last 24 hours for charts
            List<WeatherReading> history = weatherService.getReadingsForStation(station, 24);
            model.addAttribute("history", history);

            // Get statistics
            model.addAttribute("stats24h", weatherService.getStatsForStation(station, 24));
            model.addAttribute("stats7d", weatherService.getStatsForStation(station, 168)); // 7 days
        } else {
            // All stations view
            weatherService.getLatestReading().ifPresent(reading ->
                model.addAttribute("current", reading)
            );

            // Get readings for the last 24 hours for charts
            List<WeatherReading> history = weatherService.getReadingsForLastHours(24);
            model.addAttribute("history", history);

            // Get statistics
            model.addAttribute("stats24h", weatherService.getStats(24));
            model.addAttribute("stats7d", weatherService.getStats(168)); // 7 days
        }

        return "dashboard";
    }

    @GetMapping("/station/{stationId}")
    public String stationDetail(@PathVariable String stationId, Model model) {
        weatherService.getStation(stationId).ifPresent(station -> 
            model.addAttribute("station", station)
        );

        weatherService.getLatestReadingForStation(stationId).ifPresent(reading ->
            model.addAttribute("current", reading)
        );

        List<WeatherReading> history = weatherService.getReadingsForStation(stationId, 24);
        model.addAttribute("history", history);

        return "station-detail";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<WeatherReading> readings = weatherService.getReadingsForLastHoursDesc(168); // 7 days, most recent first
        model.addAttribute("readings", readings);
        return "history";
    }

    @GetMapping("/stations")
    public String stations(Model model) {
        model.addAttribute("stations", weatherService.getAllStations());
        return "stations";
    }
}

