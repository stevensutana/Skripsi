package models.helpers;

import models.Alternative;
import models.ProtoRegion;

import java.util.*;

/**
 * Created by Node8 on 1/12/15.
 */
public final class Constants {

    public static Alternative[] alternatives;

    public static Map<String, ProtoRegion> regioninfos;


    // Cache types and expiry
    public static String cache_geocoding = "geocoding";
    public static String cache_expiry_geocoding_mysql = "6 MONTH";
    public static String cache_searchplace = "searchplace";
    public static String cache_expiry_searchplace_mysql = "1 MONTH";

    public static int cookie_expiry = 3600 * 24 * 365;

    /** MySQL interval for session expiry. */
    public static String session_expiry_interval_mysql = "6 HOUR";
    /** Unix time interval for session expiry (seconds). */
    public static int session_expiry_interval_unix = 6 * 3600;
    /** Major customer's timezone in seconds, currently points to Bandung (GMT+7) */
    public static int timezone_offset = 7 * 60 * 60;

    /** Number of decimal digits for lat/lon. */
    public static int latlon_precision = 5;

    /** maximum uploaded file size */
    public static int max_filesize = 100 * 1024;

    public static String places_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    /** Maximum number of search result. */
    public static int search_maxresult = 10;
    /** API key for server side apps. */
    public static String gmaps_server_key = "AIzaSyBa1bNBVkchvxSFd8U_Cn7HsHux6M-DIk4";
    /** URL for Google Maps" Reverse geocoding web service. */
    public static String gmaps_geocode_url = "https://maps.googleapis.com/maps/api/geocode/json";

    public static String angkotwebid_url_prefix = "https://angkot.web.id/go/route/";
    public static String angkotwebid_url_suffix = "?ref=kiri";


    public static int speed_walk = 5;
    public static String menjangan_url = "http://newmenjangan.cloudapp.net:8000";

    public static String proto_address = "address";
    public static String proto_apikey = "apikey";
    public static String proto_apikeys_list = "apikeyslist";
    public static String proto_attributions = "attributions";
    public static String proto_company = "company";
    public static String proto_description = "description";
    public static String proto_domainfilter = "domainfilter";
    public static String proto_errorcode = "errorcode";
    public static String proto_filename = "filename";
    public static String proto_fqcategoryid = "fqcategoryid";
    public static String proto_fullname = "fullname";
    public static String proto_geodata = "geodata";
    public static String proto_internalinfo = "internalinfo";
    public static String proto_locale = "locale";
    public static String proto_locale_indonesia = "id";
    public static String proto_locale_english = "en";
    public static String proto_location = "location";
    public static String proto_message = "message";
    public static String proto_mode = "mode";
    public static String proto_mode_add_track = "addtrack";
    public static String proto_mode_add_apikey = "addapikey";
    public static String proto_mode_cleargeodata = "cleargeodata";
    public static String proto_mode_delete_place = "deleteplace";
    public static String proto_mode_delete_track = "deletetrack";
    public static String proto_mode_findroute = "findroute";
    public static String proto_mode_getdetails_track = "getdetailstrack";
    public static String proto_mode_getprofile = "getprofile";
    public static String proto_mode_importkml = "importkml";
    public static String proto_mode_list_apikeys = "listapikeys";
    public static String proto_mode_list_places = "listplaces";
    public static String proto_mode_list_tracks = "listtracks";
    public static String proto_mode_login = "login";
    public static String proto_mode_logout = "logout";
    public static String proto_mode_register = "register";
    public static String proto_mode_reporterror = "reporterror";
    public static String proto_mode_search = "searchplace";
    public static String proto_mode_update_apikey = "updateapikey";
    public static String proto_mode_update_profile = "updateprofile";
    public static String proto_mode_update_track = "updatetrack";
    public static String proto_mode_nearbytransports = "nearbytransports";
    public static String proto_nearbytransports = "nearbytransports";
    public static String proto_new_trackid = "newtrackid";
    public static String proto_orderid = "orderid";
    public static String proto_password = "password";
    public static String proto_pathloop = "loop";
    public static String proto_penalty = "penalty";
    public static String proto_phonenumber = "phonenumber";
    public static String proto_placename = "placename";
    public static String proto_placeslistresult = "placeslistresult";
    public static String proto_presentation = "presentation";
    public static String proto_presentation_desktop = "desktop";
    public static String proto_presentation_mobile = "mobile";
    public static String proto_privilege_apiUsage = "apiusage";
    public static String proto_privilege_route = "route";
    public static String proto_privileges = "privileges";
    public static String proto_querystring = "querystring";
    public static String proto_radius = "radius";
    public static String proto_rating = "rating";
    public static String proto_region = "region";
    public static String proto_region_bandung = "bdo";
    public static String proto_region_jakarta = "cgk";
    public static String proto_region_surabaya = "sub";
    public static String proto_region_malang = "mlg";
    public static String proto_routefinish = "finish";
    public static String proto_routestart = "start";
    public static String proto_routingresult = "routingresult";
    public static String proto_routingresults = "routingresults";
    public static String proto_search_querystring = "querystring";
    public static String proto_search_result = "searchresult";
    public static String proto_sessionid = "sessionid";
    public static String proto_status = "status";
    public static String proto_status_credentialfail = "credentialfail";
    public static String proto_status_error = "error";
    public static String proto_status_ok = "ok";
    public static String proto_status_sessionexpired = "sessionexpired";
    public static String proto_steps = "steps";
    public static String proto_time = "time";
    public static String proto_trackid = "trackid";
    public static String proto_trackname = "trackname";
    public static String proto_tracktype = "tracktype";
    public static String proto_trackslist = "trackslist";
    public static String proto_tracktypeslist = "tracktypeslist";
    public static String proto_transfernodes = "transfernodes";
    public static String proto_traveltime = "traveltime";
    public static String proto_updateprofile = "updateprofile";
    public static String proto_uploadedfile = "uploadedfile";
    public static String proto_url = "url";
    public static String proto_userid = "userid";
    public static String proto_venuedetailsref = "venuedetailsref";
    public static String proto_venuephotoref = "venuephotoref";
    public static String proto_venueid = "venueid";
    public static String proto_venues = "venues";
    public static String proto_verifier = "verifier";
    public static String proto_version = "version";
    public static String proto_width = "width";
    // KalapaDago protocol constants
    public static String protokd_point_finish = "finish";
    public static String protokd_point_start = "start";
    public static String protokd_result_none = "none";
    public static String protokd_transitmode_walk = "walk";
    public static String protokd_maximumwalk = "mw";
    public static String protokd_walkingmultiplier = "wm";
    public static String protokd_penaltytransfer = "pt";

    static{

        alternatives = new Alternative[3];
        alternatives[0] = new Alternative(0.75,1,0.15);
        alternatives[1] = new Alternative(1,0.75,0.15);
        alternatives[2] = new Alternative(0.75,1,0.45);

        regioninfos = new HashMap<String, ProtoRegion>();

        regioninfos.put(proto_region_bandung, new ProtoRegion(-6.91474,107.60981,17000,12,", *(bandung|bdg)$","Bandung"));
        regioninfos.put(proto_region_jakarta, new ProtoRegion(-6.21154,106.84517,15000,11,", *(jakarta|jkt)$","Jakarta"));
        regioninfos.put(proto_region_surabaya, new ProtoRegion(-7.27421,112.71908,15000,12,", *(surabaya|sby)$","Surabaya"));
        regioninfos.put(proto_region_malang, new ProtoRegion(-7.9812985,112.6319264,15000,13,", *(malang|mlg)$","Malang"));






    }


}
