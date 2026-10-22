package edu.assignment.dronedispatch.facade;

import edu.assignment.dronedispatch.common.*;
import edu.assignment.dronedispatch.persistence.*;
import edu.assignment.dronedispatch.subsystem.a.*;
import edu.assignment.dronedispatch.subsystem.b.*;
import edu.assignment.dronedispatch.subsystem.c.*;
import java.util.Optional;
import java.util.UUID;

/** Coordinates independent subsystems and persists every workflow boundary. */
public final class DroneDispatchFacade {

 private final RoutePlanner routePlanner;
 private final WeatherChecker weatherChecker;
 private final RouteRepository routeRepository;

 private final BatteryManager batteryManager;
 private final PayloadSensor payloadSensor;
 private final DroneRepository droneRepository;

 private final LandingPadController landingPadController;
 private final DeliveryConfirmation deliveryConfirmation;
 private final DeliveryRepository deliveryRepository;

 private final DispatchProcessRepository processRepository;

 public DroneDispatchFacade(
         RoutePlanner rp,
         WeatherChecker wc,
         RouteRepository rr,
         BatteryManager bm,
         PayloadSensor ps,
         DroneRepository dr,
         LandingPadController lp,
         DeliveryConfirmation dc,
         DeliveryRepository drep,
         DispatchProcessRepository pr
 ) {
  routePlanner = rp;
  weatherChecker = wc;
  routeRepository = rr;
  batteryManager = bm;
  payloadSensor = ps;
  droneRepository = dr;
  landingPadController = lp;
  deliveryConfirmation = dc;
  deliveryRepository = drep;
  processRepository = pr;
 }

 public DispatchResult dispatch(DispatchRequest request, FailureMode mode) {
  String id = UUID.randomUUID().toString();
  DispatchProcess process = DispatchProcess.create(
          id,
          request.destination(),
          request.payloadKg(),
          "STARTED"
  );
  processRepository.save(process);

  String routeId = null;
  String droneId = null;
  String deliveryId = null;

  try {
   RoutePlan route = routePlanner.plan(request.destination(), weatherChecker);
   routeRepository.save(route);
   routeId = route.id();
   process = with(process, "A_COMPLETED", routeId, null, null, null);

   if (mode == FailureMode.FAIL_B) {
    throw new DispatchException("Demonstration failure in subsystem B");
   }

   batteryManager.ensureFlightReady();
   payloadSensor.ensurePayloadAllowed(request.payloadKg());
   DroneReservation drone = new DroneReservation(
           id,
           request.payloadKg(),
           batteryManager.availableBatteryPercent()
   );
   drone.reserve();
   droneRepository.save(drone);
   droneId = drone.id();
   process = with(process, "B_COMPLETED", routeId, droneId, null, null);

   if (mode == FailureMode.FAIL_C) {
    throw new DispatchException("Demonstration failure in subsystem C");
   }

   String pad = landingPadController.reserveLandingPad(request.destination());
   DeliveryRecord delivery = deliveryConfirmation.confirm(id, routeId, droneId, pad);
   deliveryRepository.save(delivery);
   deliveryId = delivery.id();
   process = with(process, "COMPLETED", routeId, droneId, deliveryId, null);


   if (mode == FailureMode.FAIL_C_AB) {
    throw new DispatchException("Demonstrate A and B works with failure in subsystem C");
   }

   RoutePlan route1 = routePlanner.plan(request.destination(), weatherChecker);
   routeRepository.save(route);
   routeId = route.id();   routeRepository.save(route);

   batteryManager.ensureFlightReady();
   payloadSensor.ensurePayloadAllowed(request.payloadKg());

   DroneReservation drone1 = new DroneReservation(
           id,
           request.payloadKg(),
           batteryManager.availableBatteryPercent()
   );

   return new DispatchResult(id, "COMPLETED", "Delivery confirmed at " + pad);
  } catch (RuntimeException failure) {
   DispatchProcess persisted = processRepository.findById(id).orElse(process);

   System.out.println("[ERROR] " + failure.getMessage());
   System.out.println("[COMPENSATION] Starting rollback...");

   try {
    compensatePersisted(persisted, failure.getMessage());
    System.out.println("[COMPENSATION] Drone reservation B released (if reserved).");
    System.out.println("[COMPENSATION] Route A cancelled (if created).");
    System.out.println("[COMPENSATION] Workflow compensated successfully.");
   } catch (RuntimeException compensationFailure) {
    throw new DispatchException(
            "Dispatch failed and compensation needs recovery. Workflow ID: " + id,
            compensationFailure
    );
   }

   return new DispatchResult(
           id,
           "COMPENSATED",
           "Dispatch failed: " + failure.getMessage()
                   + ". Persisted actions compensated."
   );
  }
 }

 private DispatchProcess with(
         DispatchProcess old,
         String status,
         String routeId,
         String droneId,
         String deliveryId,
         String error
 ) {
  processRepository.update(old, status, routeId, droneId, deliveryId, error);
  return processRepository.findById(old.id()).orElseThrow();
 }

 /** Uses persisted IDs/status as the source of truth; compensation order is C/B/A reverse order. */
 public void compensatePersisted(DispatchProcess process, String reason) {
  DispatchProcess current = processRepository.findById(process.id())
          .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

  processRepository.update(
          current,
          "COMPENSATING",
          current.routeId(),
          current.droneReservationId(),
          current.deliveryId(),
          reason
  );

  // If C was persisted, mark it cancelled first; normally C failure occurs before a record is saved.
  if (current.deliveryId() != null) {
   deliveryRepository.updateStatus(current.deliveryId(), DeliveryStatus.CANCELLED);
  }

  if (current.droneReservationId() != null) {
   Optional<DroneReservation> drone = droneRepository.findById(current.droneReservationId());
   if (drone.isPresent() && drone.get().status() == DroneStatus.RESERVED) {
    droneRepository.updateStatus(current.droneReservationId(), DroneStatus.RELEASED);
   }
  }

  if (current.routeId() != null) {
   Optional<RoutePlan> route = routeRepository.findById(current.routeId());
   if (route.isPresent() && route.get().status() != RouteStatus.CANCELLED) {
    routeRepository.updateStatus(current.routeId(), RouteStatus.CANCELLED);
   }
  }

  DispatchProcess latest = processRepository.findById(current.id()).orElseThrow();
  processRepository.update(
          latest,
          "COMPENSATED",
          latest.routeId(),
          latest.droneReservationId(),
          latest.deliveryId(),
          reason
  );
 }

 public DispatchResult dispatch(DispatchRequest request) {
  return dispatch(request, FailureMode.NORMAL);
 }
}