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
     * Includes fee_amount column.
     */
    public boolean addRoute(Route route) throws SQLException {
        String sql = "INSERT INTO routes (route_name, total_stops, distance, fee_amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getRouteName());
            ps.setInt(2, route.getTotalStops());
            ps.setDouble(3, route.getDistance());
            ps.setDouble(4, route.getFeeAmount());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieve all routes with fee_amount for table and dropdown population.
     */
    public List<Route> getAllRoutes() throws SQLException {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT id, route_name, total_stops, distance, fee_amount FROM routes ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                routes.add(new Route(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getInt("total_stops"),
                    rs.getDouble("distance"),
                    rs.getDouble("fee_amount")
                ));
            }
        }
        return routes;
    }

    /**
     * Retrieve a single route by ID.
     */
    public Route getRouteById(int id) throws SQLException {
        String sql = "SELECT id, route_name, total_stops, distance, fee_amount FROM routes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Route(
                        rs.getInt("id"),
                        rs.getString("route_name"),
                        rs.getInt("total_stops"),
                        rs.getDouble("distance"),
                        rs.getDouble("fee_amount")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieve a route by ID using an existing connection (for transaction support).
     */
    public Route getRouteById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, route_name, total_stops, distance, fee_amount FROM routes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Route(
                        rs.getInt("id"),
                        rs.getString("route_name"),
                        rs.getInt("total_stops"),
                        rs.getDouble("distance"),
                        rs.getDouble("fee_amount")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Update the fee amount for a specific route.
     */
    public boolean updateRouteFee(int routeId, double newFee) throws SQLException {
        String sql = "UPDATE routes SET fee_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newFee);
            ps.setInt(2, routeId);
            return ps.executeUpdate() > 0;
        }
    }
}

