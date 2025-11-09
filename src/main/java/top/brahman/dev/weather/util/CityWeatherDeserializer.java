package top.brahman.dev.weather.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.*;
import top.brahman.dev.weather.client.WeatherClient;
import top.brahman.dev.weather.exception.WeatherClientException;
import top.brahman.dev.weather.entity.CityWeather;
import top.brahman.dev.weather.model.WeatherModel;

import java.lang.reflect.Type;

import static top.brahman.dev.weather.util.Util.mapper;

/**
 * Custom Gson deserializer for converting OpenWeatherMap API responses into {@link CityWeather} objects.
 * <p>
 * This deserializer bridges Gson (used by Retrofit for HTTP calls) and Jackson (used for
 * internal JSON processing). It performs a two-step deserialization:
 * </p>
 * <ol>
 *   <li>Jackson deserializes the JSON into an internal {@link WeatherModel}</li>
 *   <li>The model is then transformed into the public {@link CityWeather} entity</li>
 * </ol>
 *
 * <h2>Error Handling</h2>
 * <p>
 * If deserialization fails (e.g., due to malformed JSON or schema mismatch),
 * the method returns {@code null}. The calling code should handle this case
 * appropriately (typically by throwing a {@link WeatherClientException}).
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * This deserializer is automatically registered with Gson when building the
 * Retrofit client in {@link WeatherClient.Builder#build()}.
 * SDK users do not interact with this class directly.
 * </p>
 *
 * @see CityWeather
 * @see WeatherModel
 * @see WeatherClient
 */
public class CityWeatherDeserializer implements JsonDeserializer<CityWeather> {
    /**
     * Deserializes a JSON element into a {@link CityWeather} object.
     * <p>
     * This method uses Jackson's {@link com.fasterxml.jackson.databind.ObjectMapper}
     * to first parse the JSON into a {@link WeatherModel}, then converts it to
     * the public {@link CityWeather} entity.
     * </p>
     *
     * @param json    the JSON element from the API response
     * @param type    the type of object to deserialize to (always {@link CityWeather})
     * @param jsonDeserializationContext the Gson context (unused in this implementation)
     * @return a {@link CityWeather} instance, or {@code null} if deserialization fails
     */
    @Override
    public CityWeather deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return mapper.readValue(json.toString(), WeatherModel.class).toCityWeather();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
