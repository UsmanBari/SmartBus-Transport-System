package model;

import java.sql.Timestamp;

/**
 * Represents a Fee Challan entity for the fee management system.
 * Lifecycle: UNPAID → PROOF_SUBMITTED → PAID
 */
public class FeeChallan {

    private int       challanId;
    private int       studentId;
    private String    studentRoll;
    private String    studentName;
    private int       routeId;
    private String    routeName;
    private double    amountDue;
    private String    status;           // "UNPAID", "PROOF_SUBMITTED", "PAID"
    private Timestamp issuedAt;
    private String    receiptImagePath;
    private Timestamp proofSubmittedAt;
    private Timestamp paidAt;

    public FeeChallan() {}

    /** Constructor for creating a new challan (before DB insert). */
    public FeeChallan(int studentId, String studentRoll, String studentName,
                      int routeId, String routeName, double amountDue) {
        this.studentId   = studentId;
        this.studentRoll = studentRoll;
        this.studentName = studentName;
        this.routeId     = routeId;
        this.routeName   = routeName;
        this.amountDue   = amountDue;
        this.status      = "UNPAID";
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int       getChallanId()        { return challanId; }
    public int       getStudentId()        { return studentId; }
    public String    getStudentRoll()      { return studentRoll; }
    public String    getStudentName()      { return studentName; }
    public int       getRouteId()          { return routeId; }
    public String    getRouteName()        { return routeName; }
    public double    getAmountDue()        { return amountDue; }
    public String    getStatus()           { return status; }
    public Timestamp getIssuedAt()         { return issuedAt; }
    public String    getReceiptImagePath() { return receiptImagePath; }
    public Timestamp getProofSubmittedAt() { return proofSubmittedAt; }
    public Timestamp getPaidAt()           { return paidAt; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setChallanId(int id)                    { this.challanId = id; }
    public void setStudentId(int id)                    { this.studentId = id; }
    public void setStudentRoll(String roll)             { this.studentRoll = roll; }
    public void setStudentName(String name)             { this.studentName = name; }
    public void setRouteId(int id)                      { this.routeId = id; }
    public void setRouteName(String name)               { this.routeName = name; }
    public void setAmountDue(double amount)             { this.amountDue = amount; }
    public void setStatus(String status)                { this.status = status; }
    public void setIssuedAt(Timestamp ts)               { this.issuedAt = ts; }
    public void setReceiptImagePath(String path)        { this.receiptImagePath = path; }
    public void setProofSubmittedAt(Timestamp ts)       { this.proofSubmittedAt = ts; }
    public void setPaidAt(Timestamp ts)                 { this.paidAt = ts; }

    @Override
    public String toString() {
        return "Challan #" + challanId + " – " + studentName + " (" + studentRoll + ") – " + status;
    }
}
