package top.brahman.dev.weather.exception;

import top.brahman.dev.weather.client.WeatherClient;

/**
 * Exception thrown when a requested city is not found in the OpenWeatherMap database.
 * <p>
 * This exception is typically raised when the API returns a 404 HTTP status code,
 * indicating that the specified city name does not exist in the weather service's
 * database or the name was misspelled.
 * </p>
 *
 * <h2>Common Causes</h2>
 * <ul>
 *   <li>City name is misspelled</li>
 *   <li>City is too small or not in the OpenWeatherMap database</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * try {
 *     CityWeather weather = client.getWeatherFor("NonExistentCity");
 * } catch (CityNotFoundException e) {
 *     System.err.println("City not found: " + e.getMessage());
 * }
 * }</pre>
 *
 * @see WeatherClient#getWeatherFor(String)
 */
public class CityNotFoundException extends RuntimeException {
    /**
     * Constructs a new CityNotFoundException for the specified city.
     *
     * @param city the name of the city that was not found
     */
    public CityNotFoundException(String city) {
        super(city + " not found");
    }
}
