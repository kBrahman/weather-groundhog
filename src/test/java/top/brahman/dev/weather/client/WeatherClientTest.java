package top.brahman.dev.weather.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import top.brahman.dev.weather.CityWeatherBuilder;
import top.brahman.dev.weather.TestUtil;
import top.brahman.dev.weather.exception.CityNotFoundException;
import top.brahman.dev.weather.exception.WeatherClientException;
import top.brahman.dev.weather.model.APIError;
import top.brahman.dev.weather.entity.CityWeather;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static top.brahman.dev.weather.util.Util.CACHE_CAPACITY;
import static top.brahman.dev.weather.util.Util.CACHE_TTL_SEC;
import static top.brahman.dev.weather.util.Util.URL;

@ExtendWith(MockitoExtension.class)
public class WeatherClientTest {
    private static final CityWeather zoccaTestEntity;

    static {
        try {
            zoccaTestEntity = TestUtil.constructZocca();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse test JSON", e);
        }
    }

    @Mock
    private WeatherClient.WeatherApi api;
    @Mock
    private Call<CityWeather> mockCall;
    private WeatherClient client;

    @BeforeEach
    void setUp() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create()).baseUrl(URL).build();
        Converter<ResponseBody, APIError> converter = retrofit.responseBodyConverter(APIError.class, new Annotation[0]);
        String apiKey = UUID.randomUUID().toString();
        client = new WeatherClient(apiKey, api, converter);
    }

    @AfterEach
    void closeClient() {
        client.close();
    }

    @Test
    void builder_throw_whenApiKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (var _ = WeatherClient.builder().build()) {}
        });
    }

    @Test
    void builder_throw_whenMultipleInstancesWithSameApiKey() {
        final String key = "test_1";
        final WeatherClient c = WeatherClient.builder().apiKey(key).build();
        assertThrows(IllegalArgumentException.class, () -> {
            try (var _ = WeatherClient.builder().apiKey(key).build()) {}
        });
        c.close();
    }

    @Test
    void closeAndResueSameApiKey() {
        final String key = "test_2";
        try (var _ = WeatherClient.builder().apiKey(key).build()) {}
        try (var _ = WeatherClient.builder().apiKey(key).build()) {}
    }

    @Test
    void getWeatherForCity_success() throws Exception {
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(zoccaTestEntity));
        final CityWeather res = client.getWeatherFor("Zocca");
        assertEquals(zoccaTestEntity, res);
    }

    @Test
    void getWeatherForCity_networkError() throws IOException {
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));
        WeatherClientException exception = assertThrows(WeatherClientException.class, () -> client.getWeatherFor("Zocca"));
        assertInstanceOf(WeatherClientException.class, exception);
        assertInstanceOf(IOException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("Failed to fetch weather"));
    }


    @Test
    void getWeatherForCity_cityNotFoundError() throws IOException {
        String badName = "badName";
        when(api.get(any(), eq(badName))).thenReturn(mockCall);
        String errorJson = "{\"cod\":\"404\",\"message\":\"city not found\"}";
        ResponseBody errorBody = ResponseBody.create(errorJson, MediaType.get("application/json"));
        Response<CityWeather> errorResponse = Response.error(404, errorBody);
        when(mockCall.execute()).thenReturn(errorResponse);
        assertThrows(CityNotFoundException.class, () -> client.getWeatherFor(badName));
    }

    @Test
    void reuseCachedValue_whenNotExpired() throws Exception {
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        final CityWeather currentZocca = CityWeatherBuilder.from(zoccaTestEntity).datetime(Instant.now().getEpochSecond()).build();
        final CityWeather zoccaFromFuture = CityWeatherBuilder.from(currentZocca)
                .datetime(currentZocca.datetime() + new Random().nextInt(CACHE_TTL_SEC) + 1).build();
        when(mockCall.execute()).thenReturn(Response.success(currentZocca)).thenReturn(Response.success(zoccaFromFuture));
        final CityWeather cityWeather1 = client.getWeatherFor("Zocca");
        final CityWeather cityWeather2 = client.getWeatherFor("Zocca");
        assertEquals(cityWeather1, cityWeather2);
    }

    @Test
    void cacheNewValue_whenExpired() throws Exception {
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        final CityWeather expiredZocca = CityWeatherBuilder.from(zoccaTestEntity)
                .datetime(Instant.now().getEpochSecond() - CACHE_TTL_SEC - 1).build();
        final CityWeather freshZocca = CityWeatherBuilder.from(expiredZocca).datetime(Instant.now().getEpochSecond()).build();
        when(mockCall.execute()).thenReturn(Response.success(expiredZocca)).thenReturn(Response.success(freshZocca));
        final CityWeather cityWeather1 = client.getWeatherFor("Zocca");
        final CityWeather cityWeather2 = client.getWeatherFor("Zocca");
        assertNotEquals(cityWeather1, cityWeather2);
    }

    @Test
    void removeUnexpiredLRUValueFromCache_whenCapacityFull() throws Exception {
        final long now = Instant.now().getEpochSecond();
        final CityWeather oldest = CityWeatherBuilder.from(zoccaTestEntity).datetime(now).build();
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(oldest));
        final CityWeather shouldBecomeLRU = client.getWeatherFor("Zocca");
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            final String name = String.valueOf((char) ('A' + i));
            final CityWeather nextCity = CityWeatherBuilder.from(zoccaTestEntity).name(name).datetime(now + i + 1).build();
            when(api.get(any(), eq(name))).thenReturn(mockCall);
            when(mockCall.execute()).thenReturn(Response.success(nextCity));
            client.getWeatherFor(name);
        }
        final CityWeather newest = CityWeatherBuilder.from(zoccaTestEntity).datetime(now + CACHE_CAPACITY + 1).build();
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(newest));
        final CityWeather fresh = client.getWeatherFor("Zocca");
        assertNotEquals(shouldBecomeLRU, fresh);
    }

    @Test
    void updateWeatherAutomatically_whenInPollingMode() throws Exception {
        client.setMode(WeatherClient.ClientMode.POLLING);
        final long now = Instant.now().getEpochSecond();
        final CityWeather expired = CityWeatherBuilder.from(zoccaTestEntity).datetime(now - CACHE_TTL_SEC).build();
        final CityWeather usedByAutomaticUpdate = CityWeatherBuilder.from(expired).datetime(now).build();
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(expired)).thenReturn(Response.success(usedByAutomaticUpdate));
        CityWeather exp = client.getWeatherFor("Zocca");
        Awaitility.await().atMost(Duration.ofMillis(101)).untilAsserted(() -> {
            client.setMode(WeatherClient.ClientMode.ON_DEMAND);
            final CityWeather curr = client.getWeatherFor("Zocca");
            assertNotEquals(exp, curr);
        });
    }

    @Test
    void neverThrow_wenNotClosed() throws Exception {
        client.setMode(WeatherClient.ClientMode.POLLING);
        when(api.get(any(), eq("Zocca"))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(zoccaTestEntity));
        client.getWeatherFor("Zocca");
    }
}
