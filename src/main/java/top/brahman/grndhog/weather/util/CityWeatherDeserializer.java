package top.brahman.grndhog.weather.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.*;
import top.brahman.grndhog.weather.entity.CityWeather;
import top.brahman.grndhog.weather.model.WeatherModel;

import java.lang.reflect.Type;

import static top.brahman.grndhog.weather.util.Util.mapper;

public class CityWeatherDeserializer implements JsonDeserializer<CityWeather> {
    @Override
    public CityWeather deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return mapper.readValue(json.toString(), WeatherModel.class).toCityWeather();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
