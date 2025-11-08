package top.brahman.grndhog.weather.exception;

import java.io.IOException;

public class WeatherClientException extends IOException {
    public WeatherClientException(String message) {
        super(message);
    }

    public WeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
