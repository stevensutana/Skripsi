package models.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.DB;
import play.libs.Json;
import play.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Node8 on 21/2/16.
 */
public final class Utils {

    public void update_trackversion(){
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();

            statement.executeUpdate("UPDATE properties SET propertyvalue=propertyvalue+1 WHERE propertyname='trackversion'");
            connection.close();
        } catch (Exception e) {
        }
    }

    public void update_placesversion(){
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();

            statement.executeUpdate("UPDATE properties SET propertyvalue=propertyvalue+1 WHERE propertyname='placesversion'");
            connection.close();
        } catch (Exception e) {
        }
    }

    public static void log_statistic(String verifier,String type,String additional_info){
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes

            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO statistics (verifier, type, additionalInfo) VALUES ?,?,?");
            stmt.setString(1,verifier);
            stmt.setString(2,type);
            stmt.setString(3,additional_info);
            stmt.executeUpdate();
//            Statement statement = connection.createStatement();
//
//            statement.executeUpdate("INSERT INTO statistics (verifier, tipe, additionalInfo) VALUES ("+verifier+","+type+","+additional_info+")");
            connection.close();
        } catch (Exception e) {
        }
    }


    public static ObjectNode die_nice(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.proto_status, Constants.proto_status_error);
        obj.put(Constants.proto_message, message);
        return obj;
    }

//    public boolean check_apikey_validity(String apikey){
//        boolean bool = true;
//        Connection connection = null;
//        StringBuilder output = new StringBuilder();
//        String ipAddr = request().remoteAddress();//ip address client
//        String refererUrl = request().getHeader("referer");
//        try {
//            connection = DB.getConnection();
//            // Look for angkot.web.id refreshes
//            Statement statement = connection.createStatement();
//            ResultSet result = statement.executeQuery("SELECT verifier, ipFilter, domainFilter FROM apikeys WHERE verifier = '"+apikey+"';");
//
//            if(result.next()){
//                while (result.next()) {
//                    if(!ipAddr.equals(result.getString("ipFilter")) && !result.getString("ipFilter").isEmpty()){
//                        bool = false;
//                    }
//
//                    if(domain_matches(refererUrl,result.getString("domainFilter"))){
//                        bool = false;
//                    }
//
//
////                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));
//
//                }
//            }else{
//                bool = false;
//            }
//
//            connection.close();
//        } catch (Exception e) {
//        }
//        return bool;
//    }



    public static boolean domain_matches(String url,String filter){
        boolean bool = false;


        filter = filter.replaceAll(".", "\\\\.");
        filter = filter.replaceAll("\\*", ".*");

        String domain = "";
        if(url != null){
            domain = url;
        }
//
//        if(pregMatch("/^$filter\$/",domain)){
//            bool = true;
//        }

        //string is : /^$filter\$/
        return pregMatch("\\/\\^"+filter+"\\\\$/",domain);
    }


    public static boolean pregMatch(String regex, String content) {


        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        // Check all occurrences
//        while (matcher.find()) {
//            Logger.debug("Start index: " + matcher.start());
//            Logger.debug(" End index: " + matcher.end());
//            Logger.debug(" Found: " + matcher.group());
//        }
        return matcher.find();
    }

    public static int indexPregMatch(String regex, String content) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
//
//            Logger.debug("Start index: " + matcher.start());
//            Logger.debug(" End index: " + matcher.end());
//            Logger.debug(" Found: " + matcher.group());
        }
        return index;
    }


    public static ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.proto_status, Constants.proto_status_ok);
        if (message != null) {
            obj.put(Constants.proto_message, message);
        }
        return obj;
    }

//    public void start_working(){
//        response().setContentType("application/json");
//        response().setHeader("Cache-control", "no-cache");
//        response().setHeader("Pragma","no-cache");
//    }

    public static String get_from_cache(String type,String key){

        Connection connection = DB.getConnection();
        StringBuilder output = new StringBuilder();
        try {
//
//            DataSource ds = DB.getDataSource();
//            Connection connection = ds.getConnection();
//            connection = DB.getConnection();
            // Look for angkot.web.id refreshes


            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "SELECT cacheValue FROM cache WHERE type = ? AND cacheKey = ?");
            stmt.setString(1,type);
            stmt.setString(2,key);

            ResultSet result = stmt.executeQuery();
//            Statement statement = connection.createStatement();
//            ResultSet result = statement.executeQuery("SELECT cacheValue FROM cache WHERE tipe = '"+type+"' AND cacheKey = '" + key + "';");

            while (result.next()) {

                output.append(result.getString(1));

//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

            }


            connection.close();
        } catch (Exception e) {
            log_error("Unable to retrieve from cache:" + e.getMessage());
        }

        return output.toString();
    }

    public static void put_to_cache(String type, String key,String value){
        Connection connection = DB.getConnection();
        StringBuilder output = new StringBuilder();
        try {

            // Look for angkot.web.id refreshes

//            Statement statement = connection.createStatement();
//
//            statement.executeUpdate("INSERT INTO cache(type, cacheKey, cacheValue) VALUES ('"+type+"','"+key+"','"+value+"');");

            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO cache(type, cacheKey, cacheValue) VALUES (?,?,?)");
            stmt.setString(1,type);
            stmt.setString(2, key);

            stmt.setString(3, String.valueOf(value));
            stmt.executeUpdate();
            connection.close();
        } catch (Exception e) {
            log_error("Warning: Can't store cache "+e.getMessage());
        }


    }
    


    public static double compute_distance(double lat1,double lon1,double lat2,double lon2){
        int earth_radius = 6371;

        double dLat = deg2rad(lat2-lat1);
        double dLon = deg2rad(lon2-lon1);

        lat1 = deg2rad(lat1);
        lat2 = deg2rad(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) * Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        return c * earth_radius;

    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static void log_error(String message){

        Logger.error(message);
    }

    public static String validateLocale(String locale){

        if(locale.equals(Constants.proto_locale_indonesia)){

            return locale;
        }else{
            return Constants.proto_locale_english;
        }
    }

    public static String validateRegion(String region){

        if(Constants.regioninfos.get(region) != null){

            return region;
        }else{
            return Constants.proto_region_bandung;
        }
    }




}
