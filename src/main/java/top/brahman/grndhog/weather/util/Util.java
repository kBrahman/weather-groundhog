package top.brahman.grndhog.weather.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
    public static final int CACHE_TTL_SEC = 600;
    public static final int CACHE_CAPACITY = 10;
    public static final String URL = "https://api.openweathermap.org/data/2.5/";
    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
