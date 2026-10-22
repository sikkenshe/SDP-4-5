package edu.assignment.dronedispatch.subsystem.b;
import edu.assignment.dronedispatch.common.DispatchException;

public final class BatteryManager {
    public int availableBatteryPercent(){return 92;}
    public void ensureFlightReady(){if(availableBatteryPercent()<60)throw new DispatchException("Drone battery is too low");}
}
