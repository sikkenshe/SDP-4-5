package edu.assignment.dronedispatch.subsystem.b;
import edu.assignment.dronedispatch.common.DispatchException;

public final class PayloadSensor {
    public void ensurePayloadAllowed(double payloadKg){if(!Double.isFinite(payloadKg)||payloadKg<=0||payloadKg>5.0)throw new DispatchException("Payload sensor rejected payload: maximum is 5 kg");}
}
