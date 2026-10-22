package edu.assignment.dronedispatch.subsystem.b;
import edu.assignment.dronedispatch.persistence.DatabaseManager;
import java.sql.*;import java.util.*;

public final class DroneRepository {
    private final DatabaseManager database; public DroneRepository(DatabaseManager database){this.database=database;}
    public void save(DroneReservation d){String sql="INSERT INTO drone_reservations(id,workflow_id,payload_kg,battery_percent,status,created_at) VALUES(?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status";try(Connection c=database.openConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,d.id());p.setString(2,d.workflowId());p.setDouble(3,d.payloadKg());p.setInt(4,d.batteryPercent());p.setString(5,d.status().name());p.setString(6,d.createdAt());p.executeUpdate();}catch(SQLException e){throw new IllegalStateException("Could not save drone reservation",e);}}
    public Optional<DroneReservation> findById(String id){try(Connection c=database.openConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM drone_reservations WHERE id=?")){p.setString(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return Optional.empty();return Optional.of(map(r));}}catch(SQLException e){throw new IllegalStateException("Could not read drone reservation",e);}}
    private DroneReservation map(ResultSet r)throws SQLException{return new DroneReservation(r.getString("id"),r.getString("workflow_id"),r.getDouble("payload_kg"),r.getInt("battery_percent"),DroneStatus.valueOf(r.getString("status")),r.getString("created_at"));}
    public void updateStatus(String id,DroneStatus status){
        DroneReservation drone=findById(id).orElseThrow(()->new IllegalArgumentException("Drone reservation not found: "+id));
        if(status==DroneStatus.RELEASED)drone.release();
        else if(status==DroneStatus.CANCELLED)drone.cancel();
        else if(status==DroneStatus.RESERVED&&drone.status()==DroneStatus.NEW)drone.reserve();
        else if(drone.status()!=status)throw new IllegalStateException("Illegal drone status transition: "+drone.status()+" -> "+status);
        save(drone);
    }
    public List<DroneReservation> findAll(){List<DroneReservation> out=new ArrayList<>();try(Connection c=database.openConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM drone_reservations ORDER BY created_at DESC")){while(r.next())out.add(map(r));return out;}catch(SQLException e){throw new IllegalStateException("Could not list drone reservations",e);}}
}
