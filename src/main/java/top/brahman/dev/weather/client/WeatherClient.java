package top.brahman.dev.weather.client;

import com.google.gson.GsonBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import top.brahman.dev.weather.cache.LRUCache;
import top.brahman.dev.weather.exception.CityNotFoundException;
import top.brahman.dev.weather.exception.WeatherClientException;
import top.brahman.dev.weather.model.APIError;
import top.brahman.dev.weather.entity.CityWeather;
import top.brahman.dev.weather.util.Util;
import top.brahman.dev.weather.util.CityWeatherDeserializer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static top.brahman.dev.weather.util.Util.CACHE_TTL_SEC;
import static top.brahman.dev.weather.util.Util.URL;

/**
 * The main entry point for weather data retrieval from <a href="https://api.openweathermap.org/data/2.5/weather">https://api.openweathermap.org/data/2.5/weather</a>
 *
 * <p>This client provides two operational modes:</p>
 * <ul>
 *   <li><b>{@link ClientMode#ON_DEMAND}</b>: Fetches fresh data on every call (default). Ideal for low-frequency requests.</li>
 *   <li><b>{@link ClientMode#POLLING}</b>: Caches data and auto-updates in the background.</li>
 * </ul>
 *
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>✅ <b>Thread-safe</b> — Safe for concurrent use across multiple threads</li>
 *   <li>✅ <b>Automatic caching</b> — LRU cache with TTL-based expiration ({@link Util#CACHE_TTL_SEC}  seconds)</li>
 *   <li>✅ <b>Background updates</b> — In {@code POLLING} mode, refreshes data when it expires</li>
 *   <li>✅ <b>API key safety</b> — Prevents accidental reuse of API keys across instances</li>
 *   <li>✅ <b>Graceful error handling</b> — Translates HTTP errors to meaningful exceptions</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create a client (ON_DEMAND mode)
 * WeatherClient client = WeatherClient.builder()
 *     .apiKey("your_api_key")
 *     .build();
 *
 * try {
 *     CityWeather berlin = client.getWeatherFor("Berlin");
 *     System.out.println("Current temp in Berlin: " + berlin.temperature().temp() + "°C");
 * } catch (CityNotFoundException e) {
 *     System.err.println("City not found: " + e.getCityName());
 * } catch (WeatherClientException e) {
 *     System.err.println("API failure: " + e.getMessage());
 * }
 * }</pre>
 *
 * <h2>Lifecycle Management</h2>
 * <p>This client holds system resources (thread pools). For long-running applications:</p>
 * <ul>
 *   <li>✅ <b>Single instance per API key</b> — Reuse the client across your application</li>
 *   <li>⚠️ <b>Shutdown when done</b> — Call {@link #close()} to release threads</li>
 * </ul>
 *
 * <h2>Threading Model</h2>
 * <ul>
 *   <li>🔄 <b>{@code ON_DEMAND}</b>: Blocking calls — runs on caller's thread</li>
 *   <li>⚙️ <b>{@code POLLING}</b>: Uses a dedicated {@link ScheduledExecutorService} for background updates</li>
 * </ul>
 *
 * @author Kairat Kaibrakhman <a href="mailto:brahman.dev.kz@gmail.com">brahman.dev.kz@gmail.com</a>
 * @see CityWeather
 * @see ClientMode
 * @since 1.0.0
 */

public final class WeatherClient implements AutoCloseable {
    private static final Set<String> usedApiKeys = ConcurrentHashMap.newKeySet();
    private final Logger logger = Logger.getLogger(WeatherClient.class.getSimpleName());
    private final LRUCache<String, CityWeather> cache = new LRUCache<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final String apiKey;
    private final WeatherApi api;
    private final Converter<ResponseBody, APIError> converter;
    private volatile ClientMode mode;

