package edu.assignment.dronedispatch.persistence;

import java.time.Instant;

public record DispatchProcess(
        String id,
        String destination,
        double payloadKg,
        String status,
        String routeId,
        String droneReservationId,
        String deliveryId,
        String lastError,
        String updatedAt
) {
 public static DispatchProcess create(String id, String destination, double payloadKg, String status) {
  return new DispatchProcess(
          id,
          destination,
          payloadKg,
          status,
          null,
          null,
          null,
          null,
          Instant.now().toString()
  );
 }
}