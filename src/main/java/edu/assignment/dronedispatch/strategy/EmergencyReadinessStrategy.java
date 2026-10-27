
package edu.assignment.dronedispatch.strategy;

public class EmergencyReadinessStrategy
        implements DroneReadinessStrategy {

    @Override
    public boolean isReady(double batteryPercent) {
        return batteryPercent >= 15;
    }
}
