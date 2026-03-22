package model;

import java.sql.Timestamp;

/**
 * Represents a Student entity for transport registration.
 */
public class Student {

    private int       studentId;
    private String    studentName;
    private String    studentRoll;
    private int       selectedRouteId;
    private int       assignedRouteId;
    private int       busNumber;
    private String    status;           // PENDING | APPROVED | REJECTED
    private Timestamp registrationDate;

    // ── Extra display fields (joined from routes table) ──────────────────────
    private String selectedRouteName;
    private String assignedRouteName;

    public Student() {}

    /** Constructor for new registration (insert). */
    public Student(String studentName, String studentRoll, int selectedRouteId) {
        this.studentName     = studentName;
        this.studentRoll     = studentRoll;
        this.selectedRouteId = selectedRouteId;
        this.status          = "PENDING";
    }

    /** Full row constructor (result set mapping). */
    public Student(int studentId, String studentName, String studentRoll,
                   int selectedRouteId, int assignedRouteId, int busNumber,
                   String status, Timestamp registrationDate) {
        this.studentId       = studentId;
        this.studentName     = studentName;
        this.studentRoll     = studentRoll;
        this.selectedRouteId = selectedRouteId;
        this.assignedRouteId = assignedRouteId;
        this.busNumber       = busNumber;
        this.status          = status;
        this.registrationDate = registrationDate;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int       getStudentId()        { return studentId; }
    public String    getStudentName()      { return studentName; }
    public String    getStudentRoll()      { return studentRoll; }
    public int       getSelectedRouteId()  { return selectedRouteId; }
    public int       getAssignedRouteId()  { return assignedRouteId; }
    public int       getBusNumber()        { return busNumber; }
    public String    getStatus()           { return status; }
    public Timestamp getRegistrationDate() { return registrationDate; }
    public String    getSelectedRouteName(){ return selectedRouteName; }
    public String    getAssignedRouteName(){ return assignedRouteName; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setStudentId(int id)                   { this.studentId = id; }
    public void setStudentName(String name)            { this.studentName = name; }
    public void setStudentRoll(String roll)            { this.studentRoll = roll; }
    public void setSelectedRouteId(int id)             { this.selectedRouteId = id; }
    public void setAssignedRouteId(int id)             { this.assignedRouteId = id; }
    public void setBusNumber(int bus)                  { this.busNumber = bus; }
    public void setStatus(String status)               { this.status = status; }
    public void setRegistrationDate(Timestamp ts)      { this.registrationDate = ts; }
    public void setSelectedRouteName(String name)      { this.selectedRouteName = name; }
    public void setAssignedRouteName(String name)      { this.assignedRouteName = name; }

    @Override
    public String toString() { return studentName + " (" + studentRoll + ")"; }
}
