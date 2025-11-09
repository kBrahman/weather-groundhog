package top.brahman.dev.weather.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.brahman.dev.weather.cache.LRUCache;
import top.brahman.dev.weather.entity.CityWeather;

/**
 * Utility class containing shared constants and configuration for the Weather SDK.
 * <p>
 * This class provides centralized configuration values used throughout the SDK,
 * including cache settings, API endpoints, and JSON processing configuration.
 * </p>
 *
 * <h2>Configuration Constants</h2>
 * <ul>
 *   <li>{@link #CACHE_TTL_SEC} — How long weather data remains fresh</li>
 *   <li>{@link #CACHE_CAPACITY} — Maximum number of cities stored in cache</li>
 *   <li>{@link #URL} — Base URL for OpenWeatherMap API calls</li>
 *   <li>{@link #mapper} — Shared Jackson ObjectMapper for JSON processing</li>
 * </ul>
 */
public class Util {
    /**
     * Cache Time-To-Live in seconds.
     * <p>
     * Weather data is considered fresh (up to date) if retrieved within this time window.
     * After this period expires:
     * </p>
     * <ul>
     *   <li>In <b>ON_DEMAND</b> mode: A fresh API call is triggered</li>
     *   <li>In <b>POLLING</b> mode: Background refresh is scheduled</li>
     * </ul>
     * <p>
     * Default: <b>600 seconds (10 minutes)</b> — balances freshness with API rate limits.
     * </p>
     */
    public static final int CACHE_TTL_SEC = 600;

    /**
     * Maximum number of cities stored in the LRU cache.
     * <p>
     * When this limit is reached, the least recently accessed city's data is
     * automatically evicted to make room for new entries. This prevents unbounded
     * memory growth while keeping frequently accessed cities readily available.
     * </p>
     * <p>
     * Default: <b>10 cities</b> — suitable for most applications.
     * </p>
     *
     * @see LRUCache
     */
    public static final int CACHE_CAPACITY = 10;

    /**
     * Base URL for the OpenWeatherMap API (version 2.5).
     * <p>
     * All weather data requests are made to endpoints under this base URL.
     * Example full endpoint: {@code https://api.openweathermap.org/data/2.5/weather}
     * </p>
     */
    public static final String URL = "https://api.openweathermap.org/data/2.5/";

    /**
     * Shared Jackson ObjectMapper for JSON serialization and deserialization.
     * <p>
     * Configured to ignore unknown properties in JSON responses, making the SDK
     * more resilient to API schema changes. This mapper is used for:
     * </p>
     * <ul>
     *   <li>Deserializing API responses into internal models</li>
     *   <li>Serializing {@link CityWeather} to JSON</li>
     * </ul>
     * <p>
     * The lenient configuration ({@code FAIL_ON_UNKNOWN_PROPERTIES = false}) ensures
     * that adding new fields to the API won't break existing SDK functionality.
     * </p>
     */
    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
