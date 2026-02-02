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
    public String dashboard(Model model) {
        // Fractal station data
        weatherService.getLatestReadingForStation("fractal").ifPresent(reading ->
            model.addAttribute("fractalCurrent", reading)
        );
        model.addAttribute("fractalHistory", weatherService.getReadingsForStation("fractal", 24));
        model.addAttribute("fractalStats24h", weatherService.getStatsForStation("fractal", 24));
        model.addAttribute("fractalStats7d", weatherService.getStatsForStation("fractal", 168));

        // RussMonsta station data
        weatherService.getLatestReadingForStation("RussMonsta-House").ifPresent(reading ->
            model.addAttribute("russmonstaoCurrent", reading)
        );
        model.addAttribute("russmonstaHistory", weatherService.getReadingsForStation("RussMonsta-House", 24));
        model.addAttribute("russmonstaStats24h", weatherService.getStatsForStation("RussMonsta-House", 24));
        model.addAttribute("russmonstaStats7d", weatherService.getStatsForStation("RussMonsta-House", 168));

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

