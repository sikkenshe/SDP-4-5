
package edu.assignment.dronedispatch.cli;

import edu.assignment.dronedispatch.strategy.*;

public final class DroneReadinessDemo {

    private DroneReadinessDemo() {}

    public static void run() {
        System.out.println("\n--- DRONE READINESS STRATEGY DEMO ---");

        double battery = 40;

        DroneReadinessSelector selector =
                new DroneReadinessSelector();

        DroneReadinessStrategy selected =
                selector.selectStrategy(battery);

        DroneReadinessContext context =
                new DroneReadinessContext(selected);

        System.out.println("Battery: " + battery + "%");
        System.out.println("Automatic strategy: "
                + selected.getClass().getSimpleName());
        System.out.println("Ready: " + context.isReady(battery));

        context.setStrategy(new ConservativeReadinessStrategy());

        System.out.println("After switching to Conservative:");
        System.out.println("Ready: " + context.isReady(battery));
    }
}
