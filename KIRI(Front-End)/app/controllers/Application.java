package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Alternatives;
import models.Constants;
import models.ProtoRegion;
import models.User;
import play.*;
import play.Logger;
import play.cache.*;
import play.api.libs.ws.WSAuthScheme;
import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;

public class Application extends Controller {

    Form<User> userForm = Form.form(User.class);
    DynamicForm dynamicForm;
    @Inject
    CacheApi cache;



    @Inject WSClient ws;

    public Result index() {
        String locale = retrieve_data("locale");
        if(locale!=null){
            ctx().setTransientLang(locale);
        }
//
//        ObjectNode objNode = Json.newObject();
//
//        objNode.put(Constants.proto_status,Constants.proto_status_ok);
//
//        Map<String, Object> routing_result = new HashMap<String, Object>();
//
//        routing_result.put(Constants.proto_steps, "asb");
//        routing_result.put(Constants.proto_traveltime,"jkl");
//
//
//            String routingResults = "";
//            for (Map.Entry<String,Object> entry : routing_result.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                routingResults += key + " = " + value.toString() +", ";
//                // do stuff
//            }
//        Logger.debug(routingResults);
//
//        objNode.put(Constants.proto_routingresults,routingResults);
//        Logger.debug(objNode.toString());
        return ok(index.render("id"));
    }

    //i18n
//    public Result id() {
//        ctx().setTransientLang("id");
//        return ok(index.render());
//    }

    public Result testing(){
//        F.Promise<JsonNode> promise = WS.url("http://52.76.73.21:3000/api/users/")
//                .setContentType("application/x-www-form-urlencoded")
//                .post("token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0NTQzMDg5MzR9.FkJvWact_rYv-1WWUnH1q16iUnVs91eJYOCUkZ-Xto0&username=emu@emu.emu")
//                .map(
////                        new F.Function<WSResponse, String>() {
////                            public String apply(WSResponse response) {
////                                String result = response.getBody();
////                                return result;
////                            }
////                        }
//                        new F.Function<WSResponse, JsonNode>() {
//                            public JsonNode apply(WSResponse response) {
//                                JsonNode json = response.asJson();
//                                return json;
//                            }
//                        }
//                );
//
//        long timeout = 1000l;// 1 sec might be too many for most cases!
//
////        String result = promise.get(timeout);
//        JsonNode json = promise.get(timeout);
//        String result = json.get("message").textValue();
//        ArrayNode arr = (ArrayNode)json.get("Users");
//        JsonNode user = json.get("Users");
//        result += arr.get(0).get("home").textValue();

//        //for get request
//        F.Promise<String> promise = WS.url("http://newmenjangan.cloudapp.net:8000")
//                .setQueryParameter("start", "-6.94303,107.60260")
//                .setQueryParameter("finish", "-6.91678,107.61118")
//                .setQueryParameter("mw", "0.75")
//                .setQueryParameter("wm","0.75")
//                .setQueryParameter("pt","0.75")
//                .get()
//                .map(
//                        new F.Function<WSResponse, String>() {
//                            public String apply(WSResponse response) {
//                                String result = response.getBody();
//                                return result;
//                            }
//                        }
//                );
//
//
//        long timeout = 1000l;// 1 sec might be too many for most cases!
//        String result = promise.get(timeout);
//
//        //end request


        Alternatives alternatives[] = new Alternatives[3];
        alternatives[0] = new Alternatives(0.75,1,0.15);
        alternatives[1] = new Alternatives(1,0.75,0.15);
        alternatives[2] = new Alternatives(0.75,1,0.45);
        String result = ""+alternatives[0].getMw()+alternatives[1].getMw()+"ASD"+alternatives[2].getMw();


        return ok(alternatives[0].print());
    }

