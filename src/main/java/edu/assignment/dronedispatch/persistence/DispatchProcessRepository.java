package edu.assignment.dronedispatch.persistence;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class DispatchProcessRepository {

 private final DatabaseManager database;

 public DispatchProcessRepository(DatabaseManager database) {
  this.database = database;
 }

 public void save(DispatchProcess p) {
  String sql = "INSERT INTO dispatch_process(id, destination, payload_kg, status, route_id, drone_reservation_id, delivery_id, last_error, updated_at) "
          + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) "
          + "ON CONFLICT(id) DO UPDATE SET "
          + "status = excluded.status, "
          + "route_id = excluded.route_id, "
          + "drone_reservation_id = excluded.drone_reservation_id, "
          + "delivery_id = excluded.delivery_id, "
          + "last_error = excluded.last_error, "
          + "updated_at = excluded.updated_at";

  try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql)) {
   s.setString(1, p.id());
   s.setString(2, p.destination());
   s.setDouble(3, p.payloadKg());
   s.setString(4, p.status());
   s.setString(5, p.routeId());
   s.setString(6, p.droneReservationId());
   s.setString(7, p.deliveryId());
   s.setString(8, p.lastError());
   s.setString(9, Instant.now().toString());
   s.executeUpdate();
  } catch (SQLException e) {
   throw new IllegalStateException("Could not save workflow", e);
  }
 }

 public Optional<DispatchProcess> findById(String id) {
  try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM dispatch_process WHERE id = ?")) {
   s.setString(1, id);
   try (ResultSet r = s.executeQuery()) {
    return r.next() ? Optional.of(map(r)) : Optional.empty();
   }
  } catch (SQLException e) {
   throw new IllegalStateException("Could not read workflow", e);
  }
 }

 private DispatchProcess map(ResultSet r) throws SQLException {
  return new DispatchProcess(
          r.getString("id"),
          r.getString("destination"),
          r.getDouble("payload_kg"),
          r.getString("status"),
          r.getString("route_id"),
          r.getString("drone_reservation_id"),
          r.getString("delivery_id"),
          r.getString("last_error"),
          r.getString("updated_at")
  );
 }

 public void update(DispatchProcess old, String status, String routeId, String droneId, String deliveryId, String error) {
  save(new DispatchProcess(
          old.id(),
          old.destination(),
          old.payloadKg(),
          status,
          routeId,
          droneId,
          deliveryId,
          error,
          Instant.now().toString()
  ));
 }

 public List<DispatchProcess> findAll() {
  List<DispatchProcess> out = new ArrayList<>();
  try (Connection c = database.openConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM dispatch_process ORDER BY updated_at DESC")) {
   while (r.next()) {
    out.add(map(r));
   }
   return out;
  } catch (SQLException e) {
   throw new IllegalStateException("Could not list workflows", e);
  }
 }

 public List<DispatchProcess> findIncomplete() {
  List<DispatchProcess> out = new ArrayList<>();
  String sql = "SELECT * FROM dispatch_process WHERE status IN ('STARTED', 'A_COMPLETED', 'B_COMPLETED', 'COMPENSATING') ORDER BY updated_at";
  try (Connection c = database.openConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
   while (r.next()) {
    out.add(map(r));
   }
   return out;
  } catch (SQLException e) {
   throw new IllegalStateException("Could not find incomplete workflows", e);
  }
 }
}