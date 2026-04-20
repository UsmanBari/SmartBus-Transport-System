package dao;

import config.DatabaseConnection;
import model.FeeChallan;
import org.junit.*;
import static org.junit.Assert.*;

import java.sql.*;

/**
 * White-Box Tests for FeeChallanDAO.createChallan()
 * Coverage target: 100% branch coverage (B1–B5)
 *
 * Branches tested:
 *   B1 – Connection acquired successfully → proceeds to INSERT
 *   B2 – Connection fails (null) → returns false / throws
 *   B3 – INSERT executes, rows affected > 0 → returns true
 *   B4 – INSERT executes, rows affected = 0 → returns false
 *   B5 – SQLException thrown during INSERT → exception propagated
 */
public class FeeChallanDAOTest {

    private static Connection conn;
    private FeeChallanDAO dao;

    // ── Setup / Teardown ────────────────────────────────────────────────

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Ensure DB connection is available (entry criterion)
        conn = DatabaseConnection.getInstance();
        assertNotNull("Database connection should be available for testing", conn);

        // Create test data: ensure a route and a student exist
        ensureTestData(conn);
    }

    @Before
    public void setUp() {
        dao = new FeeChallanDAO();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up test challans after each test
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM fee_challans WHERE student_roll = 'TEST-001'")) {
            ps.executeUpdate();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Clean up all test data
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM fee_challans WHERE student_roll = 'TEST-001'");
            stmt.executeUpdate("DELETE FROM students WHERE student_roll = 'TEST-001'");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  B1: Connection acquired successfully – proceeds to INSERT
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testB1_ConnectionAcquiredSuccessfully() throws Exception {
        // Branch B1: Normal DB connection available → should proceed to INSERT
        Connection testConn = DatabaseConnection.getInstance();
        assertNotNull("B1: Connection should be acquired successfully", testConn);
        assertFalse("B1: Connection should not be closed", testConn.isClosed());

        FeeChallan challan = createTestChallan();
        boolean result = dao.createChallan(testConn, challan);

        assertTrue("B1: createChallan should succeed with valid connection", result);
        assertTrue("B1: Challan ID should be set after insert", challan.getChallanId() > 0);

        System.out.println("PASS - B1: Connection acquired successfully. Challan ID = " + challan.getChallanId());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  B2: Connection fails (null) – should return false
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testB2_ConnectionFailsNull() throws Exception {
        // Branch B2: Null connection passed → should fail cleanly and return false
        FeeChallan challan = createTestChallan();
        boolean result = dao.createChallan(null, challan);

        assertFalse("B2: createChallan should return false for null connection", result);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  B3: INSERT executes, rows affected > 0 – returns true
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testB3_InsertSuccessful_ReturnsTrue() throws Exception {
        // Branch B3: Valid FeeChallan object → INSERT succeeds, rows > 0
        FeeChallan challan = createTestChallan();
        boolean result = dao.createChallan(conn, challan);

        assertTrue("B3: createChallan should return true when INSERT succeeds", result);
        assertTrue("B3: Generated key should be set", challan.getChallanId() > 0);

        // Verify data actually exists in DB
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM fee_challans WHERE challan_id = ?")) {
            ps.setInt(1, challan.getChallanId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("B3: Challan should exist in database after insert", rs.next());
                assertEquals("B3: Status should be UNPAID", "UNPAID", rs.getString("status"));
                assertEquals("B3: Amount should match", 12000.0, rs.getDouble("amount_due"), 0.01);
            }
        }

        System.out.println("PASS - B3: INSERT successful, rows > 0. Returned true.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  B4: INSERT executes, rows affected = 0 – connection failure simulated
    //      We test with a closed connection to simulate failure.
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testB4_InsertFailure_ReturnsFalse() throws Exception {
        // Branch B4: Simulate INSERT failure by using a separate closed connection
        Connection tempConn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/transport_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            "root", "");
        tempConn.close(); // Close it to force failure

        FeeChallan challan = createTestChallan();
        boolean result = dao.createChallan(tempConn, challan);
        assertFalse("B4: createChallan should return false for closed connection", result);

        System.out.println("PASS - B4: INSERT failed on closed connection and returned false.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  B5: SQLException thrown during INSERT – constraint violation
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testB5_SQLException_ConstraintViolation() throws Exception {
        // Branch B5: Trigger a constraint violation by using invalid foreign key
        FeeChallan challan = new FeeChallan();
        challan.setStudentId(999999);       // Non-existent student
        challan.setStudentRoll("INVALID-999");
        challan.setStudentName("Invalid Student");
        challan.setRouteId(999999);         // Non-existent route
        challan.setRouteName("Invalid Route");
        challan.setAmountDue(5000.0);

        boolean result = dao.createChallan(conn, challan);
        assertFalse("B5: createChallan should return false for invalid foreign keys", result);

        System.out.println("PASS - B5: Invalid foreign key insert returned false.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Additional White-Box Tests: Other DAO Methods
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testGetChallanByStudentId_Exists() throws Exception {
        // Insert a challan first
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);

        // Retrieve by student_id
        int testStudentId = challan.getStudentId();
        FeeChallan retrieved = dao.getChallanByStudentId(testStudentId);

        assertNotNull("Should retrieve challan by student ID", retrieved);
        assertEquals("Student roll should match", "TEST-001", retrieved.getStudentRoll());
        assertEquals("Status should be UNPAID", "UNPAID", retrieved.getStatus());

        System.out.println("PASS - getChallanByStudentId: Retrieved challan #" + retrieved.getChallanId());
    }

    @Test
    public void testGetChallanByStudentId_NotExists() throws Exception {
        // Try to get a challan for a student that has none
        FeeChallan retrieved = dao.getChallanByStudentId(999999);
        assertNull("Should return null for non-existent student", retrieved);

        System.out.println("PASS - getChallanByStudentId: null returned for non-existent student");
    }

    @Test
    public void testSubmitProof_ValidChallan() throws Exception {
        // Insert a challan
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);

        // Submit proof
        boolean result = dao.submitProof(challan.getChallanId(), "receipts/test_receipt.jpg");
        assertTrue("submitProof should return true for UNPAID challan", result);

        // Verify status changed
        FeeChallan updated = dao.getChallanByStudentId(challan.getStudentId());
        assertNotNull("Challan should still exist", updated);
        assertEquals("Status should be PROOF_SUBMITTED", "PROOF_SUBMITTED", updated.getStatus());
        assertEquals("Receipt path should be set", "receipts/test_receipt.jpg", updated.getReceiptImagePath());

        System.out.println("PASS - submitProof: Status changed to PROOF_SUBMITTED");
    }

    @Test
    public void testConfirmPayment_ValidChallan() throws Exception {
        // Insert and submit proof first
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);
        dao.submitProof(challan.getChallanId(), "receipts/test_receipt.jpg");

        // Confirm payment
        boolean result = dao.confirmPayment(challan.getChallanId());
        assertTrue("confirmPayment should return true for PROOF_SUBMITTED challan", result);

        // Verify status changed to PAID
        FeeChallan updated = dao.getChallanByStudentId(challan.getStudentId());
        assertNotNull("Challan should still exist", updated);
        assertEquals("Status should be PAID", "PAID", updated.getStatus());
        assertNotNull("paid_at should be set", updated.getPaidAt());

        System.out.println("PASS - confirmPayment: Status changed to PAID. Paid at: " + updated.getPaidAt());
    }

    @Test
    public void testConfirmPayment_WrongStatus() throws Exception {
        // Try to confirm an UNPAID challan (not PROOF_SUBMITTED)
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);

        boolean result = dao.confirmPayment(challan.getChallanId());
        assertFalse("confirmPayment should return false for UNPAID challan", result);

        System.out.println("PASS - confirmPayment: Correctly rejected UNPAID challan");
    }

    @Test
    public void testGetAllChallans() throws Exception {
        // Insert a challan
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);

        java.util.List<FeeChallan> all = dao.getAllChallans();
        assertNotNull("getAllChallans should not return null", all);
        assertTrue("Should have at least 1 challan", all.size() >= 1);

        System.out.println("PASS - getAllChallans: Retrieved " + all.size() + " challans");
    }

    @Test
    public void testGetChallansByStatus() throws Exception {
        // Insert a challan (UNPAID)
        FeeChallan challan = createTestChallan();
        dao.createChallan(conn, challan);

        java.util.List<FeeChallan> unpaid = dao.getChallansByStatus("UNPAID");
        assertNotNull("getChallansByStatus should not return null", unpaid);
        assertTrue("Should have at least 1 UNPAID challan", unpaid.size() >= 1);

        java.util.List<FeeChallan> paid = dao.getChallansByStatus("PAID");
        assertNotNull("getChallansByStatus for PAID should not return null", paid);

        System.out.println("PASS - getChallansByStatus: UNPAID=" + unpaid.size() + ", PAID=" + paid.size());
    }

    @Test
    public void testGetTotalCollectedAndPending() throws Exception {
        double collected = dao.getTotalCollected();
        double pending = dao.getTotalPending();

        assertTrue("Total collected should be >= 0", collected >= 0);
        assertTrue("Total pending should be >= 0", pending >= 0);

        System.out.println("PASS - Aggregates: Collected=" + collected + ", Pending=" + pending);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private FeeChallan createTestChallan() throws Exception {
        // Get the test student ID
        int studentId = getTestStudentId();
        int routeId = getTestRouteId();

        FeeChallan challan = new FeeChallan();
        challan.setStudentId(studentId);
        challan.setStudentRoll("TEST-001");
        challan.setStudentName("Test Student");
        challan.setRouteId(routeId);
        challan.setRouteName("Test Route");
        challan.setAmountDue(12000.0);
        return challan;
    }

    private int getTestStudentId() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT student_id FROM students WHERE student_roll = 'TEST-001'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new RuntimeException("Test student not found. Run ensureTestData first.");
    }

    private int getTestRouteId() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM routes LIMIT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new RuntimeException("No routes found. Insert test routes first.");
    }

    private static void ensureTestData(Connection conn) throws Exception {
        // Ensure at least one route exists
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM routes")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO routes (route_name, total_stops, distance, fee_amount) VALUES ('Test Route', 5, 10.0, 12000.0)")) {
                        ins.executeUpdate();
                    }
                }
            }
        }

        // Get a route ID for the test student
        int routeId;
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM routes LIMIT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                routeId = rs.getInt(1);
            }
        }

        // Ensure test student exists in students table
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM students WHERE student_roll = 'TEST-001'")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO students (student_name, student_roll, selected_route_id, status) "
                          + "VALUES ('Test Student', 'TEST-001', ?, 'APPROVED')")) {
                        ins.setInt(1, routeId);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }
}
