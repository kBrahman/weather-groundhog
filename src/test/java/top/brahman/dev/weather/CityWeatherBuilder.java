package top.brahman.dev.weather;


import top.brahman.dev.weather.entity.CityWeather;

/**
 * Test-only builder for {@link CityWeather}.
 *
 * <p><b>Never used in production code</b> — exists solely to create modified
 * copies of weather data during testing, without polluting the public SDK API.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * CityWeather testWeather = CityWeatherBuilder.from(realWeather)
 *     .name("Punxsutawney")
 *     .datetime(1700000000L)
 *     .build();
 * </pre>
 */
public final class CityWeatherBuilder {
    private CityWeather.Weather weather;
    private CityWeather.Temperature temperature;
    private int visibility;
    private CityWeather.Wind wind;
    private long datetime;
    private CityWeather.Sys sys;
    private int timezone;
    private String name;

    private CityWeatherBuilder() {
    }

    /**
     * Creates a builder initialized with values from the given CityWeather.
     */
    public static CityWeatherBuilder from(CityWeather original) {
        CityWeatherBuilder builder = new CityWeatherBuilder();
        builder.weather = original.weather();
        builder.temperature = original.temperature();
        builder.visibility = original.visibility();
        builder.wind = original.wind();
        builder.datetime = original.datetime();
        builder.sys = original.sys();
        builder.timezone = original.timezone();
        builder.name = original.name();
        return builder;
    }

    /**
     * Sets a new city name (e.g., for testing renamed locations).
     */
    public CityWeatherBuilder name(String newName) {
        this.name = newName;
        return this; // Enable chaining
    }

    /**
     * Sets a new timestamp in UNIX seconds (e.g., for cache expiration tests).
     */
    public CityWeatherBuilder datetime(long newDatetime) {
        this.datetime = newDatetime;
        return this; // Enable chaining
    }

    /**
     * Builds a new {@link CityWeather} instance with the current builder state.
     *
     * @return a new immutable CityWeather record
     */
    public CityWeather build() {
        return new CityWeather(
                weather,
                temperature,
                visibility,
                wind,
                datetime,
                sys,
                timezone,
                name
        );
    }
}