package edu.assignment.dronedispatch.demo;

import edu.assignment.dronedispatch.persistence.DatabaseManager;
import edu.assignment.dronedispatch.subsystem.a.*;
import java.nio.file.Path;

/** Standalone entry point proving subsystem A can be used without either other subsystem or a facade. */
public final class RouteSubsystemDemo {
    private RouteSubsystemDemo() {
    }

    public static void main(String[] args) {
        DatabaseManager database = new DatabaseManager(Path.of("data/route-subsystem-demo.db"));
        RoutePlanner planner = new RoutePlanner();
        WeatherChecker weather = new WeatherChecker();
        RouteRepository repository = new RouteRepository(database);
        RoutePlan route = planner.plan("Standalone A demo destination", weather);
        repository.save(route);
        System.out.println("Subsystem A saved route " + route.id() + " with status " + route.status());
        System.out.println("Persisted routes: " + repository.findAll().size());
    }
}
