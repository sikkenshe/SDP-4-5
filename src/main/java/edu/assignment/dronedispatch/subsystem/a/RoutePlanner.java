package edu.assignment.dronedispatch.subsystem.a;

import edu.assignment.dronedispatch.common.DispatchException;

public final class RoutePlanner {
    public RoutePlan plan(String destination, WeatherChecker weatherChecker) {
        if (destination == null || destination.isBlank()) throw new DispatchException("Destination is required");
        weatherChecker.ensureSafe(destination);
        double distance = Math.max(1.0, Math.min(50.0, destination.trim().length() * 1.7));
        RoutePlan route = new RoutePlan(destination.trim(), distance);
        route.markPlanned();
        return route;
    }
}
