package models;

public class ProtoRegion {

    private double lat,lon;
    private int radius,zoom;
    private String searchPlace_regex,name;

    public ProtoRegion(double lat, double lon, int radius, int zoom, String searchPlace_regex,String name) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.zoom = zoom;
        this.searchPlace_regex = searchPlace_regex;
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

    public String getSearchPlace_regex() {
        return searchPlace_regex;
    }

    public String getName() {
        return name;
    }
}
