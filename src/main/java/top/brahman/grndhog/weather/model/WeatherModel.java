package top.brahman.grndhog.weather.model;

import top.brahman.grndhog.weather.entity.CityWeather;

import java.util.List;

public record WeatherModel(List<CityWeather.Weather> weather, Main main, int visibility, CityWeather.Wind wind,
                           long dt,
                           CityWeather.Sys sys, int timezone, String name) {
    private record Main(double temp, double feels_like) {}

    public CityWeather toCityWeather() {
        return new CityWeather(weather.getFirst(), new CityWeather.Temperature(main.temp, main.feels_like), visibility, wind, dt, sys, timezone,
                name);
    }
}
