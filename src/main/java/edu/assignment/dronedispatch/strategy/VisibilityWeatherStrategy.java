
package edu.assignment.dronedispatch.strategy;

public class VisibilityWeatherStrategy implements WeatherStrategy {

    private final WeatherDataSource weatherDataSource;
    private final int minVisibilityMeters;

    public VisibilityWeatherStrategy(
            WeatherDataSource weatherDataSource,
            int minVisibilityMeters) {
        this.weatherDataSource = weatherDataSource;
        this.minVisibilityMeters = minVisibilityMeters;
    }

    @Override
    public boolean isSafe(String destination) {
        WeatherConditions weather =
                weatherDataSource.getConditions(destination);

        return !weather.storm()
                && weather.visibilityMeters() >= minVisibilityMeters;
    }
}
