package models.helpers;

import models.Alternative;
import models.ProtoRegion;

import java.util.*;

public final class Constants {


    public static final Alternative[] ALTERNATIVES;

    public static final Map<String, ProtoRegion> REGIONINFOS;


    // Cache types and expiry
    public static final String CACHE_GEOCODING = "geocoding";
    public static final String CACHE_SEARCHPLACE = "searchplace";

    public static final String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    /** Maximum number of search result. */
    public static final int SEARCH_MAXRESULT = 10;
    /** API key for server side apps. */
    public static final String GMAPS_SERVER_KEY = "AIzaSyBa1bNBVkchvxSFd8U_Cn7HsHux6M-DIk4";
    /** URL for Google Maps" Reverse geocoding web service. */
    public static final String GMAPS_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public static final String ANGKOTWEBID_URL_PREFIX = "https://angkot.web.id/go/route/";
    public static final String ANGKOTWEBID_URL_SUFFIX = "?ref=kiri";


    public static final int SPEED_WALK = 5;
    public static final String MENJANGAN_URL = "http://newmenjangan.cloudapp.net:8000";

    public static final String PROTO_ATTRIBUTIONS = "attributions";

    public static final String PROTO_ERRORCODE = "errorcode";

    public static final String PROTO_LOCALE = "locale";
    public static final String PROTO_LOCALE_INDONESIA = "id";
    public static final String PROTO_LOCALE_ENGLISH = "en";
    public static final String PROTO_LOCATION = "location";
    public static final String PROTO_MESSAGE = "message";

    public static final String PROTO_MODE_FINDROUTE = "findroute";
    public static final String PROTO_MODE_REPORTERROR = "reporterror";
    public static final String PROTO_MODE_SEARCH = "searchplace";
    public static final String PROTO_MODE_NEARBYTRANSPORTS = "nearbytransports";
    public static final String PROTO_NEARBYTRANSPORTS = "nearbytransports";
    public static final String PROTO_PLACENAME = "placename";
    public static final String PROTO_PRESENTATION_DESKTOP = "desktop";
    public static final String PROTO_PRESENTATION_MOBILE = "mobile";
    public static final String PROTO_REGION = "region";
    public static final String PROTO_REGION_BANDUNG = "bdo";
    public static final String PROTO_REGION_JAKARTA = "cgk";
    public static final String PROTO_REGION_SURABAYA = "sub";
    public static final String PROTO_REGION_MALANG = "mlg";
    public static final String PROTO_ROUTESTART = "start";
    public static final String PROTO_ROUTINGRESULT = "routingresult";
    public static final String PROTO_ROUTINGRESULTS = "routingresults";
    public static final String PROTO_SEARCH_QUERYSTRING = "querystring";
    public static final String PROTO_SEARCH_RESULT = "searchresult";
    public static final String PROTO_STATUS = "status";
    public static final String PROTO_STATUS_ERROR = "error";
    public static final String PROTO_STATUS_OK = "ok";
    public static final String PROTO_STEPS = "steps";
    public static final String PROTO_TRAVELTIME = "traveltime";
    // KalapaDago protocol constants
    public static final String PROTOKD_POINT_FINISH = "finish";
    public static final String PROTOKD_POINT_START = "start";
    public static final String PROTOKD_RESULT_NONE = "none";
    public static final String PROTOKD_TRANSITMODE_WALK = "walk";

    static{

        ALTERNATIVES = new Alternative[3];
        ALTERNATIVES[0] = new Alternative(0.75,1,0.15);
        ALTERNATIVES[1] = new Alternative(1,0.75,0.15);
        ALTERNATIVES[2] = new Alternative(0.75,1,0.45);

        REGIONINFOS = new HashMap<String, ProtoRegion>();

        REGIONINFOS.put(PROTO_REGION_BANDUNG, new ProtoRegion(-6.91474, 107.60981, 17000, 12, ", *(bandung|bdg)$", "Bandung"));
        REGIONINFOS.put(PROTO_REGION_JAKARTA, new ProtoRegion(-6.21154, 106.84517, 15000, 11, ", *(jakarta|jkt)$", "Jakarta"));
        REGIONINFOS.put(PROTO_REGION_SURABAYA, new ProtoRegion(-7.27421, 112.71908, 15000, 12, ", *(surabaya|sby)$", "Surabaya"));
        REGIONINFOS.put(PROTO_REGION_MALANG, new ProtoRegion(-7.9812985, 112.6319264, 15000, 13, ", *(malang|mlg)$", "Malang"));
    }


}
