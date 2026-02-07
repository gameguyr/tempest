package com.tempest.entity;

/**
 * Weather metrics available for alert thresholds.
 */
public enum WeatherMetric {
    TEMPERATURE("Temperature", "°F"),
    HUMIDITY("Humidity", "%"),
    PRESSURE("Pressure", "hPa"),
    WIND_SPEED("Wind Speed", "km/h"),
    RAINFALL("Rainfall", "mm"),
    UV_INDEX("UV Index", ""),
    LIGHT_LEVEL("Light Level", "lux"),
    BATTERY_VOLTAGE("Battery Voltage", "V");

    private final String displayName;
    private final String unit;

    WeatherMetric(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
}
