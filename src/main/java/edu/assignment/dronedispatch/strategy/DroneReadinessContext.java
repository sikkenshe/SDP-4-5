
package edu.assignment.dronedispatch.strategy;

public class DroneReadinessContext {

    private DroneReadinessStrategy strategy;

    public DroneReadinessContext(DroneReadinessStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DroneReadinessStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isReady(double batteryPercent) {
        return strategy.isReady(batteryPercent);
    }
}
