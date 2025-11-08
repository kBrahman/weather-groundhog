package top.brahman.grndhog.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import top.brahman.grndhog.weather.entity.CityWeather;

import static top.brahman.grndhog.weather.util.Util.mapper;

public class TestUtil {

    public static CityWeather constructZocca() throws JsonProcessingException {
        return mapper.readValue(ZOCCA_JSON, CityWeather.class);
    }

    static final String ZOCCA_JSON = """
            {
              "weather": {
                "main": "Clouds",
                "description": "scattered clouds"
              },
              "temperature": {
                "temp": 269.6,
                "feels_like": 267.57
              },
              "visibility": 10000,
              "wind": {
                "speed": 1.38
              },
              "datetime": 1675744800,
              "sys": {
                "sunrise": 1675751262,
                "sunset": 1675787560
              },
              "timezone": 3600,
              "name": "Zocca"
            }
            """;

    public static CityWeather withName(CityWeather original, String newName) {
        return new CityWeather(
                original.weather(),
                original.temperature(),
                original.visibility(),
                original.wind(),
                original.datetime(),
                original.sys(),
                original.timezone(),
                newName
        );
    }
}
