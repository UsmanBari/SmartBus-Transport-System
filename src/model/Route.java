package model;

/**
 * Represents a transport Route entity.
 */
public class Route {

    private int    id;
    private String routeName;
    private int    totalStops;
    private double distance;

    public Route() {}

    public Route(String routeName, int totalStops, double distance) {
        this.routeName  = routeName;
        this.totalStops = totalStops;
        this.distance   = distance;
    }

    public Route(int id, String routeName, int totalStops, double distance) {
        this.id         = id;
        this.routeName  = routeName;
        this.totalStops = totalStops;
        this.distance   = distance;
    }

    public int    getId()         { return id; }
    public String getRouteName()  { return routeName; }
    public int    getTotalStops() { return totalStops; }
    public double getDistance()   { return distance; }

    public void setId(int id)               { this.id = id; }
    public void setRouteName(String n)      { this.routeName = n; }
    public void setTotalStops(int s)        { this.totalStops = s; }
    public void setDistance(double d)       { this.distance = d; }

    @Override
    public String toString() { return routeName; }
}
