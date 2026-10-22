package edu.assignment.dronedispatch.demo;

import edu.assignment.dronedispatch.persistence.DatabaseManager;
import edu.assignment.dronedispatch.subsystem.b.*;
import java.nio.file.Path;
import java.util.UUID;

/** Standalone entry point proving subsystem B can be used without either other subsystem or a facade. */
public final class DroneSubsystemDemo {
    private DroneSubsystemDemo() { }

    public static void main(String[] args) {
        DatabaseManager database = new DatabaseManager(Path.of("data/drone-subsystem-demo.db"));
        BatteryManager battery = new BatteryManager();
        PayloadSensor payloadSensor = new PayloadSensor();
        DroneRepository repository = new DroneRepository(database);
        battery.ensureFlightReady();
        payloadSensor.ensurePayloadAllowed(1.0);
        DroneReservation reservation = new DroneReservation(UUID.randomUUID().toString(), 1.0, battery.availableBatteryPercent());
        reservation.reserve();
        repository.save(reservation);
        System.out.println("Subsystem B saved reservation " + reservation.id() + " with status " + reservation.status());
        System.out.println("Persisted reservations: " + repository.findAll().size());
    }
}
