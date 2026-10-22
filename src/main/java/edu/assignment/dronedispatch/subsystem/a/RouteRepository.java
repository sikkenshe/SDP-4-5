package edu.assignment.dronedispatch.subsystem.a;

import edu.assignment.dronedispatch.persistence.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RouteRepository {
    private final DatabaseManager database;
    public RouteRepository(DatabaseManager database) { this.database = database; }
    public void save(RoutePlan route) {
        String sql="INSERT INTO route_plans(id,destination,distance_km,status,created_at) VALUES(?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET destination=excluded.destination,distance_km=excluded.distance_km,status=excluded.status";
        try(Connection c=database.openConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,route.id()); p.setString(2,route.destination()); p.setDouble(3,route.distanceKm()); p.setString(4,route.status().name()); p.setString(5,route.createdAt()); p.executeUpdate();
        } catch(SQLException e){throw new IllegalStateException("Could not save route",e);}
    }
    public Optional<RoutePlan> findById(String id) {
        try(Connection c=database.openConnection(); PreparedStatement p=c.prepareStatement("SELECT * FROM route_plans WHERE id=?")) {
            p.setString(1,id); try(ResultSet r=p.executeQuery()){ if(!r.next()) return Optional.empty(); return Optional.of(new RoutePlan(r.getString("id"),r.getString("destination"),r.getDouble("distance_km"),RouteStatus.valueOf(r.getString("status")),r.getString("created_at"))); }
        } catch(SQLException e){throw new IllegalStateException("Could not read route",e);}
    }
    public void updateStatus(String id, RouteStatus status) {
        RoutePlan route = findById(id).orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        if (status == RouteStatus.CANCELLED) route.cancel();
        else if (status == RouteStatus.PLANNED && route.status() == RouteStatus.NEW) route.markPlanned();
        else if (route.status() != status) throw new IllegalStateException("Illegal route status transition: " + route.status() + " -> " + status);
        save(route);
    }
    public List<RoutePlan> findAll(){List<RoutePlan> out=new ArrayList<>();try(Connection c=database.openConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM route_plans ORDER BY created_at DESC")){while(r.next())out.add(new RoutePlan(r.getString("id"),r.getString("destination"),r.getDouble("distance_km"),RouteStatus.valueOf(r.getString("status")),r.getString("created_at")));return out;}catch(SQLException e){throw new IllegalStateException("Could not list routes",e);}}
}