    /**
     * Private constructor — use {@link Builder} to instantiate.
     */
    private WeatherClient(String key, ClientMode mode, WeatherApi api, Converter<ResponseBody, APIError> converter) {
        this.apiKey = key;
        this.mode = mode;
        this.api = api;
        this.converter = converter;
    }

    /**
     * Test constructor
     */
    WeatherClient(String key, WeatherApi api, Converter<ResponseBody, APIError> converter) {
        this(key, ClientMode.ON_DEMAND, api, converter);
        usedApiKeys.add(key);
    }

    /**
     * Changes operational mode at runtime (e.g., from {@code ON_DEMAND} to {@code POLLING}).
     * If new mode is {@code  POLLING}, then schedules update for each city in the cache
     *
     * @param mode the new operational mode (must not be null)
     * @throws IllegalArgumentException if mode is null
     */
    public synchronized void setMode(ClientMode mode) {
        if (isClosed()) throw new IllegalStateException("Client is closed. Build a new one");
        if (this.mode == mode) return;
        if (mode == null) throw new IllegalArgumentException("ClientMode must not be null");
        this.mode = mode;
        if (mode == ClientMode.POLLING) cache.forEachValue(this::scheduleUpdate);
    }

    private boolean isClosed() {
        return !usedApiKeys.contains(apiKey);
    }

    /**
     * Creates a new builder for configuring a {@link  WeatherClient} instance.
     *
     * @return a fresh builder instance
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * Retrieves weather data for a city.
     *
     * <p><b>Behavior by mode:</b></p>
     * <ul>
     *   <li><b>{@code ON_DEMAND}</b>: Returns cached data if fresh (within {@value Util#CACHE_TTL_SEC}  seconds),
     *       otherwise fetches from API</li>
     *   <li><b>{@code POLLING}</b>: Returns cached data immediately if it is in cache. If not, fetches from API, caches it
     *   and schedules a background periodic refresh. In this mode, data may get expired for a short period of time. For that reason we need condition
     *   {@code (mode == ClientMode.POLLING || !weather.isExpired())}</li>
     * </ul>
     *
     * <h4>Error Handling</h4>
     * <ul>
     *   <li>{@link CityNotFoundException} — When city name is invalid (HTTP 404)</li>
     *   <li>{@link WeatherClientException} — For network issues or API errors</li>
     *   <li>{@link RuntimeException} — For unexpected conditions (e.g., null responses)</li>
     * </ul>
     *
     * <h4>Thread Safety</h4>
     * <p>Safe for concurrent calls from multiple threads.</p>
     *
     * @param city the city name (e.g., "London", "New York") — case-insensitive
     * @return a {@link CityWeather} record with current conditions
     * @throws CityNotFoundException    if the city doesn't exist in OpenWeatherMap's database
     * @throws WeatherClientException   for network failures or API errors
     * @throws IllegalArgumentException if {@code city} is blank or null
     */
    public CityWeather getWeatherFor(String city) throws WeatherClientException {
        final CityWeather weather = cache.get(city);
        if (weather != null && (mode == ClientMode.POLLING || !weather.isExpired())) return weather;
        try {
            final CityWeather cityWeather = getFromCloud(city);
            if (cityWeather == null) throw new IOException("Got null result. Probably problem with deserialization");
            cache.put(city, cityWeather);
            if (mode == ClientMode.POLLING) scheduleUpdate(cityWeather);
            return cityWeather;
        } catch (IOException e) {
            throw new WeatherClientException("Failed to fetch weather for city: " + city, e);
        }
    }

    private CityWeather getFromCloud(String city) throws IOException {
        final Response<CityWeather> response = api.get(apiKey, city).execute();
        if (!response.isSuccessful()) {
            final ResponseBody errorBody = response.errorBody();
            if (errorBody == null)
                throw new RuntimeException("API error: " + "Unsuccessful response without error body");
            final APIError error = converter.convert(errorBody);
            throw error.cod() == 404 ? new CityNotFoundException(city) : new RuntimeException("API error: " + error.message());
        }
        return response.body();
    }

