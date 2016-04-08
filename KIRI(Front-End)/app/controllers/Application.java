package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.helpers.Constants;
import models.helpers.Utils;
import play.Logger;
import play.api.mvc.Request;
import play.cache.CacheApi;
import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;

import scala.util.parsing.json.JSONObject;
import views.html.*;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Application extends Controller {

    //    Form<User> userForm = Form.form(User.class);
    private DynamicForm dynamicForm;



    @Inject private WSClient ws;
    @Inject
    CacheApi cache;

    private static final Comparator<ArrayList<String>> comparator = new Comparator<ArrayList<String>>() {
        @Override
        public int compare(ArrayList<String> o1, ArrayList<String> o2) {
//            int a = Integer.parseInt(o1.get(3));
//            int b = Integer.parseInt(o2.get(3));
            return Double.compare(Double.parseDouble(o1.get(3)),Double.parseDouble(o2.get(3)));
//            return a-b;
        }
    };


    public Result index() {
        String locale = retrieve_data(Constants.proto_locale);

        Http.Cookie cookieLocale = request().cookie(Constants.proto_locale);
        if(locale==null){
            if(cookieLocale!=null){
                locale = Utils.validateLocale(cookieLocale.value());
            }else{
                locale = Constants.proto_locale_english;
            }
        }else{//set default id

            locale = Utils.validateLocale(locale);
            response().setCookie(Constants.proto_locale,locale);
        }

        //i18n
        ctx().setTransientLang(locale);

        String region = retrieve_data(Constants.proto_region);
//        String cookieRegion = request().cookie(Constants.proto_region).value();
        Http.Cookie cookieRegion = request().cookie(Constants.proto_region);

        if(region==null){
            if(cookieRegion!=null){
                region = Utils.validateRegion(cookieRegion.value());
            }else{
                region = Constants.proto_region_bandung;
            }
        }else{//set default id

            region = Utils.validateLocale(region);
            response().setCookie(Constants.proto_region,region);
        }

        Logger.debug("locale: "+locale+" ,region: "+region);
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

//
//        Logger.debug(Utils.pregMatch(regex,pattern)+ " debugiasdija");
//        ArrayList<String> arr = new ArrayList<String>();
//        ArrayList<String> arr1 = new ArrayList<String>();
//
//
//        ArrayList<ArrayList<String>> nearby_result = new ArrayList<ArrayList<String>>();
//
//        arr.add("a");
//        arr.add("b");
//        arr.add("3");
//
//        nearby_result.add(arr);
//
//
//        arr1.add("a");
//        arr1.add("b");
//        arr1.add("1");
//
//        nearby_result.add(arr1);
//
//        Logger.debug(nearby_result.toString());
//
//
//        Collections.sort(nearby_result, comparator);
//
//
//        Logger.debug(nearby_result.toString());
//        String presentation = "mobile";
//        Logger.debug(Messages.get("message_routenotfound_"+presentation));


//
//        String str = String.format("%.5f,%.5f",2.0123123,10.12381293123);
//        Logger.debug(str);



//        String message = "<script>" +
//                "var region = 'sby';" +
//                "var input_text = [],coordinates = [];\n" +
//                "\n" +
//                "            input_text['start'] = null;\n" +
//                "            coordinates['start'] = null;\n" +
//                "            input_text['finish'] = null;\n" +
//                "            coordinates['finish'] = null;\n" +
//                "\n" +
//                "            var locale='id';\n" +
//                "            var region='bdo';\n" +
//                "</script>";

        String regions = new String();
        for (Map.Entry<String, ProtoRegion> iterator : Constants.regioninfos.entrySet()) {
            regions+= iterator.getKey()+ ": {center: '"+  iterator.getValue().getLat() + ","+ iterator.getValue().getLon() + "', zoom: "+ iterator.getValue().getZoom() +"},";
        }
        String message =
                "var input_text = [],coordinates = [];\n" +
                        "input_text['start'] = null;\n" +
                        "coordinates['start'] = null;\n" +
                        "input_text['finish'] = null;\n" +
                        "coordinates['finish'] = null;\n"+

                        "var locale='"+ locale+"';\n" +
                        "var region='"+region+"';\n" +
                        "var messageBuyTicket = '"+Messages.get("index_buyticket")+"';\n" +
                        "var messageConnectionError = '"+Messages.get("index_connectionerror")+"';\n" +
                        "var messageFillBoth = '"+Messages.get("index_fillboth")+"';\n" +
                        "var messageNotFound = '"+Messages.get("index_notfound")+"';\n" +
                        "var messageOops = '"+Messages.get("index_oops")+"';\n" +
                        "var messageOrderTicketHere = '"+Messages.get("index_order_ticket_here")+"';\n" +
                        "var messagePleaseWait = '<img src=\"/assets/images/loading.gif\" alt=\"... \"/>'+ '"+Messages.get("index_pleasewait")+"';\n" +
                        "var messageITake = '"+Messages.get("index_itake")+"';\n" +
                        "var region='"+region+"';\n" +
                        "var regions = {\n" +
                        regions +
                        "};\n" +
                        "$(document).foundation();";
        return ok(index.render(locale,Constants.regioninfos,message,region));
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


//        Utils.put_to_cache(Constants.cache_searchplace, "bdo/aa", "[{location=-6.91642,107.60111, placename=AA Taksi}");
//        Alternative alternatives[] = new Alternative[3];
//        alternatives[0] = new Alternative(0.75,1,0.15);
//        alternatives[1] = new Alternative(1,0.75,0.15);
//        alternatives[2] = new Alternative(0.75,1,0.45);
//        String result = ""+alternatives[0].getMw()+alternatives[1].getMw()+"ASD"+alternatives[2].getMw();

        ArrayList<Map<String,JsonNode>> routing_results = new ArrayList<Map<String,JsonNode>>();
        for (int i = 0; i < 3; i++) {

            Map<String, JsonNode> routing_result = new HashMap<String, JsonNode>();
            routing_result.put(Constants.proto_steps,Json.toJson(i));
            routing_result.put(Constants.proto_traveltime, Json.toJson(format_traveltime(100 + i)));
            routing_results.add(routing_result);

        }


        return ok(routing_results.toString());
    }

    public Result testdb() {
        java.sql.Connection connection = null;
        StringBuilder output = new StringBuilder();
        String verifier = "";
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "SELECT verifier, ipFilter, domainFilter FROM apikeys WHERE verifier = ?");

            stmt.setString(1, "02428203D4526448");
//            Statement statement = connection.createStatement();
//            ResultSet result = statement.executeQuery("SELECT verifier, ipFilter, domainFilter FROM apikeys WHERE verifier = '02428203D4526448';");

            ResultSet result = stmt.executeQuery();
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

//        Map<String, Object> routing_results = null;

//        Map<String, JsonNode> routing_result = new HashMap<String, JsonNode>();
        ArrayList<Map<String,JsonNode>> routing_results = new ArrayList<Map<String,JsonNode>>();

        ArrayList<ArrayList<Object>> route_output = new ArrayList<ArrayList<Object>>();


        String mode = retrieve_data("mode");
        int version = Integer.parseInt(retrieve_data("version"));
        if(version == 0){
            version = 1;
        }
        String apikey = retrieve_data("apikey");
//        return ok(check_apikey(apikey));
        if(!check_apikey(apikey).equals("{}")){

            return ok(check_apikey(apikey));
        }
//        Logger.debug("check apikey:"+check_apikey(apikey));


        if(mode.equals(Constants.proto_mode_findroute))
        {
//            Logger.debug("findroute");
            results = new HashMap<String, Boolean>();

            String start = retrieve_data("start");
            String finish = retrieve_data("finish");
            String locale = retrieve_data("locale");


            //localization
            ctx().setTransientLang(locale);

            String presentation = retrieve_data("presentation");

            String result = null;
            if(!presentation.equals(Constants.proto_presentation_mobile) && !presentation.equals(Constants.proto_presentation_desktop))
            {
                Utils.log_error("Presentation not understood: "+ presentation);
            }


            if(version>=2)
            {
//                Logger.debug("version >= 2");
                int count = presentation.equals(Constants.proto_presentation_mobile)?1:Constants.alternatives.length;
//                if(presentation.equals("mobile")){
//                    count = 1;
//                }else{
//                    count = Constants.alternatives.length;
//                }

//                Logger.debug("count : " + count);

                for (int i = 0;i<count;i++){
                    result = getFromMenjangan(start,finish,Constants.alternatives[i].getMw(),Constants.alternatives[i].getWm(),Constants.alternatives[i].getPt());


                    results.put(result, true);



                }

                Logger.debug("results" + results.toString());



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
                route_output = new ArrayList<ArrayList<Object>>();
                travel_time = 0;

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
                            ArrayList<Object> arrRouteOutput = new ArrayList<Object>();
                            arrRouteOutput.add("none");
                            arrRouteOutput.add("none");

                            String arrStartFinish[] = new String[2];
                            arrStartFinish[0] = start;
                            arrStartFinish[1] = finish;

                            arrRouteOutput.add(arrStartFinish);
                            arrRouteOutput.add(Messages.get("message_routenotfound_"+ presentation));
                            arrRouteOutput.add(null);
                            arrRouteOutput.add(null);


                            Logger.debug("ARRAY ROUTE OUTPUT: " + arrRouteOutput.toString());
                            route_output.add(arrRouteOutput);
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
                    try{

                        String nearbyplaceids = stepSplit[4];
                    }catch (Exception ex){
                        Logger.error(ex.getMessage());
                    }

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
//                        Logger.debug("humanize from"+humanized_from);
                    } catch (Exception e) {
                        Utils.log_error("humanized from error :" + e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        humanized_to = humanize_point(to);
//                        Logger.debug("humanize to"+humanized_to);
                    } catch (Exception e) {
                        Utils.log_error("humanized to error :" + e.getMessage());
                    }

                    String humanreadable = null;

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
                        booking_url = null;
                        editor_url = null;

                    }   else
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
                                }else{
                                    booking_url = null;
                                }

                                if(result2.getString(6).startsWith("angkotwebid:"))
                                {
                                    String[] token = result2.getString(6).split(":");
                                    editor_url = Constants.angkotwebid_url_prefix + token[1] + Constants.angkotwebid_url_suffix;
                                }else{
                                    editor_url = null;
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
                        try{

                            humanreadable = humanreadable.replaceAll("%from",humanized_from);
                            humanreadable = humanreadable.replaceAll("%to",humanized_to);
                            humanreadable = humanreadable.replaceAll("%distance",format_distance(Double.parseDouble(distance),locale));
                            humanreadable = humanreadable.replaceAll("%trackname",readable_track_name);
                            humanreadable = humanreadable.replaceAll("%tracktype",track_type_name);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        travel_time += Double.parseDouble(distance) / speed;

                        if(means.equals("bdo_angkot") && version < 3)
                        {
                            means = "angkot";
                        }

                    }
                    Logger.debug("humanreadable : "+humanreadable);

                    if(humanreadable != null && !humanreadable.isEmpty())
                    {
//                        route_output.clear();

                        ArrayList<Object> arrRouteOutput = new ArrayList<Object>();
                        arrRouteOutput.add(means);
                        arrRouteOutput.add(means_detail);

//                        String pointString = "";
//                        for (int i = 0;i<points.length;i++){
//                            pointString += points[i] + " ";
//                        }
                        arrRouteOutput.add(points);
                        arrRouteOutput.add(humanreadable);
                        arrRouteOutput.add(booking_url==null? NullNode.getInstance():booking_url);
                        arrRouteOutput.add(editor_url==null?NullNode.getInstance():editor_url);

                        route_output.add(arrRouteOutput);
                    }
//                    Logger.debug("route outut: "+route_output.toString());done



                }


                Map<String, JsonNode> routing_result = new HashMap<String, JsonNode>();

                routing_result.put(Constants.proto_steps,Json.toJson(route_output));
                routing_result.put(Constants.proto_traveltime, Json.toJson(format_traveltime(travel_time)));

                Logger.debug("routing_result: "+routing_result.toString());

//                routing_results = routing_result;
//                routing_results = new ArrayList<Map<String,JsonNode>>();
                routing_results.add(routing_result);

//                ArrayList<Map<String,JsonNode>> testarr = new ArrayList<Map<String,JsonNode>>();
//                testarr.add(routing_result);
//
//                Logger.debug("testarr: " + testarr.toString());

            }
            Logger.debug("routing results : "+routing_results.toString()+ routing_results.size());





            Utils.log_statistic(apikey, "FINDROUTE", start + "/" + finish + "/" + results.size());

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

                objectNode.put(Constants.proto_routingresults, Json.toJson(routing_results));
