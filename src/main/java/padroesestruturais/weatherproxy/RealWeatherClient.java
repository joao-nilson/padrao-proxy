package padroesestruturais.weatherproxy;

public class RealWeatherClient implements WeatherClient {

    @Override
    public Forecast getForecast(String city) {
        return new Forecast(city, 25.0, "Sunny");
    }
}
