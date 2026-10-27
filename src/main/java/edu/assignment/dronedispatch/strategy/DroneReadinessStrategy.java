
package edu.assignment.dronedispatch.strategy;

public interface DroneReadinessStrategy {
    boolean isReady(double batteryPercent);
}
