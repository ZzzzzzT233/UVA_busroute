package edu.virginia.cs.hw6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static java.lang.Integer.lowestOneBit;
import static java.lang.Integer.parseInt;

public class ApiBusLineReader implements BusLineReader {
    private List<BusLine> BusLine_list;
    public ApiBusLineReader() {
        BusLine_list = new ArrayList<>();
    }
    @Override
    public List<BusLine> getBusLines(){
        ConfigSingleton configSingleton = ConfigSingleton.getInstance(); // set up the lines url
        String busLinesURL = configSingleton.getBusLinesURL();
        String text = readUrltoString(busLinesURL);
        JSONObject o = new JSONObject(text);

        String busStopUrl = configSingleton.getBusStopsURL(); //Set up the stops url
        String text1 = readUrltoString(busStopUrl);
        JSONObject s = new JSONObject(text1);

        Map<Integer, Stop> stopMap = getIntegerStopMap(s);

        JSONArray routesArray = o.getJSONArray("routes");
        for (Object e : routesArray) {
            if (e instanceof JSONObject r) {
                int id = (int) r.get("id");
                boolean isActive = r.getBoolean("is_active");
                String longName = r.getString("long_name");
                String shortName = r.getString("short_name");
                Map<Integer, List<Stop>> routeStopMap = getRouteStopMap(s, stopMap);

                if (routeStopMap.containsKey(id)) {
                    Route route = new Route(routeStopMap.get(id));
                    BusLine busLine = new BusLine(id, isActive, longName, shortName, route);
                    BusLine_list.add(busLine);
                } else {
                    BusLine busLine = new BusLine(id, isActive, longName, shortName);
                    BusLine_list.add(busLine);
                }

            }
        }
        return BusLine_list;
    }

  private Map<Integer,List<Stop>> getRouteStopMap(JSONObject s, Map<Integer, Stop> stopMap){
      Map<Integer,List<Stop>> routeStopMap = new HashMap<>();
        JSONArray routesArray = s.getJSONArray("routes");
        for(Object routes: routesArray){
            if(routes instanceof JSONObject route){
                List<Stop> stopList = new ArrayList<>();
                int key = (int) route.get("id");
                JSONArray stopIds = route.getJSONArray("stops");
                for (int i = 0; i<stopIds.length();i++){
                    int stopId = (int) stopIds.get(i);
                    if(stopMap.containsKey(stopId)){
                        stopList.add(stopMap.get(stopId));
                    }
                }
                routeStopMap.put(key,stopList);
            }
        }
        return routeStopMap;
  }

    private static Map<Integer, Stop> getIntegerStopMap(JSONObject s) {
        Map<Integer,Stop> stopMap = new HashMap<>();
        JSONArray stopsArray = s.getJSONArray("stops");
        for(Object stops:stopsArray){
            if (stops instanceof JSONObject stop){
                int key = (int) stop.get("id");
                String name = stop.getString("name");
                JSONArray positionArray = stop.getJSONArray("position");
                double latitude = (double) positionArray.getDouble(0);
                double longitude = (double) positionArray.getDouble(0);
                Stop curStop = new Stop(key,name,latitude,longitude);
                stopMap.put(key,curStop);
            }
        }
        return stopMap;
    }

    public String readUrltoString(String link){
        try{
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String result = content.toString();
            return result;
        }catch (IOException e){
            System.out.println("The URL does not exit");
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args){
        ApiBusLineReader test_apiblr = new ApiBusLineReader();
        for (BusLine b : test_apiblr.getBusLines()) {
            System.out.println(b.toString());
        }
    }
}
