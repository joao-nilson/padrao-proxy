package padroesestruturais.weatherproxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class CachingWeatherClientTest {

    private static class CountingWeatherClient implements WeatherClient {
        private int calls = 0;

        @Override
        public Forecast getForecast(String city) {
            calls++;
            return new Forecast(city, 20.0 + calls, "Call " + calls);
        }
    }

    private static class FakeClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private CountingWeatherClient api;
    private FakeClock clock;
    private WeatherClient client;

    @BeforeEach
    void setUp() {
        api = new CountingWeatherClient();
        clock = new FakeClock();
        client = new CachingWeatherClient(api, Duration.ofMinutes(10), clock);
    }

    @Test
    void shouldCallRealApiOnFirstRequest() {
        Forecast forecast = client.getForecast("Juiz de Fora");

        assertEquals("Juiz de Fora", forecast.getCity());
        assertEquals(1, api.calls);
    }

    @Test
    void shouldReturnCachedForecastWithinTenMinutes() {
        Forecast first = client.getForecast("Juiz de Fora");
        clock.advance(Duration.ofMinutes(9).plusSeconds(59));
        Forecast second = client.getForecast("Juiz de Fora");

        assertSame(first, second);
        assertEquals(1, api.calls);
    }

    @Test
    void shouldCallRealApiAgainAfterTenMinutes() {
        Forecast first = client.getForecast("Juiz de Fora");
        clock.advance(Duration.ofMinutes(10));
        Forecast second = client.getForecast("Juiz de Fora");

        assertNotSame(first, second);
        assertEquals(2, api.calls);
    }

    @Test
    void shouldCacheEachCitySeparately() {
        client.getForecast("Juiz de Fora");
        client.getForecast("Rio de Janeiro");
        client.getForecast("Juiz de Fora");

        assertEquals(2, api.calls);
    }

    @Test
    void shouldIgnoreCaseAndSurroundingSpacesInCityName() {
        Forecast first = client.getForecast("Juiz de Fora");
        Forecast second = client.getForecast("  juiz DE fora ");

        assertSame(first, second);
        assertEquals(1, api.calls);
    }

    @Test
    void shouldCacheWhenUsingDefaultConstructor() {
        WeatherClient defaultClient = new CachingWeatherClient(api);

        Forecast first = defaultClient.getForecast("Juiz de Fora");
        Forecast second = defaultClient.getForecast("Juiz de Fora");

        assertSame(first, second);
        assertEquals(1, api.calls);
    }
}
