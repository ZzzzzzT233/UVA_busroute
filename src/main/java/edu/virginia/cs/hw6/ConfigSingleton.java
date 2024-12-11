
package edu.virginia.cs.hw6;

import org.json.JSONObject;

import java.io.*;
import java.nio.Buffer;
import java.rmi.RemoteException;

public class ConfigSingleton {
    private static final String configurationFileName = "config.json";
    private static ConfigSingleton instance;
    private String busStopsURL;
    private String busLinesURL;
    private String databaseName;

    private ConfigSingleton(){             // change the field from private to public
        setFieldsFromJSON();
    }

    public static ConfigSingleton getInstance(){
        if (instance == null) {
            instance = new ConfigSingleton();
        }
        return instance;
    }

    public String getBusStopsURL() {
        return busStopsURL;
    }

    public String getBusLinesURL() {
        return busLinesURL;
    }

    public String getDatabaseFilename() {
        return databaseName;
    }

    private void setFieldsFromJSON(){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String fileName = "edu.virginia.cs.hw6/" + configurationFileName;
            String file = classLoader.getResource(fileName).getFile();
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            String text = sb.toString();
            JSONObject o = new JSONObject(text);
            JSONObject endPoints = o.getJSONObject("endpoints");
            this.busStopsURL = endPoints.getString("stops");
            this.busLinesURL = endPoints.getString("lines");
            this.databaseName = o.getString("database");
            br.close();
        }catch(IOException e){
            throw new RuntimeException("cannot have access to the file");
        }
    }


}

