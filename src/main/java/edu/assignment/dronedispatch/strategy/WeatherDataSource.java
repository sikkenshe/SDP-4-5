package edu.assignment.dronedispatch.strategy;

/**
 * Provides weather information for a destination.
 */
public interface WeatherDataSource {

    WeatherConditions getConditions(String destination);
}