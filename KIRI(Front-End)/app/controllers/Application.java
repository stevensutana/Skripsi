package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;

import scala.util.parsing.json.JSONObject;
import views.html.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Application extends Controller {

    Form<User> userForm = Form.form(User.class);
    DynamicForm dynamicForm;

    public Result index() {
        ctx().setTransientLang("en");
        return ok(index.render());
    }

    //i18n
    public Result id() {
        ctx().setTransientLang("id");
        return ok(index.render());
    }

    public Result findRoute() {
        return ok(index.render());
    }

    public Result testdb() {
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        String verifier = "";
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT verifier, ipFilter, domainFilter FROM apikeys WHERE verifier = '02428203D4526448';");

            while (result.next()) {
//                verifier = result.getString("verifier");
                if(result.getString("verifier").equals("02428203D4526448")){
                    output.append("asd");
                }
//                output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));
            }
//            output.append(verifier);
            connection.close();
            return ok(output.toString());
        } catch (Exception e) {
            return internalServerError(e.toString());
        } finally {
            if (connection != null) {
//                      connection.close();
            }
        }
    }

//    public Result submit()
//    {
//        Form<User> boundForm = userForm.bindFromRequest();
//
//        if(boundForm.hasErrors())
//        {
//            flash("error","Please correct the form");
//            return badRequest(index.render(boundForm));
//        }
//
//        User user = boundForm.get();
//        flash("Success","Calculating routes");
//        return redirect("/");
//
//    }


    public void start_working(){
        response().setContentType("application/json");
        response().setHeader("Cache-control", "no-cache");
        response().setHeader("Pragma","no-cache");
    }

    public String retrieve_data(String param){
        String str = this.dynamicForm.get(param);
        return str;
    }

    public ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put("status", "ok");
        if (message != null) {
            obj.put("status", message);
        }
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



    public boolean domain_matches(String url,String filter){
        boolean bool = false;

//        filter = filter.replaceAll('.', '\\.');
//        filter = filter.replace('*', '.*');
//
//        String domain = "";
//        if(url != null){
//            domain = url;
//        }
//
//        if(pregMatch("/^$filter\$/",domain)){
//            bool = true;
//        }

        return bool;
    }

    public static boolean pregMatch(String pattern, String content) {
        return content.matches(pattern);
    }

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

    public void log_statistic(String verifier,String type,String additional_info){
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();

            statement.executeUpdate("INSERT INTO statistics (verifier, type, additionalInfo) VALUES ("+verifier+","+type+","+additional_info+")");
            connection.close();
        } catch (Exception e) {
        }
    }

    public double compute_distance(double lat1,double lon1,double lat2,double lon2){
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



}
