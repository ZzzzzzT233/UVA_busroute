package edu.virginia.cs.hw6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApiStopReader implements StopReader {
    private List<Stop> Stops_list;
    public ApiStopReader() {
        Stops_list = new ArrayList<>();
    }
    @Override
    public List<Stop> getStops(){
//        List<Stop> stopList = new ArrayList<>();
        ConfigSingleton configSingleton = ConfigSingleton.getInstance();
        String busStopUrl = configSingleton.getBusStopsURL();
        String text = readUrltoString(busStopUrl);
        JSONObject o = new JSONObject(text);
        JSONArray stopsArray = o.getJSONArray("stops");
        for (Object e : stopsArray) {
            if (e instanceof JSONObject stops) {
                int id = stops.getInt("id");
                String name = stops.getString("name");
                JSONArray positionArray = stops.getJSONArray("position");
                double latitude = positionArray.getDouble(0);
                double longitude = positionArray.getDouble(1);
                Stop stop = new Stop(id, name, latitude, longitude);
                Stops_list.add(stop);
            }
        }
        return Stops_list;
    }

    public String readUrltoString(String link){
        try {
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
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args){
        ApiStopReader test_apisr = new ApiStopReader();
        test_apisr.getStops();
        for(Stop s : test_apisr.Stops_list) {
            System.out.println(s.getName());
        }
    }


}
