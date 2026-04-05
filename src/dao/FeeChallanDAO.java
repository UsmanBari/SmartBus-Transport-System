package dao;

import config.DatabaseConnection;
import model.FeeChallan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FeeChallan entities.
 * All SQL uses PreparedStatement (parameterized queries).
 */
public class FeeChallanDAO {

    // ═══════════════════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Insert a new UNPAID challan.
     * Uses the provided connection (for transaction support in approval flow).
     */
    public boolean createChallan(Connection conn, FeeChallan challan) throws SQLException {
        String sql = "INSERT INTO fee_challans (student_id, student_roll, student_name, "
                   + "route_id, route_name, amount_due, status, issued_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 'UNPAID', CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, challan.getStudentId());
            ps.setString(2, challan.getStudentRoll());
            ps.setString(3, challan.getStudentName());
            ps.setInt(4, challan.getRouteId());
            ps.setString(5, challan.getRouteName());
            ps.setDouble(6, challan.getAmountDue());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        challan.setChallanId(keys.getInt(1));
                    }
                }
            }
            return rows > 0;
        }
    }

    /**
     * Convenience overload: creates its own connection (non-transactional).
     */
    public boolean createChallan(FeeChallan challan) throws SQLException {
        return createChallan(DatabaseConnection.getInstance(), challan);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get challan by student_id — used in student portal.
     */
    public FeeChallan getChallanByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM fee_challans WHERE student_id = ? ORDER BY issued_at DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all challans — used in admin Fee Management panel.
     */
    public List<FeeChallan> getAllChallans() throws SQLException {
        List<FeeChallan> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_challans ORDER BY issued_at DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Get challans filtered by status.
     */
    public List<FeeChallan> getChallansByStatus(String status) throws SQLException {
        List<FeeChallan> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_challans WHERE status = ? ORDER BY issued_at DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Student submits receipt: update status to PROOF_SUBMITTED,
     * save image path and timestamp.
     */
    public boolean submitProof(int challanId, String receiptImagePath) throws SQLException {
        String sql = "UPDATE fee_challans SET status = 'PROOF_SUBMITTED', "
                   + "receipt_image_path = ?, proof_submitted_at = CURRENT_TIMESTAMP "
                   + "WHERE challan_id = ? AND status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptImagePath);
            ps.setInt(2, challanId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Admin confirms payment: update status to PAID, set paid_at.
     */
    public boolean confirmPayment(int challanId) throws SQLException {
        String sql = "UPDATE fee_challans SET status = 'PAID', "
                   + "paid_at = CURRENT_TIMESTAMP "
                   + "WHERE challan_id = ? AND status = 'PROOF_SUBMITTED'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challanId);
            return ps.executeUpdate() > 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  AGGREGATES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sum of amount_due where status = PAID.
     */
    public double getTotalCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_due), 0) FROM fee_challans WHERE status = 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    /**
     * Sum of amount_due where status != PAID (UNPAID + PROOF_SUBMITTED).
     */
    public double getTotalPending() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_due), 0) FROM fee_challans WHERE status != 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════════════════

    private FeeChallan mapRow(ResultSet rs) throws SQLException {
        FeeChallan c = new FeeChallan();
        c.setChallanId(rs.getInt("challan_id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setStudentRoll(rs.getString("student_roll"));
        c.setStudentName(rs.getString("student_name"));
        c.setRouteId(rs.getInt("route_id"));
        c.setRouteName(rs.getString("route_name"));
        c.setAmountDue(rs.getDouble("amount_due"));
        c.setStatus(rs.getString("status"));
        c.setIssuedAt(rs.getTimestamp("issued_at"));
        c.setReceiptImagePath(rs.getString("receipt_image_path"));
        c.setProofSubmittedAt(rs.getTimestamp("proof_submitted_at"));
        c.setPaidAt(rs.getTimestamp("paid_at"));
        return c;
    }
}
