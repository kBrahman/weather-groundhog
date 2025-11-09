package top.brahman.dev.weather.exception;

import top.brahman.dev.weather.client.WeatherClient;

import java.io.IOException;

/**
 * Exception thrown when a general failure occurs during weather data retrieval.
 * <p>
 * This exception wraps various failures that can occur when communicating with
 * the OpenWeatherMap API, including network issues, timeout errors, malformed
 * responses, or other I/O-related problems.
 * </p>
 *
 * <h2>Common Scenarios</h2>
 * <ul>
 *   <li>Network connectivity issues (no internet connection)</li>
 *   <li>API server downtime or timeout</li>
 *   <li>Invalid API key or authentication failure</li>
 *   <li>Rate limiting (too many requests)</li>
 *   <li>Malformed API response</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * try {
 *     CityWeather weather = client.getWeatherFor("Berlin");
 * } catch (WeatherClientException e) {
 *     System.err.println("Failed to fetch weather: " + e.getMessage());
 *     if (e.getCause() != null) {
 *         System.err.println("Cause: " + e.getCause().getMessage());
 *     }
 * }
 * }</pre>
 *
 * @see WeatherClient#getWeatherFor(String)
 */
public class WeatherClientException extends IOException {
    /**
     * Constructs a new WeatherClientException with the specified error message.
     *
     * @param message the detail message explaining the failure
     */
    public WeatherClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new WeatherClientException with the specified error message
     * and underlying cause.
     *
     * @param message the detail message explaining the failure
     * @param cause   the underlying exception that caused this failure
     */
    public WeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
