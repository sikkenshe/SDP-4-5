
package edu.assignment.dronedispatch.strategy;

public class FixedWeatherDataSource implements WeatherDataSource {

    private final WeatherConditions conditions;

    public FixedWeatherDataSource(WeatherConditions conditions) {
        this.conditions = conditions;
    }

    @Override
    public WeatherConditions getConditions(String destination) {
        return conditions;
    }
}
