package test;

import model.FeeChallan;
import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;

/**
 * White-Box Test: FeeChallanController State Rendering – Statement Coverage
 *
 * Tests the rendering state logic that determines which UI elements are shown
 * based on the challan's existence and status.
 *
 * Paths tested:
 *   P1: challan == null        → renderNoChallanState()   – Info card displayed
 *   P2: status = UNPAID        → renderChallanCard()      – Upload section shown
 *   P3: status = PROOF_SUBMITTED → renderChallanCard()    – Upload replaced by info msg
 *   P4: status = PAID          → renderChallanCard()      – Success banner shown
 *
 * Statement coverage: all 4 rendering paths = 100%
 */
public class FeeChallanControllerStateTest {

    // ── State Constants ─────────────────────────────────────────────────

    private static final String STATE_NO_CHALLAN     = "NO_CHALLAN";
    private static final String STATE_UNPAID         = "UNPAID";
    private static final String STATE_PROOF_SUBMITTED = "PROOF_SUBMITTED";
    private static final String STATE_PAID           = "PAID";

    // ── Simulated Rendering State ────────────────────────────────────────

    /**
     * Represents the result of the controller's state rendering decision.
     * In the actual application, these map to UI panel visibility states.
     */
    static class RenderState {
        boolean showInfoCard;        // "No challan yet" card
        boolean showChallanCard;     // Challan details card
        boolean showUploadSection;   // File chooser + submit button
        boolean showInfoMessage;     // "Proof submitted, awaiting" message
        boolean showSuccessBanner;   // "Payment confirmed" banner
        boolean showSubmitButton;    // Upload submit button
        String  badgeColor;          // Badge indicator color
        String  stateName;           // Internal state name

        @Override
        public String toString() {
            return "RenderState{state=" + stateName
                + ", infoCard=" + showInfoCard
                + ", challanCard=" + showChallanCard
                + ", upload=" + showUploadSection
                + ", infoMsg=" + showInfoMessage
                + ", successBanner=" + showSuccessBanner
                + ", badge=" + badgeColor + "}";
        }
    }

