
package edu.assignment.dronedispatch.strategy;

public class WeatherStrategySelector {

    private final WeatherDataSource weatherDataSource;

    public WeatherStrategySelector(WeatherDataSource weatherDataSource) {
        this.weatherDataSource = weatherDataSource;
    }

    public WeatherStrategy selectStrategy(String destination) {
        WeatherConditions weather =
                weatherDataSource.getConditions(destination);

        if (weather.storm()) {
            return new SafeWeatherStrategy(weatherDataSource);
        }

        if (weather.windSpeedKmh() > 30) {
            return new WindAwareWeatherStrategy(
                    weatherDataSource, 30
            );
        }

        if (weather.visibilityMeters() < 1000) {
            return new VisibilityWeatherStrategy(
                    weatherDataSource, 1000
            );
        }

        return new SafeWeatherStrategy(weatherDataSource);
    }
}
