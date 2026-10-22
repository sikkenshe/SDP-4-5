package edu.assignment.dronedispatch.cli;

import edu.assignment.dronedispatch.common.*;
import edu.assignment.dronedispatch.facade.*;
import edu.assignment.dronedispatch.persistence.*;
import edu.assignment.dronedispatch.subsystem.a.*;
import edu.assignment.dronedispatch.subsystem.b.*;
import edu.assignment.dronedispatch.subsystem.c.*;
import java.nio.file.Path;
import java.util.Scanner;

public final class Main {

 private Main() {}

 public static void main(String[] args) {
  DatabaseManager db = new DatabaseManager(Path.of("data", "dispatch.db"));
  RouteRepository routes = new RouteRepository(db);
  DroneRepository drones = new DroneRepository(db);
  DeliveryRepository deliveries = new DeliveryRepository(db);
  DispatchProcessRepository processes = new DispatchProcessRepository(db);

  DroneDispatchFacade facade = new DroneDispatchFacade(
          new RoutePlanner(),
          new WeatherChecker(),
          routes,
          new BatteryManager(),
          new PayloadSensor(),
          drones,
          new LandingPadController(),
          new DeliveryConfirmation(),
          deliveries,
          processes
  );

  DispatchAdminFacade admin = new DispatchAdminFacade(processes, facade);

  try (Scanner in = new Scanner(System.in)) {
   boolean running = true;
   while (running) {
    menu();
    String choice = in.nextLine().trim();
    try {
     switch (choice) {
      case "1" -> runDispatch(in, facade, FailureMode.NORMAL);

      case "2" -> runDispatch(in, facade, FailureMode.FAIL_B);
      case "3" -> runDispatch(in, facade, FailureMode.FAIL_C);
      case "4" -> {
       System.out.println("Persisted workflows:");
       admin.inspectAll().forEach(System.out::println);
       System.out.println("Routes: " + routes.findAll());
       System.out.println("Drone reservations: " + drones.findAll());
       System.out.println("Deliveries: " + deliveries.findAll());
      }
      case "5" -> System.out.println("Recovered workflows: " + admin.recoverIncompleteProcesses());
      case "6" -> {
       try {
        facade.dispatch(new DispatchRequest("", 1.0));
       } catch (IllegalArgumentException e) {
        System.out.println("Expected validation error: " + e.getMessage());
       }
      }
      case "7" -> {
       System.out.print("Workflow ID: ");
       admin.compensate(in.nextLine().trim());
       System.out.println("Manual compensation completed.");
      }
      case "8" -> runDispatch(in, facade, FailureMode.FAIL_C_AB);
      case "0" -> running = false;
      default -> System.out.println("Unknown option.");
     }
    } catch (Exception e) {
     System.out.println("Operation failed: " + e.getMessage());
    }
   }
  }
 }

 private static void runDispatch(Scanner in, DroneDispatchFacade facade, FailureMode mode) {
  System.out.print("Destination (use 'storm' for weather failure or 'closedpad' for landing failure): ");
  String destination = in.nextLine();
  System.out.print("Payload kg (0.1-5.0): ");
  double payload = Double.parseDouble(in.nextLine().trim());
  DispatchResult result = facade.dispatch(new DispatchRequest(destination, payload), mode);
  System.out.println(result);
 }

 private static void menu() {
  System.out.println("\n--- DELIVERY DRONE DISPATCH ---");
  System.out.println("1. Dispatch successfully");
  System.out.println("2. Demonstrate B failure (compensate A)");
  System.out.println("3. Demonstrate C failure (compensate B then A)");
  System.out.println("4. Inspect persisted state");
  System.out.println("5. Recover incomplete persisted workflows");
  System.out.println("6. Demonstrate invalid input");
  System.out.println("7. Manual compensation by workflow ID");
  System.out.println("8. demonstrate a and b");
  System.out.println("0. Exit");
  System.out.print("Choose: ");
 }
}