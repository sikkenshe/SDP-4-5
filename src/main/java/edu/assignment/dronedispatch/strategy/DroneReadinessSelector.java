
package edu.assignment.dronedispatch.strategy;

public class DroneReadinessSelector {

    public DroneReadinessStrategy selectStrategy(
            double batteryPercent) {

        if (batteryPercent >= 60) {
            return new ConservativeReadinessStrategy();
        }

        if (batteryPercent >= 30) {
            return new StandardReadinessStrategy();
        }

        return new EmergencyReadinessStrategy();
    }
}
