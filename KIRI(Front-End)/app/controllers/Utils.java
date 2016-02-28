package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Constants;
import play.api.*;
import play.db.DB;
import play.libs.Json;
import play.mvc.Controller;
import play.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Node8 on 21/2/16.
 */
public final class Utils extends Controller {

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
            Statement statement = connection.createStatement();

            statement.executeUpdate("INSERT INTO statistics (verifier, tipe, additionalInfo) VALUES ("+verifier+","+type+","+additional_info+")");
            connection.close();
        } catch (Exception e) {
        }
    }


    public ObjectNode die_nice(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.proto_status, Constants.proto_status_error);
        obj.put(Constants.proto_message, message);
        return obj;
    }

    public boolean check_apikey_validity(String apikey){
        boolean bool = true;
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        String ipAddr = request().remoteAddress();//ip address client
        String refererUrl = request().getHeader("referer");
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT verifier, ipFilter, domainFilter FROM apikeys WHERE verifier = '"+apikey+"';");

            if(result.next()){
                while (result.next()) {
                    if(!ipAddr.equals(result.getString("ipFilter")) && !result.getString("ipFilter").isEmpty()){
                        bool = false;
                    }

                    if(domain_matches(refererUrl,result.getString("domainFilter"))){
                        bool = false;
                    }


//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

                }
            }else{
                bool = false;
            }

            connection.close();
        } catch (Exception e) {
        }
        return bool;
    }



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


    public static boolean pregMatch(String pattern, String content) {
        return content.matches(pattern);
    }

//    public static String pregMatch(String regex, String content,String match) {
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(content);
//        if (matcher.find()) {
//            match = matcher.group(1);   // this is almost same also, getting the first captured group
//        }
//        return match;
//    }


    public ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.proto_status, Constants.proto_status_ok);
        if (message != null) {
            obj.put(Constants.proto_message, message);
        }
        return obj;
    }

    public void start_working(){
        response().setContentType("application/json");
        response().setHeader("Cache-control", "no-cache");
        response().setHeader("Pragma","no-cache");
    }

    public static String get_from_cache(String type,String key){

        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT cacheValue FROM cache WHERE tipe = '"+type+"' AND cacheKey = '" + key + "';");

            if(result.next()){
                while (result.next()) {

                    output.append(result.getString(0));

//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

                }
            }

            connection.close();
        } catch (Exception e) {
            log_error("Unable to retrieve from cache:" + e.getMessage());
        }

        return output.toString();
    }

    public static void put_to_cache(String type, String key,String value){
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();

            statement.executeUpdate("INSERT INTO cache(tipe, cacheKey, cacheValue) VALUES ('"+type+"','"+key+"','"+value+"');");
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



}