    public Result testdb() {
        java.sql.Connection connection = null;
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

    public Result handle()
    {
        //initialize

        StringBuilder output = new StringBuilder();

        //because java cant take string as an index,use map instead
        Map<String, Boolean> results = new HashMap<String, Boolean>();

        ArrayList<Object> route_output = new ArrayList<Object>();


        String mode = retrieve_data("mode");
        int version = Integer.parseInt(retrieve_data("version"));
        if(version == 0){
            version = 1;
        }
        String apikey = retrieve_data("apikey");
        check_apikey(apikey);

        if(mode.equals("findroute"))
        {
            Logger.debug("findroute");
            String start = addSlashes(retrieve_data("start"));
            String finish = addSlashes(retrieve_data("finish"));
            String locale = addSlashes(retrieve_data("locale"));

            Logger.debug(start + " finish : "+ finish);

            String presentation = addSlashes(retrieve_data("presentation"));

            String result = null;
            if(!presentation.equals("mobile") && !presentation.equals("desktop"))
            {
                Utils.log_error("Presentation not understood: "+ presentation);
            }

            if(version>=2)
            {
                Logger.debug("version >= 2");
                int count = 0;
                if(presentation.equals("mobile")){
                    count = 1;
                }else{
                    count = Constants.alternatives.length;
                }

                Logger.debug("count : " + count);

                for (int i = 0;i<count;i++){
                    result = getFromMenjangan(start,finish,Constants.alternatives[i].getMw(),Constants.alternatives[i].getWm(),Constants.alternatives[i].getPt());


                    results.put(result,true);



//                    Logger.debug("result 0" + result);
                }

            }else//version <2
            {
                result = getFromMenjangan(start,finish);

                results.put(result,true);

//                    for(Iterator<String> i = results..iterator(); i.hasNext(); ) {
//                        String item = i.next();
//                        System.out.println(item);
//                    }

            }

            double travel_time = 0;
            for (Map.Entry<String, Boolean> iterator : results.entrySet()) {

                Logger.debug("for results");

                Logger.debug("iterator : " + iterator.getKey());

                String[] steps = iterator.getKey().split("\n");
                for (String step: steps) {
                    Logger.debug("step : "+step);
                    step = step.trim();
                    if(step.equals("")){
                        continue;
                    }

                    if(step.equals(Constants.protokd_result_none)){
                        if(results.size() == 1){
                            route_output.add("none");
                            route_output.add("none");
                            String arrStartFinish[] = new String[2];
                            arrStartFinish[0] = start;
                            arrStartFinish[1] = finish;
                            route_output.add(arrStartFinish);
                            route_output.add(Messages.get("message_routenotfound["+ presentation +"]"));
                            route_output.add(null);
                            route_output.add(null);

                            travel_time = 0;




                            break;
                        }else{
                            continue;
                        }
                    }

                    String[] stepSplit = step.split("/");
                    String means = stepSplit[0];
                    String means_detail = stepSplit[1];
                    String route = stepSplit[2];
                    String distance = stepSplit[3];
                    Logger.debug("distance: " + distance);
//                    String nearbyplaceids = stepSplit[4];

                    if(means.isEmpty() || route.isEmpty() || distance.isEmpty()){
                        //die_nice
                        Utils.log_error("Incomplete response in this line: ");
                    }

                    String[] points = route.split(" ");

                    String from = points[0];

                    String to = points[points.length-1];



                    String booking_url = null, editor_url = null;

                    for (int i = 0; i< points.length; i++)
                    {

                        if(points[i].equals(Constants.protokd_point_start))
                        {
                            points[i] = start;
                        }

                        if(points[i].equals(Constants.protokd_point_finish))
                        {
                            points[i] = finish;
                        }

                    }


                    String humanized_from = "",humanized_to = "";
                    try {
                        humanized_from = humanize_point(from);
                    } catch (Exception e) {
                        Utils.log_error("humanized from error :" + e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        humanized_to = humanize_point(to);
                    } catch (Exception e) {
                        Utils.log_error("humanized to error :" + e.getMessage());
                    }

                    String humanreadable = "";

                    Logger.debug("means : " + means);
                    if(means.equals(Constants.protokd_transitmode_walk))
                    {
                        if(humanized_from.equals(humanized_to))
                        {
                            if(!presentation.equals(Constants.proto_presentation_mobile))
                            {
                                humanreadable = Messages.get("message_walk_samestreet");
                                humanreadable = humanreadable.replaceAll("%street",humanized_from);
                                humanreadable = humanreadable.replaceAll("%distance",format_distance(Double.parseDouble(distance),locale));

                            }
                        }else
                        {

                            if(presentation.equals(Constants.proto_presentation_mobile))
                            {
                                humanized_from += "";
                                humanized_to += " %toicon";
                            }

                            humanreadable = Messages.get("message_walk");
                            humanreadable = humanreadable.replaceAll("%from",humanized_from);
                            humanreadable = humanreadable.replaceAll("%to",humanized_to);
                            humanreadable = humanreadable.replaceAll("%distance",format_distance(Double.parseDouble(distance),locale));


                        }

                        travel_time += Double.parseDouble(distance) / Constants.speed_walk;

                    }else
                    {

//                        try {
//                            means_detail = mysql_real_escape_string(DB.getConnection(),means_detail);
//                        } catch (Exception e) {
//                            Utils.log_error("Means detail fail when escape : " + e.getMessage());
//                        }

                        Logger.debug("means detail : "+ means_detail);

                        String readable_track_name = null,track_type_name = null;

                        double speed = 0;
                        java.sql.Connection
                                connection = DB.getConnection();
//                        StringBuilder output = new StringBuilder();
                        try {
//                            connection = DB.getConnection();
                            // Look for angkot.web.id refreshes
                            Statement statement = connection.createStatement();

                            ResultSet result2 = statement.executeQuery("SELECT tracks.trackname, tracktypes.name, tracktypes.url, tracks.extraParameters, tracktypes.speed, tracks.internalInfo FROM tracks JOIN tracktypes ON tracktypes.trackTypeId='" +
                                    means + "' AND tracks.trackTypeId='" + means + "' AND tracks.trackid='"+ means_detail + "';");

                            while (result2.next()) {
                                //php starts from 0,jdbc start from 1

                                readable_track_name = result2.getString(1);
                                track_type_name = result2.getString(2);
                                speed = result2.getDouble(5);

                                if((result2.getString(3) != null && !result2.getString(3).isEmpty()) && (result2.getString(4) != null && !result2.getString(4).isEmpty()))
                                {
                                    booking_url = result2.getString(3) + result2.getString(4);
                                }

                                if(result2.getString(6).startsWith("angkotwebid:"))
                                {
                                    String[] token = result2.getString(6).split(":");
                                    editor_url = Constants.angkotwebid_url_prefix + token[1] + Constants.angkotwebid_url_suffix;
                                }

//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

                            }


                            connection.close();
                        } catch (Exception e) {
                            Utils.log_error("Can't retrieve the track name from database: " + e.getMessage());
                        }

                        if(presentation.equals(Constants.proto_presentation_mobile))
                        {
                            humanized_from += " %fromicon";
                            humanized_to += " %toicon";
                        }

                        humanreadable = Messages.get("message_angkot");
                        Logger.debug("humanreadable" + humanreadable);
                        try{

                            humanreadable = humanreadable.replaceAll("%from",humanized_from);
                            humanreadable = humanreadable.replaceAll("%to",humanized_to);
                            humanreadable = humanreadable.replaceAll("%distance",format_distance(Double.parseDouble(distance),locale));
                            humanreadable = humanreadable.replaceAll("%trackname",readable_track_name);
                            humanreadable = humanreadable.replaceAll("%tracktype",track_type_name);
                        }catch (Exception ex){

                        }

                        travel_time += Double.parseDouble(distance) / speed;

                        if(means.equals("bdo_angkot") && version < 3)
                        {
                            means = "angkot";
                        }

                    }

                    if(humanreadable != null && !humanreadable.isEmpty())
                    {
                        route_output.clear();
                        route_output.add(means);
                        route_output.add(means_detail);
                        String pointString = "";
                        for (int i = 0;i<points.length;i++){
                            pointString += points[i] + " ";
                        }
                        route_output.add(pointString);
                        route_output.add(humanreadable);
                        route_output.add(booking_url);
                        route_output.add(editor_url);
                    }



                }

            }

            Map<String, Object> routing_result = new HashMap<String, Object>();

            routing_result.put(Constants.proto_steps,route_output);
            routing_result.put(Constants.proto_traveltime,format_traveltime(travel_time));


            Map<String, Object> routing_results = routing_result;

            Utils.log_statistic(apikey,"FINDROUTE",start+"/"+finish+"/"+results.size());

            ObjectNode objectNode = Json.newObject();
            if(version >=2)
            {

                objectNode.put(Constants.proto_status,Constants.proto_status_ok);
//                    String routingResults = "";
//                    for (Map.Entry<String,Object> entry : routing_results.entrySet()) {
//                        String key = entry.getKey();
//                        Object value = entry.getValue();
//                        routingResults += key + " = " + value.toString() +", ";
//                        // do stuff
//                    }

                objectNode.put(Constants.proto_routingresults, routing_results.toString());
//                    objectNode.put(Constants.proto_routingresults, routingResults);

            }else{

                objectNode.put(Constants.proto_status,Constants.proto_status_ok);
                objectNode.put(Constants.proto_routingresult,routing_results.get(Constants.proto_steps).toString());
                objectNode.put(Constants.proto_traveltime,routing_results.get(Constants.proto_traveltime).toString());
            }

            output.append(objectNode.toString());
            Logger.debug("mode findroute JSON: "+ objectNode.toString());


        }else if(mode.equals(Constants.proto_mode_search))
        {

            ObjectNode json_output = Json.newObject();

            String querystring = retrieve_data(Constants.proto_search_querystring);

//            String apikey = retrieve_data(Constants.proto_apikey);

            String region = retrieve_data(Constants.proto_region);

            region = region != null? region : Constants.proto_region_bandung;


            for (Map.Entry<String, ProtoRegion> iterator : Constants.regioninfos.entrySet()) {
                if(Utils.pregMatch("/" + iterator.getValue().getSearchPlace_regex() + "i",querystring)){
                    region = iterator.getKey();

                    break;
                }
            }

            String cached_searchplace = Utils.get_from_cache(Constants.cache_searchplace,region + "/" + querystring);

            if(cached_searchplace != null && !cached_searchplace.isEmpty()){
                json_output.put(Constants.proto_status,"ok");
                json_output.put(Constants.proto_search_result,cached_searchplace);

                json_output.put(Constants.proto_attributions,"");




                Utils.log_statistic(apikey, "SEARCHPLACE",querystring + "/cache");
            }else
            {
                double city_lat = Constants.regioninfos.get(region).getLat();
                double city_lon = Constants.regioninfos.get(region).getLon();
                double city_radius = Constants.regioninfos.get(region).getRadius();

                String full_url = Constants.places_url + "?key=" + Constants.gmaps_server_key + "&location=" + city_lat + "," + city_lon
                        + "&radius=" + city_radius + "&keyword=" + querystring + "&types=establishment|route&sensor=true";

                String result = file_get_contents(full_url);

                JsonNode json_result = null;
                try{

                    json_result = Json.parse(result);
                }catch (Exception e){
                    Utils.log_error("ERROR places url" + e.getMessage());
                }

                int size = 0;

                Logger.debug(result);

                ArrayNode arrayJsonResult = (ArrayNode)json_result.withArray("results");
                if(json_result.findPath("status").textValue().equals("ok") || json_result.findPath("status").textValue().equals("ZERO_RESULTS") )
                {
                    if(json_result.findPath("status").textValue().equals("ZERO_RESULTS"))
                    {
                        Utils.log_error("Place search not found: " + querystring);
                    }else
                    {
                        size = Math.min(arrayJsonResult.size(),Constants.search_maxresult);

                    }

                    for (int i = 0;i<size;i++){
                        String current_venue = arrayJsonResult.get(i).textValue();
//                        !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    }


                    json_output.put(Constants.proto_status,"ok");
//                    json_output.put(Constants.proto_search_result,search_result);

                    json_output.put(Constants.proto_attributions,"");


                    Utils.log_statistic(apikey, "SEARCHPLACE", querystring + "/" + size);
//                    Utils.put_to_cache(Constants.cache_searchplace,region + "/"+querystring,search_result);


                }else
                {
                    Logger.error("Place Search returned error: for this request " + full_url);
                }

            }

            Logger.debug("mode search json : " + json_output.toString());


        }else if(mode.equals(Constants.proto_mode_reporterror))
        {
            String errorcode = retrieve_data(Constants.proto_errorcode);

            Utils.log_error("Client reported error: " + errorcode);

            Logger.error(Utils.well_done("").toString());


        }else if (mode.equals(Constants.proto_mode_nearbytransports))
        {
            String start = retrieve_data(Constants.proto_routestart);

            if(version >= 2)
            {
                String result_menjangan_url = file_get_contents(Constants.menjangan_url + "/?start=" + start);
                String lines[] = result_menjangan_url.split("\n");

                for(String line : lines){

                }


            }
        }
//        output.append(mode);
        return ok(output.toString());
    }

    public String getFromMenjangan(String start,String finish,double mw,double wm,double pt){
        F.Promise<String> promise = WS.url("http://newmenjangan.cloudapp.net:8000")
                .setQueryParameter("start", start)
                .setQueryParameter("finish", finish)
                .setQueryParameter("mw", Double.toString(mw))
                .setQueryParameter("wm",Double.toString(wm))
                .setQueryParameter("pt",Double.toString(pt))
                .get()
                .map(
                        new F.Function<WSResponse, String>() {
                            public String apply(WSResponse response) {
                                String result = response.getBody();
                                return result;
                            }
                        }
                );


        long timeout = 512000l;// 1 sec might be too many for most cases!
        String result = promise.get(timeout);
        return result;
    }

    public String getFromMenjangan(String start,String finish) {
        F.Promise<String> promise = WS.url("http://newmenjangan.cloudapp.net:8000")
                .setQueryParameter("start", start)
                .setQueryParameter("finish", finish)
                .get()
                .map(
                        new F.Function<WSResponse, String>() {
                            public String apply(WSResponse response) {
                                String result = response.getBody();
                                return result;
                            }
                        }
                );


        long timeout = 1000l;// 1 sec might be too many for most cases!
        String result = promise.get(timeout);
        return result;
    }

    public String file_get_contents(String url) {
        F.Promise<String> promise = WS.url(url)
                .get()
                .map(
                        new F.Function<WSResponse, String>() {
                            public String apply(WSResponse response) {
                                String result = response.getBody();
                                return result;
                            }
                        }
                );


        long timeout = 3000l;// 1 sec might be too many for most cases!
        String result = promise.get(timeout);
        return result;
    }

    public String format_traveltime(double time){
        if(time>1){
            return Math.round(time) + Messages.get("message_hour");
        }else{

            return 5 * Math.ceil(time * 60 / 5) + Messages.get("message_min");
        }
    }



    public static String addSlashes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\\n", "\\\\n");
        s = s.replaceAll("\\r", "\\\\r");
        s = s.replaceAll("\\00", "\\\\0");
        s = s.replaceAll("'", "\\\\'");
        return s;
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


    public String retrieve_data(String param){
        this.dynamicForm = Form.form().bindFromRequest();
        String str = this.dynamicForm.get(param);
        return str;
    }






    public boolean check_apikey(String apikey){
        boolean bool = true;
        java.sql.Connection
                connection = DB.getConnection();
        StringBuilder output = new StringBuilder();
        String ipAddr = request().remoteAddress();//ip address client
        try {
//            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT apikeys.verifier, apikeys.ipFilter FROM apikeys WHERE apikeys.verifier = '"+apikey+"';");

            if(result.next()){
                while (result.next()) {

                    if(!ipAddr.equals(result.getString("ipFilter")) && !result.getString("ipFilter").isEmpty()){
                        bool = false;
                        Utils.log_error("IP address is not accepted for this API key.");
                    }

//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

                }
            }else{
                bool = false;
                Utils.log_error("API key is not recognized: "+apikey);
            }

            connection.close();
        } catch (Exception e) {
            Utils.log_error("failed to execute query on Apikey check.");
        }
        return bool;
    }



    public String humanize_point(String location) throws Exception {

        Logger.debug("location : "+location);
        if(location.equals(Constants.protokd_point_start)){
            return Messages.get("message_start");
        }else if(location.equals(Constants.protokd_point_finish)){

            return Messages.get("message_finish");

        }else{
//            location = mysql_real_escape_string(DB.getConnection(),location);

            String cached_geocode = Utils.get_from_cache(Constants.cache_geocoding,location);

            Logger.debug("cached_geocode : "+ cached_geocode);

            if(cached_geocode != null && !cached_geocode.isEmpty()){
                return cached_geocode;
            }else{
                String full_url = Constants.gmaps_geocode_url+ "?key="+ Constants.gmaps_server_key+ "&latlng=" + location+ "&sensor=false";
                String result = file_get_contents(full_url);

//                Logger.debug("url geocode : " + full_url);
                JsonNode json_response = null;
                try{

                    json_response = Json.parse(result);
                }catch (Exception e){
                    Utils.log_error("ERROR HUMANIZE POINT :" + e.getMessage());
                }

                if(json_response.findPath("status").textValue().equals("OK")){
                    Logger.debug("status : OK");
                    String bestguess = location;
                    ArrayNode arrayNode = (ArrayNode)json_response.withArray("results");
                    for (int i = 0;i<arrayNode.size();i++)
                    {
                        for (JsonNode component : json_response.findPath("results").get(0).get("address_components"))
                        {
                            if(Arrays.asList(component.findPath("types")).contains("transit_station") || Arrays.asList(component.findPath("types")).contains("route"))
                            {

                                Utils.put_to_cache(Constants.cache_geocoding,location,component.findPath("long_name").textValue());
                                return component.findPath("long_name").textValue();
                            }
                            bestguess = component.findPath("long_name").textValue();
                        }

                    }


                    Utils.log_error("Warning: can't find street name, use bestguess " + bestguess + " for "+ location);
                    Utils.put_to_cache(Constants.cache_geocoding, location, bestguess);
                    return bestguess;

                }else if(json_response.get("status").equals("ZERO_RESULTS")){

                    Utils.log_error("Warning: can't find coordinate for" + location);
                    return location;
                }else{
                    Utils.log_error("Problem while geocoding from Google reverse geocoding: ");

                    return "";
                }



            }


        }
    }

    public static String mysql_real_escape_string(java.sql.Connection link, String str)
            throws Exception
    {
        if (str == null) {
            return null;
        }

        if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]","").length() < 1) {
            return str;
        }

        String clean_string = str;
        clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
        clean_string = clean_string.replaceAll("\\n","\\\\n");
        clean_string = clean_string.replaceAll("\\r", "\\\\r");
        clean_string = clean_string.replaceAll("\\t", "\\\\t");
        clean_string = clean_string.replaceAll("\\00", "\\\\0");
        clean_string = clean_string.replaceAll("'", "\\\\'");
        clean_string = clean_string.replaceAll("\\\"", "\\\\\"");

        if (clean_string.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]"
                ,"").length() < 1)
        {
            return clean_string;
        }

        java.sql.Statement stmt = link.createStatement();
        String qry = "SELECT QUOTE('"+clean_string+"')";

        stmt.executeQuery(qry);
        java.sql.ResultSet resultSet = stmt.getResultSet();
        resultSet.first();
        String r = resultSet.getString(1);
        return r.substring(1,r.length() - 1);

    }

    public String format_distance(double distance,String locale){

        if(distance<1.0){
            return Math.floor(distance*1000.0) + " meter";
        }else{
            char decimal = locale == "id"?',':'.';
            double fdist = Math.floor(distance);
            return fdist + decimal + Math.floor((distance - fdist) * 10) + " kilometer";
        }

    }





}
