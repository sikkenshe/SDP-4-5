
package edu.assignment.dronedispatch.subsystem.a;

import edu.assignment.dronedispatch.common.DispatchException;
import edu.assignment.dronedispatch.strategy.WeatherCheckContext;
import edu.assignment.dronedispatch.strategy.WeatherConditions;
import edu.assignment.dronedispatch.strategy.WeatherDataSource;
import edu.assignment.dronedispatch.strategy.SafeWeatherStrategy;
import edu.assignment.dronedispatch.strategy.WeatherStrategy;

public final class WeatherChecker {

    private final WeatherCheckContext context;

    public WeatherChecker() {
        WeatherDataSource dataSource = new WeatherDataSource() {
            @Override
            public WeatherConditions getConditions(String destination) {
                boolean storm = destination.toLowerCase().contains("storm");

                return new WeatherConditions(
                        storm,
                        10,
                        5000
                );
            }
        };

        WeatherStrategy strategy = new SafeWeatherStrategy(dataSource);
        this.context = new WeatherCheckContext(strategy);
    }

    public WeatherChecker(WeatherCheckContext context) {
        this.context = context;
    }

    public void ensureSafe(String destination) {
        if (!context.isSafe(destination)) {
            throw new DispatchException(
                    "Weather check failed: unsafe weather at destination"
            );
        }
    }
}
