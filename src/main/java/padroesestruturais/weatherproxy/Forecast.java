package padroesestruturais.weatherproxy;

public class Forecast {

    private final String city;
    private final double temperatureCelsius;
    private final String summary;

    public Forecast(String city, double temperatureCelsius, String summary) {
        this.city = city;
        this.temperatureCelsius = temperatureCelsius;
        this.summary = summary;
    }

    public String getCity() {
        return city;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public String getSummary() {
        return summary;
    }
}
