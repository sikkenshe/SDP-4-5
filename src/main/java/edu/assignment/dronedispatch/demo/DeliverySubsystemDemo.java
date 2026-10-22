package edu.assignment.dronedispatch.demo;

import edu.assignment.dronedispatch.persistence.DatabaseManager;
import edu.assignment.dronedispatch.subsystem.c.*;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Standalone entry point proving subsystem C can be used
 * without either other subsystem or a facade.
 */
public final class DeliverySubsystemDemo {

    private DeliverySubsystemDemo() {}

    public static void main(String[] args) {
        DatabaseManager database = new DatabaseManager(Path.of("data/delivery-subsystem-demo.db"));
        LandingPadController pads = new LandingPadController();
        DeliveryConfirmation confirmation = new DeliveryConfirmation();
        DeliveryRepository repository = new DeliveryRepository(database);

        String workflowId = UUID.randomUUID().toString();
        String routeId = "standalone-route";
        String droneId = "standalone-drone";

        String pad = pads.reserveLandingPad("Standalone C demo destination");
        DeliveryRecord delivery = confirmation.confirm(workflowId, routeId, droneId, pad);
        repository.save(delivery);

        System.out.println("Subsystem C saved delivery " + delivery.id() + " with status " + delivery.status());
        System.out.println("Persisted deliveries: " + repository.findAll().size());
    }
}