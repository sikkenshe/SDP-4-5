package edu.assignment.dronedispatch.subsystem.c;
import edu.assignment.dronedispatch.common.DispatchException;

public final class DeliveryConfirmation {
 public DeliveryRecord confirm(String workflowId,String routeId,String droneReservationId,String landingPad){if(landingPad==null||landingPad.isBlank())throw new DispatchException("A valid landing pad is required");DeliveryRecord record=new DeliveryRecord(workflowId,routeId,droneReservationId,landingPad);record.confirm();return record;}
}
