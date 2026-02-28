package dao;

import config.DatabaseConnection;
import model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Route entities.
 * All SQL is executed here using PreparedStatement only.
 */
public class RouteDAO {

    /**
     * Insert a new route into the routes table.
     * Expects columns: route_name, total_stops, distance
     */
    public boolean addRoute(Route route) throws SQLException {
        String sql = "INSERT INTO routes (route_name, total_stops, distance) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getRouteName());
            ps.setInt(2, route.getTotalStops());
            ps.setDouble(3, route.getDistance());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieve all routes (id + name) for dropdown population.
     */
    public List<Route> getAllRoutes() throws SQLException {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT id, route_name, total_stops, distance FROM routes ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                routes.add(new Route(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getInt("total_stops"),
                    rs.getDouble("distance")
                ));
            }
        }
        return routes;
    }
}
