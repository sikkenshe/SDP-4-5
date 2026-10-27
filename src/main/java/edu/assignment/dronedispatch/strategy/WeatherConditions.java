
package edu.assignment.dronedispatch.strategy;

/**
 * Contains simulated weather measurements.
 */
public record WeatherConditions(
        boolean storm,
        double windSpeedKmh,
        int visibilityMeters
) {
}
