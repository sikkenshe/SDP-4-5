package edu.assignment.dronedispatch.common;

public record DispatchRequest(String destination, double payloadKg) {
    public DispatchRequest {
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("Destination must not be blank");
        if (!Double.isFinite(payloadKg) || payloadKg <= 0 || payloadKg > 5.0)
            throw new IllegalArgumentException("Payload must be greater than 0 and no more than 5 kg");
    }
}
