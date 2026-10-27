
package edu.assignment.dronedispatch.strategy;

public class SafeWeatherStrategy implements WeatherStrategy {

    private final WeatherDataSource weatherDataSource;

    public SafeWeatherStrategy(WeatherDataSource weatherDataSource) {
        this.weatherDataSource = weatherDataSource;
    }

    @Override
    public boolean isSafe(String destination) {
        WeatherConditions weather =
                weatherDataSource.getConditions(destination);

        return !weather.storm();
    }
}
