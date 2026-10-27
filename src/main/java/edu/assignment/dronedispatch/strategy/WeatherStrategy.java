
package edu.assignment.dronedispatch.strategy;

/**
 * Common contract for different weather safety algorithms.
 */
public interface WeatherStrategy {

    boolean isSafe(String destination);
}
