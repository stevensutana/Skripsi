package models;

/**
 * Created by Node8 on 1/12/15.
 */
public class ProtoRegion {

    private double lat,lon;
    private int radius,zoom;
    private String searchPlace,name;

    public ProtoRegion(double lat, double lon, int radius, int zoom, String searchPlace,String name) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.zoom = zoom;
        this.searchPlace = searchPlace;
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public int getZoom() {
        return zoom;
    }

    public int getRadius() {
        return radius;
    }

    public String getSearchPlace() {
        return searchPlace;
    }

    public String getName() {
        return name;
    }
}
