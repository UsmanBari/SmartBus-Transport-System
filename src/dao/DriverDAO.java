package dao;

import config.DatabaseConnection;
import model.Driver;

import java.sql.*;

/**
 * Data Access Object for Driver entities.
 * All SQL is executed here using PreparedStatement only.
 */
public class DriverDAO {

    /**
     * Insert a new driver into the drivers table, linked by route_id.
     * Expects columns: driver_name, route_id
     */
    public boolean addDriver(Driver driver) throws SQLException {
        String sql = "INSERT INTO drivers (driver_name, route_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driver.getDriverName());
            ps.setInt(2, driver.getRouteId());
            return ps.executeUpdate() > 0;
        }
    }
}
