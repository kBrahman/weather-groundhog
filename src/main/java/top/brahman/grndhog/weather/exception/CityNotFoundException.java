package top.brahman.grndhog.weather.exception;

public class CityNotFoundException extends Exception {
    public CityNotFoundException(String city) {
        super(city + " not found");
    }
}
