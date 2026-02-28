package model;

/**
 * Represents a Driver entity linked to a Route.
 */
public class Driver {

    private int    id;
    private String driverName;
    private int    routeId;

    public Driver() {}

    public Driver(String driverName, int routeId) {
        this.driverName = driverName;
        this.routeId    = routeId;
    }

    public Driver(int id, String driverName, int routeId) {
        this.id         = id;
        this.driverName = driverName;
        this.routeId    = routeId;
    }

    public int    getId()         { return id; }
    public String getDriverName() { return driverName; }
    public int    getRouteId()    { return routeId; }

    public void setId(int id)              { this.id = id; }
    public void setDriverName(String name) { this.driverName = name; }
    public void setRouteId(int routeId)    { this.routeId = routeId; }

    @Override
    public String toString() { return driverName; }
}
