package models.helpers;

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

    public static void log_statistic(String verifier,String type,String additional_info){
        Connection connection = null;
        try {
            connection = DB.getConnection();
            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO statistics (verifier, type, additionalInfo) VALUES ?,?,?");
            stmt.setString(1,verifier);
            stmt.setString(2,type);
            stmt.setString(3,additional_info);
            stmt.executeUpdate();

            connection.close();
        } catch (Exception e) {
        }
    }


    public static ObjectNode die_nice(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.PROTO_STATUS, Constants.PROTO_STATUS_ERROR);
        obj.put(Constants.PROTO_MESSAGE, message);
        return obj;
    }

    public static int indexPregMatch(String regex, String content) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
        }
        return index;
    }


    public static ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put(Constants.PROTO_STATUS, Constants.PROTO_STATUS_OK);
        if (message != null) {
            obj.put(Constants.PROTO_MESSAGE, message);
        }
        return obj;
    }

    public static String get_from_cache(String type,String key){

        Connection connection = DB.getConnection();
        StringBuilder output = new StringBuilder();
        try {
            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "SELECT cacheValue FROM cache WHERE type = ? AND cacheKey = ?");
            stmt.setString(1,type);
            stmt.setString(2,key);

            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                output.append(result.getString(1));
            }


            connection.close();
        } catch (Exception e) {
            log_error("Unable to retrieve from cache:" + e.getMessage());
        }

        return output.toString();
    }

    public static void put_to_cache(String type, String key,String value){
        Connection connection = DB.getConnection();
        try {
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


    public static void log_error(String message){

        Logger.error(message);
    }

    public static String validateLocale(String locale){

        if(locale.equals(Constants.PROTO_LOCALE_INDONESIA)){

            return locale;
        }else{
            return Constants.PROTO_LOCALE_ENGLISH;
        }
    }

    public static String validateRegion(String region){

        if(Constants.REGIONINFOS.get(region) != null){

            return region;
        }else{
            return Constants.PROTO_REGION_BANDUNG;
        }
    }




}
