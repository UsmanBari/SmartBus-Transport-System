package test;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Black-Box Test Cases for Route Fee Input Validation
 * Techniques: Equivalence Partitioning (EP), Boundary Value Analysis (BVA), Error Guessing
 *
 * Test Cases:
 *   TC-01: Valid fee (15000)                    – EP Valid
 *   TC-02: Boundary zero (0)                    – BVA Boundary
 *   TC-03: Negative (-500)                      – EP Invalid
 *   TC-04: Empty string                         – EP Invalid
 *   TC-05: Non-numeric ("abc")                  – Error Guessing
 *   TC-06: Minimum valid (0.01)                 – BVA Min Valid
 *
 * The validation logic under test mirrors AdminViewRoutesPanel.showEditFeeDialog():
 *   1. Parse the string with Double.parseDouble()
 *   2. If parsing fails → show error
 *   3. If value <= 0   → show error
 *   4. Otherwise       → valid, proceed to save
 */
public class RouteFeeValidationTest {

    // ── Validation logic extracted from AdminViewRoutesPanel ──────────────

    /**
     * Validates a fee amount string. Returns the parsed double if valid,
     * or throws IllegalArgumentException with the error message if invalid.
     *
     * This mirrors the actual validation in AdminViewRoutesPanel.showEditFeeDialog().
     */
    private double validateFeeInput(String feeStr) {
        if (feeStr == null || feeStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Please enter a valid fee amount (must be greater than 0).");
        }

        double fee;
        try {
            fee = Double.parseDouble(feeStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Please enter a valid fee amount (must be greater than 0).");
        }

        if (fee <= 0) {
            throw new IllegalArgumentException(
                "Please enter a valid fee amount (must be greater than 0).");
        }

        return fee;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-01: Valid fee – EP Valid
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC01_ValidFee_15000() {
        double result = validateFeeInput("15000");
        assertEquals("TC-01: Fee should be parsed as 15000.0", 15000.0, result, 0.001);
        System.out.println("PASS - TC-01: Valid fee 15000 accepted. Parsed value = " + result);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-02: Boundary zero – BVA Boundary
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC02_BoundaryZero() {
        try {
            validateFeeInput("0");
            fail("TC-02: Fee value 0 should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-02: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-02: Fee value 0 correctly rejected. Error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-03: Negative value – EP Invalid
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC03_NegativeValue() {
        try {
            validateFeeInput("-500");
            fail("TC-03: Negative fee should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-03: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-03: Negative fee -500 correctly rejected. Error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-04: Empty string – EP Invalid
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC04_EmptyString() {
        try {
            validateFeeInput("");
            fail("TC-04: Empty string should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-04: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-04: Empty string correctly rejected. Error: " + e.getMessage());
        }
    }

    @Test
    public void testTC04b_NullInput() {
        try {
            validateFeeInput(null);
            fail("TC-04b: Null input should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-04b: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-04b: Null input correctly rejected. Error: " + e.getMessage());
        }
    }

    @Test
    public void testTC04c_WhitespaceOnly() {
        try {
            validateFeeInput("   ");
            fail("TC-04c: Whitespace-only input should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-04c: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-04c: Whitespace-only correctly rejected.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-05: Non-numeric input – Error Guessing
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC05_NonNumericInput() {
        try {
            validateFeeInput("abc");
            fail("TC-05: Non-numeric input should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("TC-05: Error message should mention 'greater than 0'",
                e.getMessage().contains("greater than 0"));
            System.out.println("PASS - TC-05: Non-numeric 'abc' correctly rejected. Error: " + e.getMessage());
        }
    }

    @Test
    public void testTC05b_SpecialCharacters() {
        try {
            validateFeeInput("$15,000");
            fail("TC-05b: Currency-formatted input should be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS - TC-05b: '$15,000' correctly rejected.");
        }
    }

    @Test
    public void testTC05c_MixedAlphaNumeric() {
        try {
            validateFeeInput("123abc");
            fail("TC-05c: Mixed alpha-numeric input should be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS - TC-05c: '123abc' correctly rejected.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TC-06: Minimum valid value (0.01) – BVA Min Valid
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testTC06_MinimumValidFee() {
        double result = validateFeeInput("0.01");
        assertEquals("TC-06: Fee should be parsed as 0.01", 0.01, result, 0.001);
        System.out.println("PASS - TC-06: Minimum valid fee 0.01 accepted. Parsed value = " + result);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Additional EP / BVA Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testLargeValidFee() {
        double result = validateFeeInput("999999.99");
        assertEquals("Large fee should be accepted", 999999.99, result, 0.001);
        System.out.println("PASS - Large fee 999999.99 accepted.");
    }

    @Test
    public void testDecimalFee() {
        double result = validateFeeInput("12500.50");
        assertEquals("Decimal fee should be accepted", 12500.50, result, 0.001);
        System.out.println("PASS - Decimal fee 12500.50 accepted.");
    }

    @Test
    public void testVerySmallNegative() {
        try {
            validateFeeInput("-0.01");
            fail("Very small negative should be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS - Very small negative -0.01 correctly rejected.");
        }
    }
}
