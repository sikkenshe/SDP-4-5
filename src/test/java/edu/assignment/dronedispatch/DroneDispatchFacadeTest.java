package edu.assignment.dronedispatch;

import edu.assignment.dronedispatch.common.DispatchRequest;
import edu.assignment.dronedispatch.common.DispatchResult;
import edu.assignment.dronedispatch.common.FailureMode;
import edu.assignment.dronedispatch.facade.DispatchAdminFacade;
import edu.assignment.dronedispatch.facade.DroneDispatchFacade;
import edu.assignment.dronedispatch.persistence.DatabaseManager;
import edu.assignment.dronedispatch.persistence.DispatchProcess;
import edu.assignment.dronedispatch.persistence.DispatchProcessRepository;
import edu.assignment.dronedispatch.subsystem.a.RoutePlan;
import edu.assignment.dronedispatch.subsystem.a.RoutePlanner;
import edu.assignment.dronedispatch.subsystem.a.RouteRepository;
import edu.assignment.dronedispatch.subsystem.a.RouteStatus;
import edu.assignment.dronedispatch.subsystem.a.WeatherChecker;
import edu.assignment.dronedispatch.subsystem.b.BatteryManager;
import edu.assignment.dronedispatch.subsystem.b.DroneRepository;
import edu.assignment.dronedispatch.subsystem.b.DroneStatus;
import edu.assignment.dronedispatch.subsystem.b.DroneReservation;
import edu.assignment.dronedispatch.subsystem.b.PayloadSensor;
import edu.assignment.dronedispatch.subsystem.c.DeliveryConfirmation;
import edu.assignment.dronedispatch.subsystem.c.DeliveryRecord;
import edu.assignment.dronedispatch.subsystem.c.DeliveryStatus;
import edu.assignment.dronedispatch.subsystem.c.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DroneDispatchFacadeTest {
    @TempDir Path temp;
    private DatabaseManager db;
    private RouteRepository routes;
    private DroneRepository drones;
    private DeliveryRepository deliveries;
    private DispatchProcessRepository processes;
    private DroneDispatchFacade facade;
    private DispatchAdminFacade admin;

    @BeforeEach
    void setUp() { wire(temp.resolve("test.db")); }

    private void wire(Path path) {
        db = new DatabaseManager(path);
        routes = new RouteRepository(db);
        drones = new DroneRepository(db);
        deliveries = new DeliveryRepository(db);
        processes = new DispatchProcessRepository(db);
        facade = new DroneDispatchFacade(new RoutePlanner(), new WeatherChecker(), routes,
                new BatteryManager(), new PayloadSensor(), drones, new edu.assignment.dronedispatch.subsystem.c.LandingPadController(),
                new DeliveryConfirmation(), deliveries, processes);
        admin = new DispatchAdminFacade(processes, facade);
    }

    // Domain-specific test DSL: tests can read as "dispatch successfully to Campus North".
    private DispatchResult dispatchSuccessfullyTo(String destination) {
        return facade.dispatch(new DispatchRequest(destination, 1.2), FailureMode.NORMAL);
    }

    @Test
    void dispatchSuccessfullyToPersistsAllThreeStages() {
        DispatchResult result = dispatchSuccessfullyTo("Campus North");
        assertEquals("COMPLETED", result.status());
        assertEquals("COMPLETED", processes.findById(result.workflowId()).orElseThrow().status());
        assertEquals(1, routes.findAll().size());
        assertEquals(1, drones.findAll().size());
        assertEquals(1, deliveries.findAll().size());
        assertEquals(RouteStatus.PLANNED, routes.findAll().getFirst().status());
    }

    @Test
    void failureInBCompensatesOnlyPersistedA() {
        DispatchResult result = facade.dispatch(new DispatchRequest("Campus East", 1.0), FailureMode.FAIL_B);
        DispatchProcess process = processes.findById(result.workflowId()).orElseThrow();
        assertEquals("COMPENSATED", result.status());
        assertEquals("COMPENSATED", process.status());
        assertEquals(RouteStatus.CANCELLED, routes.findById(process.routeId()).orElseThrow().status());
        assertNull(process.droneReservationId());
        assertTrue(drones.findAll().isEmpty());
    }

    @Test
    void failureInCCompensatesBThenA() {
        DispatchResult result = facade.dispatch(new DispatchRequest("Campus West", 1.0), FailureMode.FAIL_C);
        DispatchProcess process = processes.findById(result.workflowId()).orElseThrow();
        assertEquals("COMPENSATED", result.status());
        assertEquals(RouteStatus.CANCELLED, routes.findById(process.routeId()).orElseThrow().status());
        assertEquals(DroneStatus.RELEASED, drones.findById(process.droneReservationId()).orElseThrow().status());
        assertNull(process.deliveryId());
    }

    @Test
    void committedStateSurvivesClosingAndReopeningDatabase() {
        DispatchResult result = dispatchSuccessfullyTo("Library");
        wire(temp.resolve("test.db")); // a new DatabaseManager/repositories simulate an application restart
        assertEquals("COMPLETED", processes.findById(result.workflowId()).orElseThrow().status());
        assertEquals(1, routes.findAll().size());
        assertEquals(1, drones.findAll().size());
        assertEquals(1, deliveries.findAll().size());
    }

    @Test
    void recoveryUsesPersistedACompletedMarkerAfterRestart() {
        String workflowId = UUID.randomUUID().toString();
        RoutePlan route = new RoutePlanner().plan("Interrupted delivery", new WeatherChecker());
        routes.save(route);
        processes.save(DispatchProcess.create(workflowId, "Interrupted delivery", 1.0, "STARTED"));
        DispatchProcess started = processes.findById(workflowId).orElseThrow();
        processes.update(started, "A_COMPLETED", route.id(), null, null, "simulated process interruption");

        wire(temp.resolve("test.db")); // simulate a fresh application process reading the same SQLite file
        assertEquals(1, admin.recoverIncompleteProcesses());
        assertEquals("COMPENSATED", processes.findById(workflowId).orElseThrow().status());
        assertEquals(RouteStatus.CANCELLED, routes.findById(route.id()).orElseThrow().status());
    }

    @Test
    void unsafeWeatherFailsBeforeAnyRouteIsCommitted() {
        DispatchResult result = facade.dispatch(new DispatchRequest("storm zone", 1.0), FailureMode.NORMAL);
        assertEquals("COMPENSATED", result.status());
        assertTrue(routes.findAll().isEmpty());
        assertTrue(drones.findAll().isEmpty());
    }

    @Test
    void invalidPayloadIsRejectedBeforeWorkflowCreation() {
        assertThrows(IllegalArgumentException.class, () -> new DispatchRequest("Campus", 7.0));
        assertTrue(processes.findAll().isEmpty());
    }

    @Test
    void closedLandingPadTriggersTwoStageCompensation() {
        DispatchResult result = facade.dispatch(new DispatchRequest("closedpad destination", 1.0), FailureMode.NORMAL);
        DispatchProcess process = processes.findById(result.workflowId()).orElseThrow();
        assertEquals("COMPENSATED", result.status());
        assertEquals(RouteStatus.CANCELLED, routes.findById(process.routeId()).orElseThrow().status());
        assertEquals(DroneStatus.RELEASED, drones.findById(process.droneReservationId()).orElseThrow().status());
    }

    @Test
    void routePlannerRejectsBlankDestination() {
        assertThrows(edu.assignment.dronedispatch.common.DispatchException.class,
                () -> new RoutePlanner().plan("   ", new WeatherChecker()));
        assertTrue(routes.findAll().isEmpty());
    }

    @Test
    void payloadSensorRejectsOverweightPackage() {
        assertThrows(edu.assignment.dronedispatch.common.DispatchException.class,
                () -> new PayloadSensor().ensurePayloadAllowed(5.1));
    }

    @Test
    void deliveryConfirmationRejectsBlankLandingPad() {
        assertThrows(edu.assignment.dronedispatch.common.DispatchException.class,
                () -> new DeliveryConfirmation().confirm("wf", "route", "drone", " "));
    }

    @Test
    void repositoriesReturnEmptyForUnknownIdsAndAllowLegalStatusUpdates() {
        assertTrue(routes.findById("missing-route").isEmpty());
        assertTrue(drones.findById("missing-drone").isEmpty());
        assertTrue(deliveries.findById("missing-delivery").isEmpty());
        RoutePlan route = new RoutePlanner().plan("Repository test", new WeatherChecker());
        routes.save(route);
        routes.updateStatus(route.id(), RouteStatus.CANCELLED);
        assertEquals(RouteStatus.CANCELLED, routes.findById(route.id()).orElseThrow().status());
        DroneReservation drone = new DroneReservation("wf-repo", 1.0, 92);
        drone.reserve();
        drones.save(drone);
        drones.updateStatus(drone.id(), DroneStatus.RELEASED);
        assertEquals(DroneStatus.RELEASED, drones.findById(drone.id()).orElseThrow().status());
        DeliveryRecord delivery = new DeliveryRecord("wf-repo", route.id(), drone.id(), "PAD-TEST");
        delivery.confirm();
        deliveries.save(delivery);
        deliveries.updateStatus(delivery.id(), DeliveryStatus.CANCELLED);
        assertEquals(DeliveryStatus.CANCELLED, deliveries.findById(delivery.id()).orElseThrow().status());
    }

    @Test
    void administrativeFacadeCanInspectAndManuallyCompensate() {
        DispatchResult result = dispatchSuccessfullyTo("Manual compensation destination");
        assertFalse(admin.inspectAll().isEmpty());
        admin.compensate(result.workflowId());
        DispatchProcess process = processes.findById(result.workflowId()).orElseThrow();
        assertEquals("COMPENSATED", process.status());
        assertEquals(RouteStatus.CANCELLED, routes.findById(process.routeId()).orElseThrow().status());
        assertEquals(DroneStatus.RELEASED, drones.findById(process.droneReservationId()).orElseThrow().status());
        assertEquals(DeliveryStatus.CANCELLED, deliveries.findById(process.deliveryId()).orElseThrow().status());
    }

    @Test
    void droneEntityEnforcesReservationAndReleaseTransitions() {
        DroneReservation drone = new DroneReservation("workflow-test", 1.0, 92);
        drone.reserve();
        assertEquals(DroneStatus.RESERVED, drone.status());
        drone.release();
        assertEquals(DroneStatus.RELEASED, drone.status());
        assertThrows(IllegalStateException.class, drone::reserve);
    }

    @Test
    void deliveryEntityEnforcesConfirmationTransition() {
        DeliveryRecord delivery = new DeliveryRecord("workflow-test", "route-test", "drone-test", "PAD-1");
        delivery.confirm();
        assertEquals(DeliveryStatus.CONFIRMED, delivery.status());
        assertThrows(IllegalStateException.class, delivery::confirm);
        delivery.cancel();
        assertEquals(DeliveryStatus.CANCELLED, delivery.status());
    }

    @Test
    void routeEntityRejectsIllegalTransition() {
        RoutePlan route = new RoutePlan("Test destination", 4.0);
        route.markPlanned();
        route.cancel();
        assertEquals(RouteStatus.CANCELLED, route.status());
        assertThrows(IllegalStateException.class, route::markPlanned);
    }
}