//                    objectNode.put(Constants.proto_routingresults, routingResults);

            }else{

                objectNode.put(Constants.proto_status,Constants.proto_status_ok);
                objectNode.put(Constants.proto_routingresult, Json.toJson(routing_results.get(0).get(Constants.proto_steps)));
                objectNode.put(Constants.proto_traveltime, Json.toJson(routing_results.get(0).get(Constants.proto_traveltime)));
            }

            String str = Json.stringify(objectNode);
            output.append(str);
            Logger.debug("mode findroute JSON: "+ objectNode.toString());


        }else if(mode.equals(Constants.proto_mode_search))
        {

            ObjectNode json_output = Json.newObject();

            String querystring = retrieve_data(Constants.proto_search_querystring);

//            String apikey = retrieve_data(Constants.proto_apikey);

            String region = retrieve_data(Constants.proto_region);

            region = region == null? Constants.proto_region_bandung : region;


            for (Map.Entry<String, ProtoRegion> iterator : Constants.regioninfos.entrySet()) {
                if(Utils.indexPregMatch(iterator.getValue().getSearchPlace_regex(),querystring)!=-1){
                    region = iterator.getKey();
                    int matches = Utils.indexPregMatch(iterator.getValue().getSearchPlace_regex(),querystring);
                    querystring = querystring.substring(0,matches);

                    break;
                }
            }

            String cached_searchplace = Utils.get_from_cache(Constants.cache_searchplace,region + "/" + querystring);

            Logger.debug(region+"/"+querystring);
            Logger.debug("cache_searchplace: json" + cached_searchplace);
            if(cached_searchplace != null && !cached_searchplace.isEmpty()){
                Logger.debug("cache_searchplace!=null&: " );
                json_output.put(Constants.proto_status,Constants.proto_status_ok);

                json_output.put(Constants.proto_search_result,Json.toJson(Json.parse(cached_searchplace)));

                json_output.put(Constants.proto_attributions,Json.toJson(NullNode.getInstance()));




                Utils.log_statistic(apikey, "SEARCHPLACE", querystring + "/cache");
            }else
            {
                double city_lat = Constants.regioninfos.get(region).getLat();
                double city_lon = Constants.regioninfos.get(region).getLon();
                int city_radius = Constants.regioninfos.get(region).getRadius();

//                String full_url = Constants.places_url + "?key=" + Constants.gmaps_server_key + "&location=" + city_lat + "," + city_lon
//                        + "&radius=" + city_radius + "&keyword=" + querystring + "&types=establishment|route&sensor=true";

                String result = getPlacesAPI(city_lat+","+city_lon,Integer.toString(city_radius),querystring);

//                Logger.debug(result);

                JsonNode json_result = null;
                try{

                    json_result = Json.parse(result);

                }catch (Exception e){
                    Utils.log_error("ERROR places url" + e.getMessage());
                }

                int size = 0;

                ArrayNode arrayJsonResult = (ArrayNode)json_result.withArray("results");
                if(json_result.findPath("status").textValue().equals("OK") || json_result.findPath("status").textValue().equals("ZERO_RESULTS") )
                {

//                    Map<Map<Integer,String>, Object> search_result = new HashMap<Map<Integer, String>, Object>();
                    ArrayList<Map<String, JsonNode>> search_res = new ArrayList<Map<String, JsonNode>>();

                    if(json_result.findPath("status").textValue().equals("ZERO_RESULTS"))
                    {
                        Utils.log_error("Place search not found: " + querystring);
                        size = 0;
                    }else
                    {
                        size = Math.min(arrayJsonResult.size(),Constants.search_maxresult);

                    }

//                    ArrayNode current_venue = null;
                    JsonNode current_venue = null;

                    for (int i = 0;i<size;i++){
                        current_venue = json_result.withArray("results").get(i);
//                        search_result.p
//                        search_result.put(i,Constants.proto_placename,current_venue.findPath("name").textValue());

                        Map<String, JsonNode> search_result = new HashMap<String, JsonNode>();



                        search_result.put(Constants.proto_location, Json.toJson(String.format("%.5f,%.5f",
                                Double.parseDouble(current_venue.findPath("geometry").findPath("location").findPath("lat").toString()),
                                Double.parseDouble(current_venue.findPath("geometry").findPath("location").findPath("lng").toString()))));

                        search_result.put(Constants.proto_placename, Json.toJson(current_venue.get("name").textValue()));

                        search_res.add(i, search_result);


//                        Map<Integer,String> key = new HashMap<Integer,String>();
//
//                        key.put(i,Constants.proto_placename);
//
//                        search_result.put(key, current_venue.get("name").textValue());
//
//                        Logger.debug("1" + search_result.toString());
//
//                        key.put(i, Constants.proto_location);
////
////                        search_result.put(key, String.format("%." + Constants.latlon_precision + "f,%." + Constants.latlon_precision + "f",
////                                current_venue.findPath("geometry").findPath("location").findPath("lat").toString(),
////                                current_venue.findPath("geometry").findPath("location").findPath("lon").toString()));
//
//
//                        search_result.put(key, String.format("%.5f,%.5f",
//                                Double.parseDouble(current_venue.findPath("geometry").findPath("location").findPath("lat").toString()),
//                                Double.parseDouble(current_venue.findPath("geometry").findPath("location").findPath("lng").toString())));

                        Logger.debug("2" + search_result.toString());
                        Logger.debug("3" + search_res.toString());

//                        !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    }

                    Logger.debug("HASIL SEARCH_RESULT:" + search_res.toString());


                    json_output.put(Constants.proto_status, Constants.proto_status_ok);
                    json_output.put(Constants.proto_search_result, Json.toJson(search_res));
                    json_output.put(Constants.proto_attributions, Json.toJson(NullNode.getInstance()));




                    Utils.log_statistic(apikey, "SEARCHPLACE", querystring + "/" + size);
                    String str = Json.stringify(Json.toJson(search_res));
                    Logger.debug("inserting: "+ str);
                    Utils.put_to_cache(Constants.cache_searchplace, region + "/" + querystring, str);


                }else
                {
//                    Logger.error("Place Search returned error: for this request " + full_url);
                }

            }


//            Logger.debug("sebelom : " + search_res.get(0).toString());

            String str = Json.stringify(json_output);
            Logger.debug("mode search json : " + json_output.toString());
            output.append(str);


        }else if(mode.equals(Constants.proto_mode_reporterror))
        {
            String errorcode = retrieve_data(Constants.proto_errorcode);

            Utils.log_error("Client reported error: " + errorcode);

            Logger.debug("reporterror:"+Utils.well_done("").toString());

            output.append(Utils.well_done("").toString());


        }else if (mode.equals(Constants.proto_mode_nearbytransports))
        {
            String start = retrieve_data(Constants.proto_routestart);

            if(version >= 2)
            {
                String result_menjangan_url = getFromMenjangan(start);
                String lines[] = result_menjangan_url.split("\n");

                Logger.debug(result_menjangan_url);

                ArrayList<ArrayList<String>> nearby_result = new ArrayList<ArrayList<String>>();


                for(String line : lines){
                    String[] listLine = line.split("/");

                    String trackTypeId = listLine[0];
                    String trackId = listLine[1];
                    String distance = listLine[2];

                    Connection connection = DB.getConnection();
//                        StringBuilder output = new StringBuilder();
                    try {
//                            connection = DB.getConnection();
                        // Look for angkot.web.id refreshes


                        java.sql.PreparedStatement stmt = connection.prepareStatement(
                                "SELECT trackname FROM tracks WHERE trackId = ? AND trackTypeId = ?");
                        stmt.setString(1, trackId);
                        stmt.setString(2,trackTypeId);

//                        Statement statement = connection.createStatement();
//
//                        ResultSet result = statement.executeQuery("SELECT trackname FROM tracks WHERE trackId='"+ trackId + "' AND trackTypeId='"+trackTypeId+ "';");

                        ResultSet result = stmt.executeQuery();
                        while (result.next()) {
                            //php starts from 0,jdbc start from 1

                            String trackName = result.getString(1);
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(trackTypeId);
                            list.add(trackId);
                            list.add(trackName);
                            list.add(distance);
//                            nearby_result.add(trackTypeId);
//                            nearby_result.add(trackId);
//                            nearby_result.add(trackName);
//                            nearby_result.add(distance);
                            nearby_result.add(list);

                        }


                        connection.close();
                    } catch (Exception e) {
                        Utils.log_error("Can't get nearest track details:  " + e.getMessage());
                    }

                }

//                usort;
//                nearby_result.sort(new Comparator<String>() {
//                    @Override
//                    public int compare(String o1, String o2) {
//                        return 0;
//                    }
//                });

                Logger.debug(nearby_result.toString());
                Collections.sort(nearby_result, comparator);

                Utils.log_statistic(apikey, "NEARBYTRANSPORTS", start + results.size());


                Logger.debug("after compare : " + nearby_result.toString());
                ObjectNode json_output = Json.newObject();

                json_output.put(Constants.proto_status,Constants.proto_status_ok);
                json_output.put(Constants.proto_nearbytransports,Json.toJson(nearby_result));


                output.append(json_output.toString());

            }else
            {
                Utils.log_error("Nearby transit is not supported in version 1");
            }


        }else
        {
            Utils.log_error("Mode not understood: "+ mode);
        }
//        output.append(mode);
        return ok(output.toString());
    }



    public String getFromMenjangan(String start,String finish,double mw,double wm,double pt){
        F.Promise<String> promise = WS.url(Constants.menjangan_url)
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
        F.Promise<String> promise = WS.url(Constants.menjangan_url)
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

    public String getFromMenjangan(String start) {
        F.Promise<String> promise = WS.url(Constants.menjangan_url)
                .setQueryParameter("start", start)
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


    public String getPlacesAPI(String location,String radius,String querystring) {
        F.Promise<String> promise = WS.url(Constants.places_url)
                .setQueryParameter("key", Constants.gmaps_server_key)
                .setQueryParameter("location",location)
                .setQueryParameter("radius",radius)
                .setQueryParameter("keyword",querystring)
                .setQueryParameter("types","establishment|route")
                .setQueryParameter("sensor","true")
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
            return Math.round(time) + " "+Messages.get("message_hour");
        }else{

            return (int) (5 * Math.ceil(time * 60.0 / 5)) + " " +Messages.get("message_min");
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






    public String check_apikey(String apikey){
        boolean bool = true;
        Connection connection = DB.getConnection();
        StringBuilder output = new StringBuilder();
        String ipAddr = request().remoteAddress();//ip address client
//        ObjectNode objectNode = Json.newObject();
        ObjectNode objectNode = Json.newObject();
        try {
//            connection = DB.getConnection();
            // Look for angkot.web.id refreshes

            java.sql.PreparedStatement stmt = connection.prepareStatement(
                    "SELECT apikeys.verifier, apikeys.ipFilter FROM apikeys WHERE apikeys.verifier = ?");
            stmt.setString(1, apikey);

//            Statement statement = connection.createStatement();
//            ResultSet result = statement.executeQuery("SELECT apikeys.verifier, apikeys.ipFilter FROM apikeys WHERE apikeys.verifier = '"+apikey+"';");

            ResultSet result = stmt.executeQuery();
            if(result.next()){
                while (result.next()) {

                    if(!ipAddr.equals(result.getString("ipFilter")) && !result.getString("ipFilter").isEmpty()){
                        bool = false;
                        objectNode = Utils.die_nice("IP address is not accepted for this API key.");
                    }

//                    output.append(result.getString("verifier") + "/" + result.getString("ipFilter") +"/" + result.getString("domainFilter"));

                }
            }else{
                bool = false;
                objectNode = Utils.die_nice("API key is not recognized: " + apikey);
            }

            connection.close();
        } catch (Exception e) {
            objectNode = Utils.die_nice("failed to execute query on Apikey check." + e.getMessage());
        }

        output.append(objectNode.toString());
        return output.toString();
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

//    public static String mysql_real_escape_string(java.sql.Connection link, String str)
//            throws Exception
//    {
//        if (str == null) {
//            return null;
//        }
//
//        if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]","").length() < 1) {
//            return str;
//        }
//
//        String clean_string = str;
//        clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
//        clean_string = clean_string.replaceAll("\\n","\\\\n");
//        clean_string = clean_string.replaceAll("\\r", "\\\\r");
//        clean_string = clean_string.replaceAll("\\t", "\\\\t");
//        clean_string = clean_string.replaceAll("\\00", "\\\\0");
//        clean_string = clean_string.replaceAll("'", "\\\\'");
//        clean_string = clean_string.replaceAll("\\\"", "\\\\\"");
//
//        if (clean_string.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]"
//                ,"").length() < 1)
//        {
//            return clean_string;
//        }
//
//        java.sql.Statement stmt = link.createStatement();
//        String qry = "SELECT QUOTE('"+clean_string+"')";
//
//        stmt.executeQuery(qry);
//        java.sql.ResultSet resultSet = stmt.getResultSet();
//        resultSet.first();
//        String r = resultSet.getString(1);
//        return r.substring(1,r.length() - 1);
//
//    }

    public String format_distance(double distance,String locale){


        if(distance<1.0){
            return (int) (Math.floor(distance*1000.0)) + " meter";
        }else{
            char decimal = locale == "id"?',':'.';
            double fdist = Math.floor(distance);
            return (int) (fdist + decimal + Math.floor((distance - fdist) * 10)) + " kilometer";
        }

    }





}
