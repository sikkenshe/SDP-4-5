
package edu.assignment.dronedispatch.cli;

import edu.assignment.dronedispatch.strategy.*;

public final class WeatherStrategyDemo {

    private WeatherStrategyDemo() {}

    public static void run() {
        // The same weather data is used by all strategies.
        WeatherConditions conditions =
                new WeatherConditions(false, 35, 800);

        WeatherDataSource dataSource =
                new FixedWeatherDataSource(conditions);

        // Create three different strategies.
        WeatherStrategy safeStrategy =
                new SafeWeatherStrategy(dataSource);

        WeatherStrategy windStrategy =
                new WindAwareWeatherStrategy(dataSource, 30);

        WeatherStrategy visibilityStrategy =
                new VisibilityWeatherStrategy(dataSource, 1000);

        // Create a context with the first strategy.
        WeatherCheckContext context =
                new WeatherCheckContext(safeStrategy);

        String destination = "Test destination";

        System.out.println("\n--- STRATEGY PATTERN DEMO ---");

        // Run the same input with each strategy.
        System.out.println(
                "Safe strategy: " + context.isSafe(destination));

        context.setStrategy(windStrategy);
        System.out.println(
                "Wind strategy: " + context.isSafe(destination));

        context.setStrategy(visibilityStrategy);
        System.out.println(
                "Visibility strategy: " + context.isSafe(destination));
    }
}
