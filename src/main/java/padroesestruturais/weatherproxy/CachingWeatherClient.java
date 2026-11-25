package padroesestruturais.weatherproxy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CachingWeatherClient implements WeatherClient {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private static class Entry {
        private final Forecast forecast;
        private final Instant storedAt;

        private Entry(Forecast forecast, Instant storedAt) {
            this.forecast = forecast;
            this.storedAt = storedAt;
        }
    }

    private final WeatherClient realClient;
    private final Duration ttl;
    private final Clock clock;
    private final Map<String, Entry> cache = new HashMap<>();

    public CachingWeatherClient(WeatherClient realClient) {
        this(realClient, DEFAULT_TTL, Clock.systemUTC());
    }

    public CachingWeatherClient(WeatherClient realClient, Duration ttl, Clock clock) {
        this.realClient = realClient;
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public Forecast getForecast(String city) {
        String key = city.trim().toLowerCase();
        Instant now = clock.instant();

        Entry entry = cache.get(key);
        if (entry != null && now.isBefore(entry.storedAt.plus(ttl))) {
            return entry.forecast;
        }

        Forecast forecast = realClient.getForecast(city);
        cache.put(key, new Entry(forecast, now));
        return forecast;
    }
}
