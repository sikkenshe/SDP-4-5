package edu.assignment.dronedispatch.subsystem.c;
import edu.assignment.dronedispatch.persistence.DatabaseManager;import java.sql.*;import java.util.*;

public final class DeliveryRepository {
 private final DatabaseManager database;public DeliveryRepository(DatabaseManager database){this.database=database;}
 public void save(DeliveryRecord d){String sql="INSERT INTO delivery_records(id,workflow_id,route_id,drone_reservation_id,landing_pad,status,created_at) VALUES(?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status";try(Connection c=database.openConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,d.id());p.setString(2,d.workflowId());p.setString(3,d.routeId());p.setString(4,d.droneReservationId());p.setString(5,d.landingPad());p.setString(6,d.status().name());p.setString(7,d.createdAt());p.executeUpdate();}catch(SQLException e){throw new IllegalStateException("Could not save delivery",e);}}
 public Optional<DeliveryRecord> findById(String id){try(Connection c=database.openConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM delivery_records WHERE id=?")){p.setString(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return Optional.empty();return Optional.of(map(r));}}catch(SQLException e){throw new IllegalStateException("Could not read delivery",e);}}
 private DeliveryRecord map(ResultSet r)throws SQLException{return new DeliveryRecord(r.getString("id"),r.getString("workflow_id"),r.getString("route_id"),r.getString("drone_reservation_id"),r.getString("landing_pad"),DeliveryStatus.valueOf(r.getString("status")),r.getString("created_at"));}
 public void updateStatus(String id,DeliveryStatus status){
  DeliveryRecord delivery=findById(id).orElseThrow(()->new IllegalArgumentException("Delivery not found: "+id));
  if(status==DeliveryStatus.CANCELLED)delivery.cancel();
  else if(status==DeliveryStatus.CONFIRMED&&delivery.status()==DeliveryStatus.NEW)delivery.confirm();
  else if(delivery.status()!=status)throw new IllegalStateException("Illegal delivery status transition: "+delivery.status()+" -> "+status);
  save(delivery);
 }
 public List<DeliveryRecord> findAll(){List<DeliveryRecord> out=new ArrayList<>();try(Connection c=database.openConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM delivery_records ORDER BY created_at DESC")){while(r.next())out.add(map(r));return out;}catch(SQLException e){throw new IllegalStateException("Could not list deliveries",e);}}
}
