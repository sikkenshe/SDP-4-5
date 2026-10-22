package edu.assignment.dronedispatch.subsystem.c;
import java.time.Instant;import java.util.UUID;

public final class DeliveryRecord {
 private final String id,workflowId,routeId,droneReservationId,landingPad,createdAt;private DeliveryStatus status;
 public DeliveryRecord(String workflowId,String routeId,String droneReservationId,String landingPad){this(UUID.randomUUID().toString(),workflowId,routeId,droneReservationId,landingPad,DeliveryStatus.NEW,Instant.now().toString());}
 public DeliveryRecord(String id,String workflowId,String routeId,String droneReservationId,String landingPad,DeliveryStatus status,String createdAt){this.id=id;this.workflowId=workflowId;this.routeId=routeId;this.droneReservationId=droneReservationId;this.landingPad=landingPad;this.status=status;this.createdAt=createdAt;}
 public void confirm(){if(status!=DeliveryStatus.NEW)throw new IllegalStateException("Only NEW delivery can be confirmed");status=DeliveryStatus.CONFIRMED;}
 public void cancel(){if(status==DeliveryStatus.CANCELLED)return;if(status!=DeliveryStatus.NEW&&status!=DeliveryStatus.CONFIRMED)throw new IllegalStateException("Illegal delivery transition");status=DeliveryStatus.CANCELLED;}
 public String id(){return id;}public String workflowId(){return workflowId;}public String routeId(){return routeId;}public String droneReservationId(){return droneReservationId;}public String landingPad(){return landingPad;}public DeliveryStatus status(){return status;}public String createdAt(){return createdAt;}
}