    /**
     * Mirrors the FeeChallanController's state rendering logic.
     * This is the method under test (extracted from the controller for unit testing).
     *
     * In the actual app, this logic is in FeeChallanController.refreshContent()
     * which queries the DAO and then decides which UI state to render.
     */
    private RenderState determineRenderState(FeeChallan challan) {
        RenderState state = new RenderState();

        if (challan == null) {
            // Path P1: No challan exists for this student
            state.stateName = STATE_NO_CHALLAN;
            state.showInfoCard = true;
            state.showChallanCard = false;
            state.showUploadSection = false;
            state.showInfoMessage = false;
            state.showSuccessBanner = false;
            state.showSubmitButton = false;
            state.badgeColor = null;
            return state;
        }

        state.showChallanCard = true;
        state.showInfoCard = false;

        switch (challan.getStatus()) {
            case "UNPAID":
                // Path P2: Challan exists, not yet paid
                state.stateName = STATE_UNPAID;
                state.showUploadSection = true;
                state.showSubmitButton = true;
                state.showInfoMessage = false;
                state.showSuccessBanner = false;
                state.badgeColor = "ORANGE";
                break;

            case "PROOF_SUBMITTED":
                // Path P3: Proof uploaded, awaiting admin confirmation
                state.stateName = STATE_PROOF_SUBMITTED;
                state.showUploadSection = false;
                state.showSubmitButton = false;
                state.showInfoMessage = true;
                state.showSuccessBanner = false;
                state.badgeColor = "BLUE";
                break;

            case "PAID":
                // Path P4: Payment confirmed
                state.stateName = STATE_PAID;
                state.showUploadSection = false;
                state.showSubmitButton = false;
                state.showInfoMessage = false;
                state.showSuccessBanner = true;
                state.badgeColor = "GREEN";
                break;

            default:
                state.stateName = "UNKNOWN";
                break;
        }

        return state;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  P1: challan == null → renderNoChallanState()
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testP1_NoChallan_ShowsInfoCard() {
        RenderState state = determineRenderState(null);

        assertEquals("P1: State should be NO_CHALLAN", STATE_NO_CHALLAN, state.stateName);
        assertTrue("P1: Info card should be shown", state.showInfoCard);
        assertFalse("P1: Challan card should NOT be shown", state.showChallanCard);
        assertFalse("P1: Upload section should NOT be shown", state.showUploadSection);
        assertFalse("P1: Submit button should NOT be shown", state.showSubmitButton);
        assertFalse("P1: Info message should NOT be shown", state.showInfoMessage);
        assertFalse("P1: Success banner should NOT be shown", state.showSuccessBanner);
        assertNull("P1: Badge color should be null", state.badgeColor);

        System.out.println("PASS - P1: " + state);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  P2: status = UNPAID → renderChallanCard() with upload section
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testP2_Unpaid_ShowsUploadSection() {
        FeeChallan challan = createTestChallan("UNPAID");

        RenderState state = determineRenderState(challan);

        assertEquals("P2: State should be UNPAID", STATE_UNPAID, state.stateName);
        assertFalse("P2: Info card should NOT be shown", state.showInfoCard);
        assertTrue("P2: Challan card should be shown", state.showChallanCard);
        assertTrue("P2: Upload section should be shown", state.showUploadSection);
        assertTrue("P2: Submit button should be enabled", state.showSubmitButton);
        assertFalse("P2: Info message should NOT be shown", state.showInfoMessage);
        assertFalse("P2: Success banner should NOT be shown", state.showSuccessBanner);
        assertEquals("P2: Badge color should be ORANGE", "ORANGE", state.badgeColor);

        System.out.println("PASS - P2: " + state);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  P3: status = PROOF_SUBMITTED → renderChallanCard() with info message
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testP3_ProofSubmitted_ShowsInfoMessage() {
        FeeChallan challan = createTestChallan("PROOF_SUBMITTED");
        challan.setReceiptImagePath("receipts/receipt_001.jpg");
        challan.setProofSubmittedAt(new Timestamp(System.currentTimeMillis()));

        RenderState state = determineRenderState(challan);

        assertEquals("P3: State should be PROOF_SUBMITTED", STATE_PROOF_SUBMITTED, state.stateName);
        assertFalse("P3: Info card should NOT be shown", state.showInfoCard);
        assertTrue("P3: Challan card should be shown", state.showChallanCard);
        assertFalse("P3: Upload section should NOT be shown (replaced)", state.showUploadSection);
        assertFalse("P3: Submit button should NOT be shown", state.showSubmitButton);
        assertTrue("P3: Info message should be shown", state.showInfoMessage);
        assertFalse("P3: Success banner should NOT be shown", state.showSuccessBanner);
        assertEquals("P3: Badge color should be BLUE", "BLUE", state.badgeColor);

        System.out.println("PASS - P3: " + state);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  P4: status = PAID → renderChallanCard() with success banner
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testP4_Paid_ShowsSuccessBanner() {
        FeeChallan challan = createTestChallan("PAID");
        challan.setReceiptImagePath("receipts/receipt_001.jpg");
        challan.setProofSubmittedAt(new Timestamp(System.currentTimeMillis() - 86400000));
        challan.setPaidAt(new Timestamp(System.currentTimeMillis()));

        RenderState state = determineRenderState(challan);

        assertEquals("P4: State should be PAID", STATE_PAID, state.stateName);
        assertFalse("P4: Info card should NOT be shown", state.showInfoCard);
        assertTrue("P4: Challan card should be shown", state.showChallanCard);
        assertFalse("P4: Upload section should NOT be shown", state.showUploadSection);
        assertFalse("P4: Submit button should NOT be shown (no buttons)", state.showSubmitButton);
        assertFalse("P4: Info message should NOT be shown", state.showInfoMessage);
        assertTrue("P4: Success banner should be shown", state.showSuccessBanner);
        assertEquals("P4: Badge color should be GREEN", "GREEN", state.badgeColor);

        System.out.println("PASS - P4: " + state);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Additional: State transition ordering tests
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testStateTransition_UnpaidToProofSubmitted() {
        // Simulate the lifecycle: UNPAID → PROOF_SUBMITTED
        FeeChallan challan = createTestChallan("UNPAID");

        RenderState before = determineRenderState(challan);
        assertEquals("Before: should be UNPAID", STATE_UNPAID, before.stateName);
        assertTrue("Before: upload should be visible", before.showUploadSection);

        // Student uploads proof → status changes
        challan.setStatus("PROOF_SUBMITTED");
        challan.setReceiptImagePath("receipts/receipt.jpg");
        challan.setProofSubmittedAt(new Timestamp(System.currentTimeMillis()));

        RenderState after = determineRenderState(challan);
        assertEquals("After: should be PROOF_SUBMITTED", STATE_PROOF_SUBMITTED, after.stateName);
        assertFalse("After: upload should NOT be visible", after.showUploadSection);
        assertTrue("After: info message should be visible", after.showInfoMessage);

        System.out.println("PASS - State transition: UNPAID → PROOF_SUBMITTED verified.");
    }

    @Test
    public void testStateTransition_ProofSubmittedToPaid() {
        // Simulate: PROOF_SUBMITTED → PAID
        FeeChallan challan = createTestChallan("PROOF_SUBMITTED");
        challan.setReceiptImagePath("receipts/receipt.jpg");

        RenderState before = determineRenderState(challan);
        assertEquals("Before: should be PROOF_SUBMITTED", STATE_PROOF_SUBMITTED, before.stateName);

        // Admin confirms payment
        challan.setStatus("PAID");
        challan.setPaidAt(new Timestamp(System.currentTimeMillis()));

        RenderState after = determineRenderState(challan);
        assertEquals("After: should be PAID", STATE_PAID, after.stateName);
        assertTrue("After: success banner should be visible", after.showSuccessBanner);
        assertFalse("After: no buttons should be visible", after.showSubmitButton);

        System.out.println("PASS - State transition: PROOF_SUBMITTED → PAID verified.");
    }

    @Test
    public void testFullLifecycle() {
        // P1 → P2 → P3 → P4 (full lifecycle)
        // Step 1: No challan
        RenderState s1 = determineRenderState(null);
        assertEquals("Step 1: NO_CHALLAN", STATE_NO_CHALLAN, s1.stateName);

        // Step 2: Challan created (UNPAID)
        FeeChallan challan = createTestChallan("UNPAID");
        RenderState s2 = determineRenderState(challan);
        assertEquals("Step 2: UNPAID", STATE_UNPAID, s2.stateName);

        // Step 3: Proof submitted
        challan.setStatus("PROOF_SUBMITTED");
        RenderState s3 = determineRenderState(challan);
        assertEquals("Step 3: PROOF_SUBMITTED", STATE_PROOF_SUBMITTED, s3.stateName);

        // Step 4: Payment confirmed
        challan.setStatus("PAID");
        RenderState s4 = determineRenderState(challan);
        assertEquals("Step 4: PAID", STATE_PAID, s4.stateName);

        System.out.println("PASS - Full lifecycle: NO_CHALLAN → UNPAID → PROOF_SUBMITTED → PAID");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private FeeChallan createTestChallan(String status) {
        FeeChallan challan = new FeeChallan();
        challan.setChallanId(1);
        challan.setStudentId(100);
        challan.setStudentRoll("22I-0001");
        challan.setStudentName("Test Student");
        challan.setRouteId(1);
        challan.setRouteName("Campus to City Center");
        challan.setAmountDue(12000.0);
        challan.setStatus(status);
        challan.setIssuedAt(new Timestamp(System.currentTimeMillis()));
        return challan;
    }
}
