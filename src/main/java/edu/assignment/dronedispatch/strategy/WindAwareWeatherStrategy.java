
package edu.assignment.dronedispatch.strategy;

public class WindAwareWeatherStrategy implements WeatherStrategy {

    private final WeatherDataSource weatherDataSource;
    private final double maxWindSpeedKmh;

    public WindAwareWeatherStrategy(
            WeatherDataSource weatherDataSource,
            double maxWindSpeedKmh) {
        this.weatherDataSource = weatherDataSource;
        this.maxWindSpeedKmh = maxWindSpeedKmh;
    }

    @Override
    public boolean isSafe(String destination) {
        WeatherConditions weather =
                weatherDataSource.getConditions(destination);

        return !weather.storm()
                && weather.windSpeedKmh() <= maxWindSpeedKmh;
    }
}
