package top.brahman.grndhog.weather.entity;

import static top.brahman.grndhog.weather.util.Util.CACHE_TTL_SEC;
import static top.brahman.grndhog.weather.util.Util.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.beans.Transient;
import java.time.Instant;

/**
 * Domain entity representing the full weather‐information response for a city.
 * <p>
 * This class encapsulates the relevant weather properties returned by the groundhog-weather SDK for a requested city, and also provides utility methods including
 * JSON serialization.
 * </p>
 *
 * <h3>Fields</h3>
 * <ul>
 *   <li>{@link  Weather} — a nested record type carrying {@link  Weather#main} and {@link Weather#description} (e.g., "Clouds", "scattered clouds").</li>
 *   <li>{@link  Temperature} — nested record with {@link  Temperature#temp} and {@link  Temperature#feels_like} values.</li>
 *   <li>{@link  CityWeather#visibility} — visibility in metres (e.g., 10000).</li>
 *   <li>{@link  Wind} — nested record with speed in m/s.</li>
 *   <li>{@link  CityWeather#datetime} — UNIX epoch seconds at which the weather is applicable.</li>
 *   <li>{@link  Sys} — nested record with sunrise and sunset times in UNIX epoch seconds.</li>
 *   <li>{@link  CityWeather#timezone} — offset in seconds from UTC (e.g., 3600).</li>
 *   <li>{@link  CityWeather#name} — the name of the city (e.g., "Zocca").</li>
 * </ul>
 *
 * <h3>Cache TTL / Expiry</h3>
 * <p>
 * The method {@link #isExpired()} returns whether the data is older than the defined
 * cache-time-to-live (TTL) constant {@link top.brahman.grndhog.weather.util.Util#CACHE_TTL_SEC} relative to {@code datetime}.
 * </p>
 *
 * <h3>JSON Serialization</h3>
 * <p>
 * The {@link #toJson()} method returns a JSON string matching the expected SDK API response structure:
 * <pre>
 * {
 *   "weather": {
 *     "main": "Clouds",
 *     "description": "scattered clouds"
 *   },
 *   "temperature": {
 *     "temp": 269.6,
 *     "feels_like": 267.57
 *   },
 *   "visibility": 10000,
 *   "wind": {
 *     "speed": 1.38
 *   },
 *   "datetime": 1675744800,
 *   "sys": {
 *     "sunrise": 1675751262,
 *     "sunset": 1675787560
 *   },
 *   "timezone": 3600,
 *   "name": "Zocca"
 * }
 * </pre>
 * Although clients of the SDK normally work with this object directly (rather than raw JSON),
 * this method allows retrieval of the JSON string when needed.
 * <p>
 * Note: If JSON serialization fails internally (via Jackson's {@link  top.brahman.grndhog.weather.util.Util#mapper}),
 * the method catches {@link com.fasterxml.jackson.core.JsonProcessingException} and returns
 * a fallback string of the form {@code "{error: <message>}"}.
 * </p>
 *
 * @param weather     the weather summary data
 * @param temperature the temperature details
 * @param visibility  the visibility in metres
 * @param wind        the wind details
 * @param datetime    the UNIX epoch seconds timestamp for this data
 * @param sys         the system data (sunrise/sunset)
 * @param timezone    the timezone offset in seconds from UTC
 * @param name        the city name
 */
public record CityWeather(Weather weather, Temperature temperature, int visibility, Wind wind,
                          long datetime, Sys sys, int timezone, String name) {
    @Transient
    public boolean isExpired() {
        return Instant.now().getEpochSecond() - datetime > CACHE_TTL_SEC;
    }

    public record Weather(String main, String description) {}

    public record Temperature(double temp, double feels_like) {}

    public record Wind(double speed) {}

    public record Sys(long sunrise, long sunset) {}

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{error: " + e.getMessage() + "}";
        }
    }
}