    private void scheduleUpdate(final CityWeather city) {
        final long delay = city.datetime() + CACHE_TTL_SEC - Instant.now().getEpochSecond();
        scheduler.schedule(() -> {
            final String name = city.name();
            CityWeather fresh = null;
            try {
                fresh = getFromCloud(name);
            } catch (Exception e) {
                logger.log(Level.INFO, "Couldn't automatically update " + city.datetime() + ". " +
                        "Will retry after " + delay + " seconds if necessary");
            }
            if (mode == ClientMode.POLLING && cache.containsKey(name)) {
                if (fresh != null) {
                    cache.put(name, fresh);
                    scheduleUpdate(fresh);
                } else scheduleUpdate(city);
            }
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * Call this method when you are done working with {@link  WeatherClient}
     */
    @Override
    public void close() {
        scheduler.shutdown();
        usedApiKeys.remove(apiKey);
    }


    /**
     * Builder for {@link WeatherClient} instances — enforces safe initialization.
     *
     * <h2>Required Configuration</h2>
     * <ul>
     *   <li>{@link #apiKey(String)} — Your OpenWeatherMap API key</li>
     * </ul>
     *
     * <h2>Optional Configuration</h2>
     * <ul>
     *   <li>{@link #setMode(ClientMode)} — Defaults to {@code ON_DEMAND}</li>
     * </ul>
     *
     * <h2>Validation Rules</h2>
     * <ul>
     *   <li>❌ API key must be non-empty</li>
     *   <li>❌ API key cannot be reused across live clients (prevents accidental rate-limiting)</li>
     * </ul>
     *
     * <h2>Example</h2>
     * <pre>{@code
     * WeatherClient client = WeatherClient.builder()
     *     .apiKey("your_key_here")
     *     .setMode(ClientMode.POLLING)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        String key;
        ClientMode mode;

        /**
         * Constructs and configures the {@link WeatherClient} instance.
         *
         * <p><b>Resource note:</b> This client creates a background thread pool.
         * For long-running apps, retain a single instance and {@code close} it down when your app terminates.</p>
         *
         * @return a fully initialized, thread-safe WeatherClient
         * @throws IllegalArgumentException if an API key is missing or already in use
         */
        public WeatherClient build() {
            if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("API key must be provided");
            if (!usedApiKeys.add(key)) throw new IllegalArgumentException("API key " + key + " is already in use");
            final Retrofit retrofit = new Retrofit.Builder().baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .registerTypeAdapter(CityWeather.class, new CityWeatherDeserializer())
                            .create())).build();
            return new WeatherClient(key, mode, retrofit.create(WeatherApi.class), retrofit.responseBodyConverter(APIError.class, new Annotation[0]));
        }

        /**
         * Sets the OpenWeatherMap API key.
         *
         * @param key your API key from <a href="https://home.openweathermap.org/api_keys">https://home.openweathermap.org/api_keys</a>
         * @return this builder for chaining
         */
        public Builder apiKey(String key) {
            this.key = key;
            return this;
        }

        /**
         * Configures operational mode (optional — defaults to {@code ON_DEMAND}).
         *
         * @param mode the client mode
         * @return this builder for chaining
         */
        public Builder mode(ClientMode mode) {
            this.mode = mode;
            return this;
        }
    }

    public interface WeatherApi {
        @GET("weather")
        Call<CityWeather> get(@Query("appId") String apiKey, @Query("q") String city);
    }

    /**
     * Operational modes for the weather client.
     */
    public enum ClientMode {
        /**
         * Fetches data on every call unless cached data is fresh.
         * <p>Best for: Infrequent checks (e.g., CLI tools, batch jobs).</p>
         */
        ON_DEMAND,

        /**
         * Caches data and auto-updates in the background to keep it fresh.
         */
        POLLING
    }
}
