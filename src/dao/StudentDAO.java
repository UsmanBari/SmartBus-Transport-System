package dao;

import config.DatabaseConnection;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entities.
 * Handles authentication, registration, duplicate checking, approval, and bus assignment.
 */
public class StudentDAO {

    // ═══════════════════════════════════════════════════════════════════════
    //  AUTHENTICATION (student_users table)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Register a new student account (student_users table).
     */
    public boolean registerAccount(String name, String roll, String password) throws SQLException {
        String sql = "INSERT INTO student_users (student_name, student_roll, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, roll);
            ps.setString(3, password);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Check if a roll number already has an account in student_users.
     */
    public boolean isAccountExists(String roll) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_users WHERE student_roll = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Authenticate a student by roll number and password.
     * Returns the student name if valid, null otherwise.
     */
    public String authenticate(String roll, String password) throws SQLException {
        String sql = "SELECT student_name FROM student_users WHERE student_roll = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("student_name");
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TRANSPORT REGISTRATION (students table)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Insert a new student transport registration with status PENDING.
     */
    public boolean registerStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_name, student_roll, selected_route_id, status) "
                   + "VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentName());
            ps.setString(2, student.getStudentRoll());
            ps.setInt(3, student.getSelectedRouteId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Check if a roll number already exists in the students (transport) table.
     */
    public boolean isRollExists(String roll) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE student_roll = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Retrieve a student transport record by roll number.
     */
    public Student getStudentByRoll(String roll) throws SQLException {
        String sql = "SELECT s.student_id, s.student_name, s.student_roll, "
                   + "s.selected_route_id, s.assigned_route_id, s.bus_number, "
                   + "s.status, s.registration_date, r.route_name "
                   + "FROM students s "
                   + "LEFT JOIN routes r ON s.selected_route_id = r.id "
                   + "WHERE s.student_roll = ? "
                   + "ORDER BY s.registration_date DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student st = new Student(
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_roll"),
                        rs.getInt("selected_route_id"),
                        rs.getInt("assigned_route_id"),
                        rs.getInt("bus_number"),
                        rs.getString("status"),
                        rs.getTimestamp("registration_date")
                    );
                    st.setSelectedRouteName(rs.getString("route_name"));
                    return st;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ADMIN QUERIES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get all students with PENDING status, joined with route name.
     */
    public List<Student> getAllPendingStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, s.student_roll, "
                   + "s.selected_route_id, s.assigned_route_id, s.bus_number, "
                   + "s.status, s.registration_date, r.route_name "
                   + "FROM students s "
                   + "LEFT JOIN routes r ON s.selected_route_id = r.id "
                   + "WHERE s.status = 'PENDING' "
                   + "ORDER BY s.registration_date DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student st = new Student(
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("student_roll"),
                    rs.getInt("selected_route_id"),
                    rs.getInt("assigned_route_id"),
                    rs.getInt("bus_number"),
                    rs.getString("status"),
                    rs.getTimestamp("registration_date")
                );
                st.setSelectedRouteName(rs.getString("route_name"));
                students.add(st);
            }
        }
        return students;
    }

    /**
     * Get all students assigned to a specific route.
     */
    public List<Student> getStudentsByRoute(int routeId) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, s.student_roll, "
                   + "s.selected_route_id, s.assigned_route_id, s.bus_number, "
                   + "s.status, s.registration_date "
                   + "FROM students s "
                   + "WHERE s.assigned_route_id = ? AND s.status = 'APPROVED' "
                   + "ORDER BY s.bus_number, s.student_name";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_roll"),
                        rs.getInt("selected_route_id"),
                        rs.getInt("assigned_route_id"),
                        rs.getInt("bus_number"),
                        rs.getString("status"),
                        rs.getTimestamp("registration_date")
                    ));
                }
            }
        }
        return students;
    }

    /**
     * Get all students (for admin overview).
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, s.student_roll, "
                   + "s.selected_route_id, s.assigned_route_id, s.bus_number, "
                   + "s.status, s.registration_date, "
                   + "COALESCE(r1.route_name, '') AS selected_route, "
                   + "COALESCE(r2.route_name, '') AS assigned_route "
                   + "FROM students s "
                   + "LEFT JOIN routes r1 ON s.selected_route_id = r1.id "
                   + "LEFT JOIN routes r2 ON s.assigned_route_id = r2.id "
                   + "ORDER BY s.registration_date DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student st = new Student(
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("student_roll"),
                    rs.getInt("selected_route_id"),
                    rs.getInt("assigned_route_id"),
                    rs.getInt("bus_number"),
                    rs.getString("status"),
                    rs.getTimestamp("registration_date")
                );
                st.setSelectedRouteName(rs.getString("selected_route"));
                // Store assigned route name in a temp way
                st.setAssignedRouteName(rs.getString("assigned_route"));
                students.add(st);
            }
        }
        return students;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BUS CAPACITY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Count how many students are assigned to a specific route + bus number.
     */
    public int getBusCount(int routeId, int busNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students "
                   + "WHERE assigned_route_id = ? AND bus_number = ? AND status = 'APPROVED'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, busNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  APPROVE & ASSIGN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Approve a student and assign to a route bus.
     */
    public boolean assignStudentToBus(int studentId, int routeId, int busNumber) throws SQLException {
        String sql = "UPDATE students SET assigned_route_id = ?, bus_number = ?, "
                   + "status = 'APPROVED' WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, busNumber);
            ps.setInt(3, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DECLINE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Decline a student transport request.
     */
    public boolean declineStudent(int studentId) throws SQLException {
        String sql = "UPDATE students SET status = 'DECLINED' WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DUPLICATE CHECK
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if a student already has a PENDING or APPROVED request.
     * Used to prevent duplicate transport registrations.
     */
    public boolean hasPendingOrApprovedRequest(String roll) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE student_roll = ? AND status IN ('PENDING', 'APPROVED')";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
