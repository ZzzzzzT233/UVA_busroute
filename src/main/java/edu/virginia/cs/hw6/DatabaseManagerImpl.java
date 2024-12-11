package edu.virginia.cs.hw6;

import org.sqlite.SQLiteException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.PKIXCertPathBuilderResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManagerImpl implements DatabaseManager {
    private Connection connection;
    ConfigSingleton config = ConfigSingleton.getInstance();
    private final String url = "jdbc:sqlite:"+config.getDatabaseFilename();

    public DatabaseManagerImpl(){
    }



    @Override
    public void connect(){
        try {
            if (connection == null) {
                Class.forName("org.sqlite.JDBC");
                this.connection = DriverManager.getConnection(url);
            } else {
                throw new IllegalStateException("the manager is already connected");
            }
        }catch(SQLException e){
            throw new RuntimeException();
        }catch(ClassNotFoundException e){
            throw new RuntimeException();
        }
    }

    @Override
    public void createTables() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalStateException("The Manager has not connected yet!");
            }
            Statement statement1 = connection.createStatement();
            String stopTableQuery = "CREATE TABLE IF NOT EXISTS Stops (ID int NOT NULL, Name VARCHAR(255) NOT NULL, " +
                    "Latitude DOUBLE NOT NULL, Longitude DOUBLE NOT NULL, PRIMARY KEY (ID))";
            statement1.executeUpdate(stopTableQuery);
            Statement statement2 = connection.createStatement();
            String busLineTableQuery ="CREATE TABLE IF NOT EXISTS BusLines (ID int NOT NULL PRIMARY KEY, IsActive BOOLEAN NOT NULL, " +
                    "LongName VARCHAR(255) NOT NULL, ShortName VARCHAR(255) NOT NULL) ";
            statement2.executeUpdate(busLineTableQuery);
            Statement statement3 = connection.createStatement();
            String routesTableQuery ="CREATE TABLE Routes (ID INTEGER PRIMARY KEY, " +
                    "BusLineID int NOT NULL," +
                    "StopID int NOT NULL," +
                    "\"Order\" int NOT NULL," +
                    "FOREIGN KEY (BusLineID) REFERENCES BusLines(ID) ON DELETE CASCADE," +
                    "FOREIGN KEY (StopID) REFERENCES Stops(ID) ON DELETE CASCADE)";
            statement3.executeUpdate(routesTableQuery);
        }catch (SQLException e){
            throw new RuntimeException("The tables are created!");
        }
    }


    @Override
    public void clear() {
    try{
        if(connection == null || connection.isClosed()){
            throw new IllegalStateException("The connection is missing!");
        }
        String deleteStopTableQuery = "DELETE FROM Stops";
        String deleteBusLineTableQuery = "DELETE FROM BusLines";
        String deleteRoutesTableQuery = "DELETE FROM Routes";

        try{
            Statement statement1 = connection.createStatement();
            statement1.executeUpdate(deleteStopTableQuery);
        }catch (SQLException e){
            throw new IllegalStateException("Table Stop does not exist!");
        }
        try{
            Statement statement2 = connection.createStatement();
            statement2.executeUpdate(deleteBusLineTableQuery);
        }catch (SQLException e){
            throw new IllegalStateException("Table BusLines does not exist!");
        }
        try{
            Statement statement3 = connection.createStatement();
            statement3.executeUpdate(deleteRoutesTableQuery);
        }catch (SQLException e){
            throw new IllegalStateException("Table Routes does not exist!");
        }

        }catch (SQLException e){
            throw new IllegalStateException("Sqlite is not working!");
        }
    }

    @Override
    public void deleteTables() {
        try{
            if(connection == null || connection.isClosed()){
                throw new IllegalStateException("The connection is missing!");
            }
            String deleteStopTableQuery = "drop TABLE Stops";
            String deleteBusLineTableQuery = "drop TABLE BusLines";
            String deleteRoutesTableQuery = "drop table Routes";

            try{
                Statement statement1 = connection.createStatement();
                statement1.executeUpdate(deleteStopTableQuery);
            }catch (SQLException e){
                throw new IllegalStateException("Table Stop does not exist!");
            }
            try{
                Statement statement2 = connection.createStatement();
                statement2.executeUpdate(deleteBusLineTableQuery);
            }catch (SQLException e){
                throw new IllegalStateException("Table BusLines does not exist!");
            }
            try{
                Statement statement3 = connection.createStatement();
                statement3.executeUpdate(deleteRoutesTableQuery);
            }catch (SQLException e){
                throw new IllegalStateException("Table Routes does not exist!");
            }
        }catch (SQLException e){
            throw new IllegalStateException("The connection is closed!");
        }
    }

    @Override
    public void addStops(List<Stop> stopList) {
        try{
        if(connection == null || connection.isClosed()){
            throw new IllegalStateException("The connection is closed!");
        }
        if(!tableExist("Stops")){
            throw new IllegalStateException("The Stops table does not exist!");
        }
        String sql = "INSERT into Stops(ID, Name, Latitude, Longitude) Values(?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for(Stop stop: stopList) {
                String checkSql = "SELECT COUNT(*) FROM Stops WHERE ID = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkSql);
                checkStatement.setInt(1, stop.getId());
                ResultSet resultSet = checkStatement.executeQuery();
                resultSet.next();
                int count = resultSet.getInt(1);
                if (count > 0) {
                    throw new IllegalArgumentException("Stop with ID " + stop.getId() + " already exists in the Stops table");
                }

                // Add the Stop to the batch
                preparedStatement.setInt(1, stop.getId());
                preparedStatement.setString(2, stop.getName());
                preparedStatement.setDouble(3, stop.getLatitude());
                preparedStatement.setDouble(4, stop.getLongitude());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            System.out.println("Successfully added into the table ");
        }catch (SQLException e){
            throw new RuntimeException();
        }

    }
    public boolean tableExist(String tableName){
        try {
            ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null);
            return rs.next();
        }catch(SQLException e){
            throw new RuntimeException();
        }
    }

    @Override
    public List<Stop> getAllStops() {
        List<Stop> stopList = new ArrayList<>();
        try{
            if(connection == null || connection.isClosed()){
                throw new IllegalStateException("The connection is closed!");
            }
            Statement statement = connection.createStatement();
            String sql = "SELECT * from Stops";
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()){
                int id = rs.getInt("ID");
                String name = rs.getString("Name");
                double latitude = rs.getDouble("Latitude");
                double longitude = rs.getDouble("Longitude");
                Stop stop = new Stop(id,name,latitude,longitude);
                stopList.add(stop);
            }
            
        }catch (SQLException e){
            throw new IllegalStateException("The Stops Table does not exist!");
        }
        return stopList;
    }

    @Override
    public Stop getStopByID(int id) {
        try{
       if(connection == null || connection.isClosed()){
           throw new IllegalStateException("The connection is closed!");
       }

           Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery("SELECT * FROM Stops where ID="+id);
           if(!rs.next()){
               throw new IllegalArgumentException("The stop with that ID does not exist");
           }
           int stopID = rs.getInt("id");
           String name = rs.getString("name");
           double latitude = rs.getDouble("latitude");
           double longitude = rs.getDouble("longitude");
           Stop stop = new Stop(stopID,name,latitude,longitude);
           return stop;
       }catch (SQLException e) {
           throw new IllegalStateException("Stops table does not exist!");
       }
    }

    @Override
    public Stop getStopByName(String substring) {
        try {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is closed!");
        }

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Stops WHERE Name = ?");
            preparedStatement.setString(1, substring);
            ResultSet rs = preparedStatement.executeQuery();
            int stopID = 100000000;
            Stop stop = null;
            while (rs.next()) {
                if (rs.getInt("ID") < stopID) {
                    stopID = rs.getInt("ID");
                    String name = rs.getString("Name");
                    double latitude = rs.getDouble("Latitude");
                    double longitude = rs.getDouble("Longitude");
                    stop = new Stop(stopID, name, latitude, longitude);
                }
            }
            if (stop == null){
                throw new IllegalArgumentException("The stop with that ID does not exist");
            }
            return stop;
        } catch (SQLException e) {
            throw new IllegalStateException("Stops table does not exist!");
        }
    }
        @Override
    public void addBusLines(List<BusLine> busLineList){
            try{
            if(connection == null|| connection.isClosed()){
                throw new IllegalStateException("The connection is closed!");
            }
            if(!tableExist("BusLines") ){
                throw new IllegalStateException("The busLine table does not exist!");
            }
            if(!tableExist("Stops")){
                throw new IllegalStateException("The Stops table does not exist!");
            }
            if(!tableExist("Routes")){
                throw new IllegalStateException("The Routes table does not exist!");
            }
            String sql = "INSERT into BusLines(ID, isActive, LongName, ShortName) Values(?,?,?,?)";

                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT * FROM Stops");
                if (!rs.next()){
                    throw new IllegalStateException("The Stops table is empty!");
                }
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for(BusLine busLine:busLineList) {
                    String checkSql = "SELECT COUNT(*) FROM BusLines WHERE ID = ?";
                    PreparedStatement checkStatement = connection.prepareStatement(checkSql);
                    checkStatement.setInt(1, busLine.getId());
                    ResultSet resultSet = checkStatement.executeQuery();
                    resultSet.next();
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        throw new IllegalArgumentException("busLine with ID " + busLine.getId() + " already exists in the BusLines table");
                    }
                    preparedStatement.setInt(1, busLine.getId());
                    preparedStatement.setBoolean(2, busLine.isActive());
                    preparedStatement.setString(3, busLine.getLongName());
                    preparedStatement.setString(4, busLine.getShortName());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                System.out.println("Successfully added into the table ");

                String sQl = "INSERT into Routes (BusLineID, StopID,\"Order\") VALUES(?,?,?)";
                PreparedStatement statement1 = connection.prepareStatement(sQl);
                for (BusLine bl: busLineList){
                    Route route = bl.getRoute();

                    for(int i = 0; i< route.size(); i++) {
                        Stop stop = route.get(i);
                        try {
                            getStopByID(stop.getId());
                            statement1.setInt(1,bl.getId());
                            statement1.setInt(2,stop.getId());
                            statement1.setInt(3,i);
                            statement1.executeUpdate();
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("The stop that needed be added into the route does not exist!");
                        }
                    }
                }

            }catch (SQLException e){
                throw new RuntimeException();
            }
    }

    @Override
    public List<BusLine> getBusLines() {
        List<BusLine> busLineList = new ArrayList<>();
        try{
            if(connection == null){
                throw new IllegalStateException("The connection is closed!");
            }
            Statement statement = connection.createStatement();
            String sql = "SELECT * from BusLines";
            ResultSet rs = statement.executeQuery(sql);

            while(rs.next()){
                int id = rs.getInt("ID");
                boolean isActive = rs.getBoolean("IsActive");
                String LongName = rs.getString("LongName");
                String ShortName = rs.getString("ShortName");
                // try to add route into the busline
                try{
                    String sql1 = "SELECT * from Routes where BusLineID ="+ id;
                    Statement statement1 = connection.createStatement();
                    ResultSet rs1 = statement1.executeQuery(sql1);
                    List<Stop> stopList = new ArrayList<>();
                    while(rs1.next()){
                        int stopID = rs1.getInt("StopID");
                        Stop curStop = getStopByID(stopID);
                        stopList.add(curStop);
                    }
                    Route route = new Route(stopList);
                    BusLine busLine = new BusLine(id,isActive,LongName,ShortName,route);
                    busLineList.add(busLine);
                }catch(SQLException e){
                    throw new IllegalStateException("Stops, BusLines, or Route table does not exist");
                }

//                BusLine busLine = new BusLine(id,isActive,LongName,ShortName,route);
//                busLineList.add(busLine);
            }

        }catch (SQLException e){
            throw new IllegalStateException("The Table does not exist!");
        }
        return busLineList;
    }

    @Override
    public BusLine getBusLineById(int id) {
        try {
        if(connection == null|| connection.isClosed()){
            throw new IllegalStateException("The connection is closed!");
        }

            Statement st = connection.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM BusLines");
            if (!res.next()) {
                throw new IllegalStateException("The busLine is empty!");
            }
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM BusLines where ID=" + id);
            if (!rs.next()) {
                throw new IllegalArgumentException("The busLine with that id does not exist");
            }

            boolean isActive = rs.getBoolean("IsActive");
            String LongName = rs.getString("LongName");
            String ShortName = rs.getString("ShortName");

            // try to add route into the busline
            BusLine busLine;
            try {
                String sql1 = "SELECT * from Routes where BusLineID =" + id;
                Statement statement1 = connection.createStatement();
                ResultSet rs1 = statement1.executeQuery(sql1);
                List<Stop> stopList = new ArrayList<>();
                while (rs1.next()) {
                    int stopID = rs1.getInt("StopID");
                    Stop curStop = getStopByID(stopID);
                    stopList.add(curStop);
                }
                Route route = new Route(stopList);
                busLine = new BusLine(id, isActive, LongName, ShortName, route);
            } catch (SQLException e) {
                throw new IllegalStateException("Stops, BusLines, or Route table does not exist");
            }


            return busLine;
        }catch (SQLException e) {
            throw new IllegalStateException("Stops table does not exist!");
        }
    }

    @Override
    public BusLine getBusLineByLongName(String longName) {
        try {
        if (connection == null|| connection.isClosed()) {
            throw new IllegalStateException("The connection is closed!");
        }

            Statement st = connection.createStatement();
            ResultSet res =  st.executeQuery("SELECT * FROM BusLines");
            if(!res.next()){
                throw new IllegalStateException("The busLine is empty!");
            }
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BusLines WHERE LongName = ? COLLATE NOCASE");
            preparedStatement.setString(1, longName);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                int busLineID = rs.getInt("ID");
                boolean isActive = rs.getBoolean("IsActive");
                String LongName = rs.getString("LongName");
                String ShortName = rs.getString("ShortName");
                BusLine busLine = new BusLine(busLineID,isActive,LongName,ShortName);
                return busLine;
            }else{
                throw new IllegalArgumentException("The BusLine with that long-name is not found!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("The BusLine table does not exist!");
        }
    }

    @Override
    public BusLine getBusLineByShortName(String shortName) {
        try {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is closed!");
        }

            Statement st = connection.createStatement();
            ResultSet res =  st.executeQuery("SELECT * FROM BusLines");
            if(!res.next()){
                throw new IllegalStateException("The busLine is empty!");
            }
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BusLines WHERE ShortName = ? COLLATE NOCASE");
            preparedStatement.setString(1, shortName);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                int busLineID = rs.getInt("ID");
                boolean isActive = rs.getBoolean("isActive");
                String LongName = rs.getString("LongName");
                String ShortName = rs.getString("ShortName");
                BusLine busLine = new BusLine(busLineID,isActive,LongName,ShortName);
                return busLine;
            }else{
                throw new IllegalArgumentException("The busLine with that short-name is not found!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("The busLine Table does not exist");
        }
    }

    @Override
    public void disconnect(){
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalStateException("The the Manager hasn't connected yet");
            }
            //connection.commit();
            connection.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
