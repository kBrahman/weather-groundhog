package top.brahman.dev.weather.model;

/**
 * Represents an error response from the OpenWeatherMap API.
 * <p>
 * This record encapsulates the error information returned by the API when
 * a request fails. It is used internally by the SDK to parse error responses
 * and convert them into appropriate exceptions for the client.
 * </p>
 *
 * @param cod     the HTTP status code from the API response
 * @param message the error message describing what went wrong
 */
public record APIError(int cod, String message) {}
