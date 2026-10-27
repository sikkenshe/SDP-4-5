
package edu.assignment.dronedispatch.strategy;

public class WeatherCheckContext {

    private WeatherStrategy strategy;

    public WeatherCheckContext(WeatherStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(WeatherStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isSafe(String destination) {
        return strategy.isSafe(destination);
    }
}
