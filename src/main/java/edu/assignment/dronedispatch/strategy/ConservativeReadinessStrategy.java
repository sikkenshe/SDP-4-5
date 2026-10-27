
package edu.assignment.dronedispatch.strategy;

public class ConservativeReadinessStrategy
        implements DroneReadinessStrategy {

    @Override
    public boolean isReady(double batteryPercent) {
        return batteryPercent >= 60;
    }
}
