package edu.assignment.dronedispatch.subsystem.c;
import edu.assignment.dronedispatch.common.DispatchException;

public final class LandingPadController {
 public String reserveLandingPad(String destination){if(destination.toLowerCase().contains("closedpad"))throw new DispatchException("Landing pad unavailable at destination");return "PAD-"+Integer.toUnsignedString(destination.hashCode(),36).toUpperCase();}
}
