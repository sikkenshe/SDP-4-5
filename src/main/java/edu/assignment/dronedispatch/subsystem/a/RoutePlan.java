package edu.assignment.dronedispatch.subsystem.a;

import java.time.Instant;
import java.util.UUID;

public final class RoutePlan {
    private final String id;
    private final String destination;
    private final double distanceKm;
    private RouteStatus status;
    private final String createdAt;

    public RoutePlan(String destination, double distanceKm) {
        this(UUID.randomUUID().toString(), destination, distanceKm, RouteStatus.NEW, Instant.now().toString());
    }
    public RoutePlan(String id, String destination, double distanceKm, RouteStatus status, String createdAt) {
        this.id=id; this.destination=destination; this.distanceKm=distanceKm; this.status=status; this.createdAt=createdAt;
    }
    public void markPlanned() { if (status != RouteStatus.NEW) throw new IllegalStateException("Only NEW route can be planned"); status=RouteStatus.PLANNED; }
    public void cancel() { if (status == RouteStatus.CANCELLED) return; if (status != RouteStatus.PLANNED && status != RouteStatus.NEW) throw new IllegalStateException("Illegal route transition"); status=RouteStatus.CANCELLED; }
    public String id(){return id;} public String destination(){return destination;} public double distanceKm(){return distanceKm;} public RouteStatus status(){return status;} public String createdAt(){return createdAt;}
}
