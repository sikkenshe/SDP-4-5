package edu.assignment.dronedispatch.subsystem.a;

import edu.assignment.dronedispatch.common.DispatchException;

/** Deterministic simulated weather rule, independent of other subsystems. */
public final class WeatherChecker {
    public void ensureSafe(String destination) {
        if (destination.toLowerCase().contains("storm")) throw new DispatchException("Weather check failed: storm conditions at destination");
    }
}
