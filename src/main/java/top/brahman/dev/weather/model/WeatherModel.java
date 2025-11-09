package top.brahman.dev.weather.model;

import top.brahman.dev.weather.entity.CityWeather;
import top.brahman.dev.weather.util.CityWeatherDeserializer;

import java.util.List;

/**
 * Internal data transfer object (DTO) representing the raw JSON structure
 * returned by the OpenWeatherMap API.
 * <p>
 * This model is used as an intermediate representation during deserialization
 * of the API response. It matches the exact JSON structure from OpenWeatherMap
 * and is then converted to the SDK's public {@link CityWeather} domain entity
 * via the {@link #toCityWeather()} method.
 * </p>
 *
 * <h2>Conversion Flow</h2>
 * <pre>
 * API JSON Response → WeatherModel (internal) → CityWeather (public)
 * </pre>
 *
 * <p>
 * This separation allows the SDK to adapt to API changes without affecting
 * the public interface exposed to SDK users.
 * </p>
 *
 * @param weather    list of weather conditions (SDK uses only the first element)
 * @param main       temperature data in the API's format
 * @param visibility visibility in metres
 * @param wind       wind information
 * @param dt         data calculation time, UNIX epoch timestamp
 * @param sys        system data (sunrise/sunset)
 * @param timezone   timezone offset from UTC in seconds
 * @param name       city name
 * @see CityWeather
 * @see CityWeatherDeserializer
 */
public record WeatherModel(List<CityWeather.Weather> weather, Main main, int visibility, CityWeather.Wind wind,
                           long dt,
                           CityWeather.Sys sys, int timezone, String name) {
    /**
     * Internal record representing the temperature section of the API response.
     * <p>
     * This is converted to {@link CityWeather.Temperature} during transformation.
     * </p>
     *
     * @param temp       actual temperature in Kelvin
     * @param feels_like perceived temperature in Kelvin
     */
    private record Main(double temp, double feels_like) {}

    /**
     * Converts this internal model to the public-facing {@link CityWeather} entity.
     * <p>
     * This method transforms the API's response structure into the SDK's domain
     * model that clients interact with. The conversion includes extracting the
     * first weather condition from the list and restructuring temperature data.
     * </p>
     *
     * @return a {@link CityWeather} instance ready for client consumption
     */
    public CityWeather toCityWeather() {
        return new CityWeather(weather.getFirst(), new CityWeather.Temperature(main.temp, main.feels_like), visibility, wind, dt, sys, timezone,
                name);
    }
}
