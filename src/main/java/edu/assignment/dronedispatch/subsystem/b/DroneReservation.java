package edu.assignment.dronedispatch.subsystem.b;

import java.time.Instant;
import java.util.UUID;

public final class DroneReservation {
    private final String id, workflowId, createdAt; private final double payloadKg; private final int batteryPercent; private DroneStatus status;
    public DroneReservation(String workflowId,double payloadKg,int batteryPercent){this(UUID.randomUUID().toString(),workflowId,payloadKg,batteryPercent,DroneStatus.NEW,Instant.now().toString());}
    public DroneReservation(String id,String workflowId,double payloadKg,int batteryPercent,DroneStatus status,String createdAt){this.id=id;this.workflowId=workflowId;this.payloadKg=payloadKg;this.batteryPercent=batteryPercent;this.status=status;this.createdAt=createdAt;}
    public void reserve(){if(status!=DroneStatus.NEW)throw new IllegalStateException("Only a NEW drone reservation can be reserved");status=DroneStatus.RESERVED;}
    public void release(){if(status==DroneStatus.RELEASED)return;if(status!=DroneStatus.RESERVED)throw new IllegalStateException("Only a RESERVED drone can be released");status=DroneStatus.RELEASED;}
    public void cancel(){if(status==DroneStatus.CANCELLED)return;if(status!=DroneStatus.NEW&&status!=DroneStatus.RESERVED)throw new IllegalStateException("Illegal drone status transition");status=DroneStatus.CANCELLED;}
    public String id(){return id;} public String workflowId(){return workflowId;} public double payloadKg(){return payloadKg;} public int batteryPercent(){return batteryPercent;} public DroneStatus status(){return status;} public String createdAt(){return createdAt;}
}
